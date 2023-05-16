package com.mitmws.mvc.thread;

import com.mitmws.anomalydetection.DetectedAnomaly;
import com.mitmws.anomalydetection.DetectionRule;
import com.mitmws.client.WebsocketClient;
import com.mitmws.configuration.ApplicationConfig;
import com.mitmws.environment.Environment;
import com.mitmws.environment.EnvironmentItemScope;
import com.mitmws.httpproxy.HttpMessage;
import com.mitmws.httpproxy.HttpMessageParseException;
import com.mitmws.httpproxy.trafficlogger.WebsocketDirection;
import com.mitmws.httpproxy.trafficlogger.WebsocketTrafficRecord;
import com.mitmws.httpproxy.websocket.WebsocketException;
import com.mitmws.httpproxy.websocket.WebsocketFrame;
import com.mitmws.httpproxy.websocket.WebsocketFrameType;
import com.mitmws.integrations.python.Script;
import com.mitmws.integrations.python.ScriptManager;
import com.mitmws.logging.AppLog;
import com.mitmws.mvc.model.MainModel;
import com.mitmws.mvc.model.ProtocolTesterModel;
import com.mitmws.tester.RawWebsocketFrame;
import com.mitmws.util.GuiUtils;
import com.mitmws.util.HttpMessageUtil;

import javax.script.ScriptException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

public class RawTesterThread extends Thread {
    private Logger LOGGER = AppLog.getLogger(RawTesterThread.class.getName());
    private ProtocolTesterModel protocolTesterModel;
    private MainModel mainModel;
    public Environment environment = new Environment();
    private ApplicationConfig applicationConfig;
    boolean shutdownRequested = false;

    public RawTesterThread(MainModel mainModel ) {
        this.mainModel = mainModel;
        this.protocolTesterModel = mainModel.getProtocolTesterModel();
        applicationConfig = new ApplicationConfig();

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
        LOGGER.info("Raw tester thread started");
        RawWebsocketFrame rawWebsocketFrame = new RawWebsocketFrame();
        rawWebsocketFrame.setRawFrame(GuiUtils.parseHexString(mainModel.getProtocolTesterModel().getRawFrameHexStr()));
        ArrayList<WebsocketTrafficRecord> inboundFrames = runTest(rawWebsocketFrame);
        protocolTesterModel.addLogMessage("Shutting down");
        LOGGER.info("Raw tester thread stopping");
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

    public ArrayList<WebsocketTrafficRecord>  runTest( RawWebsocketFrame testFrame ) {
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
            websocketClient.sendRaw(testFrame);
            protocolTesterModel.addTestTrafficFrame(WebsocketDirection.OUTBOUND,conversationUuid,testFrame);

            // Read the response
            ArrayList<WebsocketFrame> frames = readFrames(websocketClient,testFrame,500);
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
            frames = readFrames(websocketClient,testFrame,500);
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