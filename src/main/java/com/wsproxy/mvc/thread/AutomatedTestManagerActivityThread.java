package com.wsproxy.mvc.thread;

import com.wsproxy.anomalydetection.AnomalyScanRequest;
import com.wsproxy.anomalydetection.DetectionRule;
import com.wsproxy.client.WebsocketClient;
import com.wsproxy.environment.Environment;
import com.wsproxy.environment.EnvironmentItemScope;
import com.wsproxy.httpproxy.*;
import com.wsproxy.httpproxy.trafficlogger.TrafficLogger;
import com.wsproxy.httpproxy.trafficlogger.TrafficSource;
import com.wsproxy.httpproxy.trafficlogger.WebsocketDirection;
import com.wsproxy.httpproxy.websocket.WebsocketException;
import com.wsproxy.httpproxy.websocket.WebsocketFrame;
import com.wsproxy.httpproxy.websocket.WebsocketFrameType;
import com.wsproxy.integrations.python.Script;
import com.wsproxy.integrations.python.ScriptManager;
import com.wsproxy.logging.AppLog;
import com.wsproxy.mvc.model.AutomatedTesterModel;
import com.wsproxy.mvc.model.InteractShTestPayload;
import com.wsproxy.mvc.model.InteractshModel;
import com.wsproxy.mvc.model.ProjectModel;
import com.wsproxy.projects.ProjectDataServiceException;
import com.wsproxy.tester.*;
import com.wsproxy.util.GuiUtils;
import com.wsproxy.util.TestUtil;

import javax.script.ScriptException;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

