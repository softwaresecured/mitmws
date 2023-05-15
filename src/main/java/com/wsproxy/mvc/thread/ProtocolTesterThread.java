package com.wsproxy.mvc.thread;
import com.wsproxy.anomalydetection.DetectedAnomaly;
import com.wsproxy.anomalydetection.DetectionRule;
import com.wsproxy.client.WebsocketClient;
import com.wsproxy.configuration.ApplicationConfig;
import com.wsproxy.environment.Environment;
import com.wsproxy.environment.EnvironmentItemScope;
import com.wsproxy.httpproxy.*;
import com.wsproxy.httpproxy.trafficlogger.WebsocketDirection;
import com.wsproxy.httpproxy.trafficlogger.WebsocketTrafficRecord;
import com.wsproxy.httpproxy.websocket.WebsocketException;
import com.wsproxy.httpproxy.websocket.WebsocketFrame;
import com.wsproxy.httpproxy.websocket.WebsocketFrameType;
import com.wsproxy.integrations.python.Script;
import com.wsproxy.integrations.python.ScriptManager;
import com.wsproxy.logging.AppLog;
import com.wsproxy.mvc.model.FuzzRecordModel;
import com.wsproxy.mvc.model.MainModel;
import com.wsproxy.mvc.model.ProtocolTesterModel;
import com.wsproxy.tester.RawWebsocketFrame;
import com.wsproxy.util.GuiUtils;
import com.wsproxy.util.HttpMessageUtil;

import javax.script.ScriptException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

public class ProtocolTesterThread extends Thread {
    private Logger LOGGER = AppLog.getLogger(ProtocolTesterThread.class.getName());
    private ProtocolTesterModel protocolTesterModel;
    private MainModel mainModel;
    public Environment environment = new Environment();
    private ApplicationConfig applicationConfig;
    boolean shutdownRequested = false;