public class AutomatedTestManagerActivityThread extends Thread {
    private AutomatedTesterModel automatedTesterModel;
    private static Logger LOGGER = AppLog.getLogger(AutomatedTestManagerActivityThread.class.getName());
    private DateFormat trafficTimeFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public Environment environment = null;
    private AutomatedTestRun testRun = null;
    private boolean shutdownRequested = false;
    private TrafficLogger logger = null;
    private ArrayList<PayloadList> payloadLists = new ArrayList<>();
    private HashMap<Integer, DetectionRule> detectionRules = new HashMap<Integer, DetectionRule>();
    //private int lastTestCompleted = 0;
    private ProjectModel projectModel = null;
    private InteractshModel interactshModel;
    private int fuzzRange = 0;
    public AutomatedTestManagerActivityThread(AutomatedTesterModel automatedTesterModel, InteractshModel interactshModel, ProjectModel projectModel, TrafficLogger logger, AutomatedTestRun testRun, HashMap<Integer, DetectionRule> detectionRules, ArrayList<PayloadList> payloadLists) {
        this.automatedTesterModel = automatedTesterModel;
        this.logger = logger;
        this.projectModel = projectModel;
        environment = new Environment();
        environment.loadEnvironment();
        environment.setInteractshModel(interactshModel);
        this.testRun = testRun;
        this.payloadLists = payloadLists;
        this.detectionRules = detectionRules;
        this.interactshModel = interactshModel;
        testRun.setStatus("NOT STARTED");

        // get the range of fuzz tests if present
        for ( int ruleId : detectionRules.keySet() ) {
            DetectionRule rule = detectionRules.get(ruleId);
            try {
                if (rule.isEnabled() && rule.getActiveRuleType().equals("PAYLOAD-FUZZ")) {
                    fuzzRange = automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun().getFuzzSeedEnd()-automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun().getFuzzSeedStart();
                }
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
    }

    public HttpMessage getUpgradeRequest() throws HttpMessageParseException, ScriptException {
        HttpMessage msg = null;
        if ( testRun.getTestSequence().getUpgradeHelperScript() != null ) {
            msg = new HttpMessage();
            ScriptManager scriptManager = new ScriptManager();
            Script helperScript = scriptManager.getScript("upgrade",testRun.getTestSequence().getUpgradeHelperScript());
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
            msg = testRun.getTestSequence().getHttpMessage();
        }
        return msg;
    }

    public void shutdown() {
        LOGGER.info("Shutdown request detected");
        shutdownRequested = true;
    }


    public void updateTestStatus() {
        automatedTesterModel.getAutomatedTestExecutionModel().setTestName(testRun.getTestName());
        automatedTesterModel.getAutomatedTestExecutionModel().setTestCount(testRun.getTestCount());
        automatedTesterModel.getAutomatedTestExecutionModel().setPctComplete(testRun.getPctComplete());
        automatedTesterModel.getAutomatedTestExecutionModel().setStepCount(testRun.getStepCount());
        automatedTesterModel.getAutomatedTestExecutionModel().setTestsCompleted(testRun.getTestsCompleted());

    }

    public int getPayloadCount() {
        int count = 0 ;
        if ( testRun.isDryRun() ) {
            count = 1;
        }
        else {
            for ( PayloadList payloadList : payloadLists ) {
                if ( payloadList.isEnabled() ) {
                    count += payloadList.getPayloads().size();
                }
            }
            for ( int ruleId : detectionRules.keySet() ) {
                if ( detectionRules.get(ruleId).isEnabled()) {
                    try {
                        if (detectionRules.get(ruleId).getActiveRuleType().equals("PAYLOAD")) {
                            count += detectionRules.get(ruleId).getPayloads().size();
                        }
                        if (detectionRules.get(ruleId).getActiveRuleType().equals("PAYLOAD-INTERACTSH")) {
                            count += detectionRules.get(ruleId).getOOBPayloads(interactshModel).size();
                        }
                        if (detectionRules.get(ruleId).getActiveRuleType().equals("PAYLOAD-FUZZ")) {
                            count += fuzzRange;
                        }
                    } catch (ScriptException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return count;
    }

    public int getEncodedTargetCount() {
        int count = testRun.getTestSequence().getTestTargets().size();
        for ( TestTarget testTarget : testRun.getTestSequence().getTestTargets()) {
            count += testTarget.getEnabledEncodings().size();
        }
        return count;
    }

    public void testLog( String msgType, String text ) {
        automatedTesterModel.addTestLogMessage(msgType,testRun.getTestName(),text);
    }

    public void run() {
        try {
            int testRunId = projectModel.getProjectDataService().getTestRunIdByName(testRun.getTestName());
            automatedTesterModel.getAutomatedTestExecutionModel().setStatus("STARTED");
            testRun.setTestRunStartTime(System.currentTimeMillis());
            testRun.setTestCount(getEncodedTargetCount()*getPayloadCount());
            testRun.setStepCount(testRun.getTestCount()*testRun.getTestSequence().getTestSequenceItems().size());
            testRun.setTestsCompleted(0);
            WebsocketClient websocketClient = new WebsocketClient();
            long startTime = System.currentTimeMillis();
            long testRuntime = (System.currentTimeMillis()-startTime)/1000;
            testRun.setStatus("RUNNING");

            LOGGER.info(String.format("Starting automated test run %s/%s. Full seq replay = %b, connection resuse=%b, dryrun = %b",
                    testRun.getTestId(),
                    testRun.getTestName(),
                    testRun.isContinueReplayAfterTestInsertion(),
                    testRun.isReuseConnection(),
                    testRun.isDryRun())
            );
            testLog("INFO", String.format("Running automated test sequence %s", testRun.getTestName()));
            /*
                Duplicate payloads that result from multiple encodings encoding to the same value are removed.
                The test progress is offset by this value
             */
            for ( TestTarget testTarget : testRun.getTestSequence().getTestTargets()) {
                if ( !testTarget.isEnabled()) {
                    continue;
                }
                if ( shutdownRequested ) {break;}
                testLog("INFO", String.format("Testing target %s ( %d-%d )", testTarget.getTargetName(),testTarget.getStartPos(),testTarget.getEndPos()));
                // Dry run
                if ( testRun.isDryRun() ) {
                    testLog("INFO", "Dry run mode - only running 1 payload");
                    ArrayList<byte[]> testPayloads = new ArrayList<byte[]>();
                    testPayloads.add("TESTPAYLOAD".getBytes(StandardCharsets.UTF_8));
                    testRun.setTestsCompleted(testRun.getTestsCompleted() + 1);
                    updateTestStatus();
                    testPayloadBatch(websocketClient,testPayloads,testRunId,testTarget);
                    continue;
                }
                else {
                    // Test payloads
                    for ( PayloadList payloadList : payloadLists ) {
                        if ( shutdownRequested ) {break;}
                        if ( !payloadList.isEnabled() ) { continue; }
                        for ( String basePayload : payloadList.getPayloads()) {
                            if ( shutdownRequested ) {break;}
                            ArrayList<String> encodedPayloads = TestUtil.encodePayloads(testTarget.getEnabledEncodings(),basePayload);
                            int pcDiff = testTarget.getEnabledEncodings().size()-(encodedPayloads.size()-1);
                            testRun.setTestsCompleted(testRun.getTestsCompleted() + pcDiff);
                            updateTestStatus();
                            ArrayList<byte[]> encPayloads = new ArrayList<byte[]>();
                            for ( String encPayload : encodedPayloads ) {
                                encPayloads.add(encPayload.getBytes(StandardCharsets.UTF_8));
                            }
                            testPayloadBatch(websocketClient,encPayloads,testRunId,testTarget);
                        }
                    }
                    // Test PAYLOAD, PAYLOAD-INTERACTSH active rules
                    for ( int ruleId : detectionRules.keySet() ) {
                        DetectionRule rule = detectionRules.get(ruleId);
                        if ( rule.isEnabled() && ( rule.getActiveRuleType().equals("PAYLOAD") | rule.getActiveRuleType().equals("PAYLOAD-INTERACTSH"))) {
                            if ( !testTarget.getEnabledEncodings().contains(PayloadEncoding.RAW)) {
                                testTarget.getEnabledEncodings().add(PayloadEncoding.RAW);
                            }
                            for ( PayloadEncoding payloadEncoding : testTarget.getEnabledEncodings() ) {

                                if ( rule.getActiveRuleType().equals("PAYLOAD") ) {
                                    ArrayList<byte[]> encodedRulePayloads = new ArrayList<byte[]>();
                                    for ( String basePayload : rule.getPayloads()) {
                                        String encodedPayload = TestUtil.encodePayload(payloadEncoding,basePayload);
                                        encodedRulePayloads.add(encodedPayload.getBytes(StandardCharsets.UTF_8));
                                    }
                                    ArrayList<String> conversationUuids = testPayloadBatch(websocketClient,encodedRulePayloads,testRunId,testTarget);
                                    projectModel.getActiveAnomalyScanQueue().add(new AnomalyScanRequest(ruleId,conversationUuids,testRun.getTestName()));
                                    updateTestStatus();
                                }
                                // Batches of 1 because needs to be 1:1 with intsh requests
                                if ( rule.getActiveRuleType().equals("PAYLOAD-INTERACTSH") ) {
                                    for ( InteractShTestPayload basePayload : rule.getOOBPayloads(interactshModel)) {
                                        ArrayList<byte[]> encodedRulePayloads = new ArrayList<byte[]>();
                                        String encodedPayload = TestUtil.encodePayload(payloadEncoding,basePayload.getTestPayload());
                                        encodedRulePayloads.add(encodedPayload.getBytes(StandardCharsets.UTF_8));
                                        ArrayList<String> conversationUuids = testPayloadBatch(websocketClient,encodedRulePayloads,testRunId,testTarget);
                                        if ( conversationUuids.size() > 0 ) {
                                            interactshModel.associatePayload(
                                                    basePayload.getInteractShPayload().split("\\.")[0],
                                                    String.format("%s\t%s\t%d", conversationUuids.get(0), testRun.getTestName(),ruleId));
                                        }
                                        updateTestStatus();
                                    }
                                }
                            }
                        }
                    }
                    // Test PAYLOAD-FUZZ rules
                    for ( int ruleId : detectionRules.keySet() ) {
                        DetectionRule rule = detectionRules.get(ruleId);
                        if ( rule.isEnabled() && rule.getActiveRuleType().equals("PAYLOAD-FUZZ") ) {
                            int start = automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun().getFuzzSeedStart();
                            int end = automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun().getFuzzSeedEnd();
                            double ratio = automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun().getFuzzRatio();
                            for ( int i = start; i < end; i++ ) {
                                byte buff[] = getPayloadSource(testTarget);
                                if ( buff != null ) {
                                    // TODO: perhaps batch since testPayloadBatch wants an array anyway
                                    String payloadHex = rule.getPayloadMutationBySeed(GuiUtils.binToHexStr(buff),i,ratio);
                                    ArrayList<byte[]> payloads = new ArrayList<>();
                                    payloads.add(GuiUtils.parseHexString(payloadHex));
                                    ArrayList<String> conversationUuids = testPayloadBatch(websocketClient,payloads,testRunId,testTarget);
                                    projectModel.getActiveAnomalyScanQueue().add(new AnomalyScanRequest(ruleId,conversationUuids,testRun.getTestName()));
                                    updateTestStatus();
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException|ClassNotFoundException|ProjectDataServiceException| WebsocketException |ScriptException|HttpMessageParseException e) {
            e.printStackTrace();
            testLog("ERROR",e.getMessage());
        }
        if ( shutdownRequested ) {
            testRun.setStatus("STOPPED");
        }
        else {
            testRun.setStatus("COMPLETE");
        }
        updateTestStatus();
    }

    public ArrayList<String> testPayloadBatch(WebsocketClient websocketClient, ArrayList<byte[]> testPayloads, int testRunId, TestTarget testTarget ) throws WebsocketException, ScriptException, HttpMessageParseException {
        ArrayList<String> conversations = new ArrayList<String>();
        for ( byte testPayload[] : testPayloads ) {
            if ( shutdownRequested ) {break;}

            if ( !websocketClient.isConnected() ) {
                establishWebsocketConnection(websocketClient,testRunId);
            }

            try {
                conversations.add(executeTest(websocketClient,testTarget,testPayload,testRunId));
            } catch (WebsocketException e) {
                testLog("ERROR",e.getMessage());
                e.printStackTrace();
            }
            finally {
                if ( !testRun.isReuseConnection()) {
                    websocketClient.disconenct();
                }
            }
            //lastTestCompleted += 1;
            testRun.setTestsCompleted(testRun.getTestsCompleted() + 1);
            updateTestStatus();
        }
        return conversations;
    }

    public void establishWebsocketConnection( WebsocketClient websocketClient, int testRunId) throws WebsocketException, HttpMessageParseException, ScriptException {
        HttpMessage upgradeMsg = environment.process(EnvironmentItemScope.HTTP,getUpgradeRequest());
        HttpMessage upgradeResponse = websocketClient.handshake(upgradeMsg);
        logger.logRFC2616Message(TrafficSource.AUTOMATED_TEST,upgradeMsg,upgradeResponse,testRun.getTestName(),testRunId);
    }

    /*
        Gets the source for a test location, used for fuzzing
     */
    public byte[] getPayloadSource( TestTarget testTarget ) {
        byte buff[] = null;
        int testableElementIdx = 0;
        for (TestSequenceItem testSequenceItem : testRun.getTestSequence().getTestSequenceItems()) {
            if (testSequenceItem.getTestSequenceItemType().equals(TestSequenceItemType.FRAME)) {
                if (testSequenceItem.getFrame().getDirection().equals(WebsocketDirection.OUTBOUND)) {
                    if (testableElementIdx == testTarget.getTestableStepIdx()) {
                        int buffLen = testTarget.getEndPos()-testTarget.getStartPos();
                        buff = new byte[buffLen];
                        System.arraycopy(testSequenceItem.getFrame().getPayloadUnmasked(),testTarget.getStartPos(),buff,0,buffLen);
                    }
                    testableElementIdx += 1;
                }
            }
        }
        return buff;
    }
    /*
        Replays the conversation and inserts the payload at the target
     */
    public String executeTest ( WebsocketClient websocketClient, TestTarget testTarget, byte payload[], int testRunId ) throws WebsocketException {
        String conversationUUID = UUID.randomUUID().toString();
        environment.setCurrentConversationId(conversationUUID);
        byte payloadSample[] = payload;
        if ( payloadSample.length > 100 ) {
            System.arraycopy(payload,0,payloadSample,0,100);
        }

        LOGGER.info(String.format("Testing target step %d %d-%d with payload: [%s]", testTarget.getTestableStepIdx(),testTarget.getStartPos(), testTarget.getEndPos(), GuiUtils.getBinPreviewStr(payloadSample)));
        int testableElementIdx = 0;
        boolean testInserted = false;
        boolean serverCloseRequested = false;
        for ( TestSequenceItem testSequenceItem : testRun.getTestSequence().getTestSequenceItems()) {
            if ( testSequenceItem.getTestSequenceItemType().equals(TestSequenceItemType.FRAME)) {
                TestUtil.delay(testSequenceItem.getDelayMsec());
                if ( testSequenceItem.getFrame().getDirection().equals(WebsocketDirection.OUTBOUND)) {
                    Color testHighlightColor = Color.WHITE;
                    WebsocketFrame curFrame = environment.process( EnvironmentItemScope.WEBSOCKET, testSequenceItem.getFrame().getCopy());
                    if ( testableElementIdx == testTarget.getTestableStepIdx()) {
                        byte[] testPayload = TestUtil.applyTestTarget(testTarget,environment.process(payload),curFrame);
                        curFrame.setPayloadUnmasked(testPayload);
                        testInserted = true;
                        testHighlightColor = testTarget.getHighlightColour();
                    }
                    websocketClient.send(curFrame);
                    // update traffic table
                    curFrame.setConversationUUID(conversationUUID);
                    curFrame.setUpgradeMessageUUID(websocketClient.getUpgradeRequest().getMessageUUID());
                    logger.logRFC6455Message(TrafficSource.AUTOMATED_TEST,curFrame,testRun.getTestName(),testRunId,testHighlightColor);
                    //this.projectModel.getPassiveAnomalyScanQueue().add(curFrame.getMessageUUID());
                    testableElementIdx += 1;
                }

            }
            /*
                We're reading frames and responding to server activities for the next sequenceItem.getDelayMsec() seconds
             */
            if ( testSequenceItem.getTestSequenceItemType().equals(TestSequenceItemType.IOWAIT)) {
                // Read frames
                long iowaitStart = System.currentTimeMillis();
                do {
                    ArrayList<WebsocketFrame> readFrames = websocketClient.recv();
                    ArrayList<WebsocketFrame> responses = new ArrayList<>();
                    if ( readFrames != null ) {
                        // Apply the env to them
                        for ( int j = 0 ; j < readFrames.size(); j++ ) {
                            readFrames.set(j,environment.process( EnvironmentItemScope.WEBSOCKET, readFrames.get(j).getCopy()));
                        }
                        for( WebsocketFrame readFrame : readFrames ) {
                            readFrame.setConversationUUID(conversationUUID);
                            readFrame.setUpgradeMessageUUID(websocketClient.getUpgradeRequest().getMessageUUID());
                            logger.logRFC6455Message(TrafficSource.AUTOMATED_TEST,readFrame,testRun.getTestName(),testRunId);
                            if ( readFrame.getOpcode().equals(WebsocketFrameType.CLOSE)) {
                                serverCloseRequested = true;
                                WebsocketFrame closeMsg = new WebsocketFrame();
                                closeMsg.setFin(1);
                                closeMsg.setDirection(WebsocketDirection.OUTBOUND);
                                closeMsg.setOpcode(WebsocketFrameType.CLOSE);
                                closeMsg.setMasked(1);
                                closeMsg.setMaskKey(closeMsg.generateMaskBytes());
                                closeMsg.setPayloadUnmasked(readFrame.getPayloadUnmasked());
                                websocketClient.send(closeMsg);
                                // update traffic table
                                closeMsg.setConversationUUID(conversationUUID);
                                closeMsg.setUpgradeMessageUUID(websocketClient.getUpgradeRequest().getMessageUUID());
                                logger.logRFC6455Message(TrafficSource.AUTOMATED_TEST,closeMsg,testRun.getTestName(),testRunId,Color.WHITE);
                                break;
                            }
                            if ( readFrame.getOpcode().equals(WebsocketFrameType.PING)) {
                                WebsocketFrame pongMsg = new WebsocketFrame();
                                pongMsg.setFin(1);
                                pongMsg.setDirection(WebsocketDirection.OUTBOUND);
                                pongMsg.setOpcode(WebsocketFrameType.PONG);
                                pongMsg.setMasked(1);
                                pongMsg.setMaskKey(pongMsg.generateMaskBytes());
                                pongMsg.setPayloadUnmasked(readFrame.getPayloadUnmasked());
                                responses.add(pongMsg);
                            }
                        }
                        if ( serverCloseRequested ) {
                            break;
                        }
                        if ( responses.size() > 0 ) {
                            for ( WebsocketFrame response : responses ) {
                                websocketClient.send(response);
                                response.setConversationUUID(conversationUUID);
                                response.setUpgradeMessageUUID(websocketClient.getUpgradeRequest().getMessageUUID());
                                logger.logRFC6455Message(TrafficSource.AUTOMATED_TEST,response,testRun.getTestName(),testRunId);
                                //this.projectModel.getPassiveAnomalyScanQueue().add(response.getMessageUUID());
                            }
                        }
                    }
                } while ( System.currentTimeMillis()-iowaitStart<testSequenceItem.getDelayMsec() && !shutdownRequested);
            }
            if ( testInserted ) {
                if ( !testRun.isContinueReplayAfterTestInsertion() ) {
                    break;
                }
            }
        }
        if ( conversationUUID != null ) {
            this.projectModel.getPassiveAnomalyScanQueue().add(new AnomalyScanRequest(conversationUUID));
        }
        environment.setCurrentConversationId(null);
        return conversationUUID;
    }
}