    public ProtocolTesterThread( MainModel mainModel ) {
        this.mainModel = mainModel;
        this.protocolTesterModel = mainModel.getProtocolTesterModel();
        applicationConfig = new ApplicationConfig();
        environment.loadEnvironment();

    }
    public void shutdown() {
        shutdownRequested = true;
        LOGGER.info("Shutdown requested");
    }
    public int getFuzzTestCount() {
        int count = 0;
        for ( int ruleId :  mainModel.getRulesModel().getActiveRules().getRules().keySet() ) {
            DetectionRule rule = mainModel.getRulesModel().getActiveRules().getRules().get(ruleId);
            try {
                if ( rule.isEnabled() && rule.getTestScope().equals("PRESENTATION-WS")) {
                    if ( rule.getActiveRuleType().equals("FUZZ-FRAME") || rule.getActiveRuleType().equals("FUZZ-FRAME-HEADER")) {
                        count += rule.getFuzzRange();
                    }
                    if ( rule.getActiveRuleType().equals("FRAME-CREATOR") ) {
                        for ( WebsocketFrame curFrame : mainModel.getProtocolTesterModel().getBaseFrames()) {
                            ArrayList<RawWebsocketFrame> mutatedFrames = rule.getMutations(curFrame);
                            count += mutatedFrames.size();
                        }
                    }
                }
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
        return count;
    }
    private String frameTypesToCsv( ArrayList<WebsocketFrameType> types ) {
        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < types.size(); i++ ) {
            if ( i > 0 ) {
                sb.append(", ");
            }
            sb.append(types.get(i).toString());
        }
        return sb.toString();
    }

    public HttpMessage getUpgradeRequest() throws HttpMessageParseException, ScriptException {
        HttpMessage msg = null;
        if ( protocolTesterModel.getUpgradeHelperScript() != null ) {
            msg = new HttpMessage();
            ScriptManager scriptManager = new ScriptManager();
            Script helperScript = scriptManager.getScript("upgrade",protocolTesterModel.getUpgradeHelperScript());
            String upgradeRequestStr = (String) helperScript.executeFunction("execute");
            if ( upgradeRequestStr != null ) {
                String scriptResponse = new String(upgradeRequestStr.getBytes());
                if ( scriptResponse != null ) {
                    byte[] reqBytes = scriptResponse.getBytes(StandardCharsets.UTF_8);
                    msg.fromBytes(reqBytes);
                }
            }
        }
        else {
            msg = protocolTesterModel.getUpgradeHttpMessage();
        }
        return msg;
    }

    public void run() {
        protocolTesterModel.setTestStatus("RUNNING");
        protocolTesterModel.getLogListModel().clear();
        mainModel.getProtocolTesterModel().setTotalTests(getFuzzTestCount());
        LOGGER.info("ProtocolTesterThread started");
        String testableFramesCsv = String.join(",",frameTypesToCsv(protocolTesterModel.getTestFrameTypes()));
        protocolTesterModel.addLogMessage(String.format("Testing frames: %s", testableFramesCsv));
        HashMap<Integer, DetectionRule> detectionRules = mainModel.getRulesModel().getActiveRules().getRules();
        String testName = String.format("FUZZ-%d", System.currentTimeMillis());
        // The seed payloads through each rule
        for ( int ruleId :  mainModel.getRulesModel().getActiveRules().getRules().keySet() ) {
            if ( shutdownRequested ) { break; }
            DetectionRule rule = detectionRules.get(ruleId);
            if ( rule.isEnabled() ) {
                try {
                    if ( rule.isEnabled() && rule.getTestScope().equals("PRESENTATION-WS") ) {
                        // Run all generated frames
                        ArrayList<WebsocketTrafficRecord> inboundFrames = null;
                        try {
                            if ( rule.getActiveRuleType().equals("FRAME-CREATOR")) {
                                protocolTesterModel.addLogMessage(String.format("Testing rule #%d - %s", ruleId,rule.getName()));
                                for ( WebsocketFrame curFrame : mainModel.getProtocolTesterModel().getBaseFrames()) {
                                    if ( shutdownRequested ) { break; }
                                    ArrayList<RawWebsocketFrame> mutatedFrames = rule.getMutations(curFrame);
                                    for (RawWebsocketFrame curMutatedFrame : mutatedFrames) {
                                        if ( shutdownRequested ) { break; }
                                        inboundFrames = runTest(curMutatedFrame);
                                        detectAnomalies(rule,testName,inboundFrames,GuiUtils.binToHexStr(curFrame.toBytes()));
                                    }
                                }
                            }
                        } catch (ScriptException e) {
                            e.printStackTrace();
                        }
                        // Fuzz the entire frame
                        if ( rule.getActiveRuleType().equals("FUZZ-FRAME")) {
                            protocolTesterModel.addLogMessage(String.format("Testing rule #%d - %s", ruleId,rule.getName()));
                            for ( WebsocketFrame curFrame : mainModel.getProtocolTesterModel().getBaseFrames()) {
                                if ( shutdownRequested ) { break; }
                                for ( int i = 0; i < rule.getFuzzRange(); i++ ) {
                                    if ( shutdownRequested ) { break; }
                                    String fuzzedFrameHex = rule.getPayloadMutationBySeed(GuiUtils.binToHexStr(curFrame.toBytes()), i, rule.getFuzzRatio());
                                    RawWebsocketFrame fuzzedFrame = new RawWebsocketFrame();
                                    fuzzedFrame.setRawFrame(GuiUtils.parseHexString(fuzzedFrameHex));
                                    inboundFrames = runTest(fuzzedFrame);
                                    detectAnomalies(rule,testName,inboundFrames,GuiUtils.binToHexStr(curFrame.toBytes()));
                                }
                            }
                        }
                        // Fuzz the frame header
                        if ( rule.getActiveRuleType().equals("FUZZ-FRAME-HEADER")) {
                            protocolTesterModel.addLogMessage(String.format("Testing rule #%d - %s", ruleId,rule.getName()));
                            for ( WebsocketFrame curFrame : mainModel.getProtocolTesterModel().getBaseFrames()) {
                                if ( shutdownRequested ) { break; }
                                for ( int i = 0; i < rule.getFuzzRange(); i++ ) {
                                    if ( shutdownRequested ) { break; }
                                    int pLen = 0;
                                    if ( curFrame.getPayload() != null ) {
                                        pLen = curFrame.getPayload().length;
                                    }
                                    String fuzzedFrameHex = rule.getFrameMutationBySeed(GuiUtils.binToHexStr(curFrame.toBytes()), i, rule.getFuzzRatio(),pLen);
                                    RawWebsocketFrame fuzzedFrame = new RawWebsocketFrame();
                                    fuzzedFrame.setRawFrame(GuiUtils.parseHexString(fuzzedFrameHex));
                                    inboundFrames = runTest(fuzzedFrame);
                                    detectAnomalies(rule,testName,inboundFrames,fuzzedFrameHex);
                                }
                            }
                        }
                    }
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
            }
        }
        protocolTesterModel.addLogMessage("Shutting down");
        LOGGER.info("ProtocolTesterThread stopping");
        protocolTesterModel.setTestStatus("STOPPED");
    }

    private void detectAnomalies( DetectionRule rule, String testName, ArrayList<WebsocketTrafficRecord> inboundFrames, String fuzzFrameHexStr) {
        ArrayList<DetectedAnomaly> detectedAnomalies = null;
        try {
            detectedAnomalies = rule.getDetectedAnomaliesForSequence(inboundFrames);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        if ( detectedAnomalies != null ) {
            for ( DetectedAnomaly anomaly : detectedAnomalies ) {
                LOGGER.info(String.format("Logged anomaly %s for sequence %s", anomaly.getAnomalyId(), anomaly.getConversationUuid()));
                anomaly.setDetector("Protocol tester");
                anomaly.setTestPayloadHexStr(fuzzFrameHexStr);
                anomaly.setTestName(testName);
                anomaly.setRecords(inboundFrames);
                mainModel.getProjectModel().getDetectedAnomalies().add(anomaly);
                mainModel.getAnomaliesModel().addAnomaly(anomaly);
            }
        }
    }

    public void incTestStep() {
        protocolTesterModel.setTestsCompleted(protocolTesterModel.getTestsCompleted()+1);
    }

    public void updateHttpSightGlass( HttpMessage req, HttpMessage resp ) {
        if( req != null && resp != null ) {
            String reqRes = String.format("%s%s", GuiUtils.getBinPreviewStr(req.getBytes()), GuiUtils.getBinPreviewStr(resp.getHeaderBytes()));
            protocolTesterModel.setLastHttpTxRx(reqRes);
        }
        else {
            protocolTesterModel.setLastHttpTxRx("");
        }
    }

    public ArrayList<WebsocketFrame> readFrames( WebsocketClient websocketClient, RawWebsocketFrame curMutatedFrame, long delay) throws WebsocketException {
        long iowaitStart = System.currentTimeMillis();
        boolean closeRequested = false;
        ArrayList<WebsocketFrame> readFrames = new ArrayList<WebsocketFrame>();
        do {
            ArrayList<WebsocketFrame> curFrames = websocketClient.recv();
            if ( curFrames != null ) {
                readFrames.addAll(curFrames);
                for ( WebsocketFrame frame: curFrames ) {
                    if( frame.getOpcode().equals(WebsocketFrameType.CLOSE)) {
                        closeRequested = true;
                    }
                }
            }
        } while ( System.currentTimeMillis()-iowaitStart<delay && !shutdownRequested && !closeRequested);
        return readFrames;
    }

    public ArrayList<WebsocketTrafficRecord>  runTest( RawWebsocketFrame curMutatedFrame ) {
        ArrayList<WebsocketTrafficRecord> inboundFrames = new ArrayList<WebsocketTrafficRecord>();
        try {
            String conversationUuid = UUID.randomUUID().toString();
            WebsocketClient websocketClient = new WebsocketClient();
            HttpMessage upgradeRequest = environment.process(EnvironmentItemScope.HTTP,getUpgradeRequest());
            HttpMessage upgradeResponse = websocketClient.handshake(upgradeRequest);

            protocolTesterModel.httpRequestResponseModel.setRequestResponse(
                    HttpMessageUtil.getRequestResponseString(upgradeRequest, upgradeResponse)
            );

            updateHttpSightGlass(upgradeRequest, upgradeResponse);
            // Send the test
            websocketClient.sendRaw(curMutatedFrame);
            protocolTesterModel.addTestTrafficFrame(WebsocketDirection.OUTBOUND,conversationUuid,curMutatedFrame);

            // Read the response
            ArrayList<WebsocketFrame> frames = readFrames(websocketClient,curMutatedFrame,500);
            for ( WebsocketFrame frame : frames ) {
                protocolTesterModel.addTestTrafficFrame(WebsocketDirection.INBOUND,conversationUuid,frame.toRawFrame());
                inboundFrames.add(new WebsocketTrafficRecord(frame));
            }
            // Close
            WebsocketFrame closeMsg = new WebsocketFrame();
            closeMsg.setFin(1);
            closeMsg.setDirection(WebsocketDirection.OUTBOUND);
            closeMsg.setOpcode(WebsocketFrameType.CLOSE);
            closeMsg.setMasked(1);
            closeMsg.setMaskKey(closeMsg.generateMaskBytes());
            closeMsg.setPayloadUnmasked(null);
            websocketClient.send(closeMsg);
            protocolTesterModel.addTestTrafficFrame(WebsocketDirection.OUTBOUND,conversationUuid,closeMsg.toRawFrame());
            frames = readFrames(websocketClient,curMutatedFrame,500);
            for ( WebsocketFrame frame : frames ) {
                protocolTesterModel.addTestTrafficFrame(WebsocketDirection.INBOUND,conversationUuid,frame.toRawFrame());
                inboundFrames.add(new WebsocketTrafficRecord(frame));
            }

        } catch (HttpMessageParseException e) {
            e.printStackTrace();
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (WebsocketException e) {
            ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        incTestStep();
        return inboundFrames;
    }
    public void delay( long msec ) {
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}