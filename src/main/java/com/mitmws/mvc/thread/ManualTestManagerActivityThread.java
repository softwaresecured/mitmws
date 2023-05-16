package com.mitmws.mvc.thread;

import com.mitmws.anomalydetection.AnomalyScanRequest;
import com.mitmws.client.WebsocketClient;
import com.mitmws.environment.Environment;
import com.mitmws.environment.EnvironmentItemScope;
import com.mitmws.httpproxy.HttpMessage;
import com.mitmws.httpproxy.websocket.WebsocketException;
import com.mitmws.httpproxy.websocket.WebsocketFrame;
import com.mitmws.httpproxy.websocket.WebsocketFrameType;
import com.mitmws.httpproxy.trafficlogger.TrafficLogger;
import com.mitmws.httpproxy.trafficlogger.TrafficRecord;
import com.mitmws.httpproxy.trafficlogger.TrafficSource;
import com.mitmws.httpproxy.trafficlogger.WebsocketDirection;
import com.mitmws.logging.AppLog;
import com.mitmws.mvc.model.*;
import com.mitmws.projects.ProjectDataServiceException;
import com.mitmws.tester.*;
import com.mitmws.util.TestUtil;

import javax.script.ScriptException;
import java.awt.*;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;
/*
    Manages a manual test run
 */
public class ManualTestManagerActivityThread extends Thread {
    private static Logger LOGGER = AppLog.getLogger(ManualTestManagerActivityThread.class.getName());
    private boolean tls = false;
    private DateFormat trafficTimeFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public Environment environment = null;
    private ArrayList<TrafficRecord> replayResults = null;
    private TestSequence sequence = null;
    private String testName = null;
    private boolean shutdownRequested = false;
    private TrafficLogger logger = null;
    private ProjectModel projectModel;
    private ManualTestExecutionModel manualTestExecutionModel;
    private ManualTesterModel manualTesterModel;
    private TestSession testSession;
    public ManualTestManagerActivityThread(MainModel mainModel, TrafficLogger logger, TestSequence sequence, String testName, boolean tls ) {
        this.manualTesterModel = mainModel.getManualTesterModel();
        this.manualTestExecutionModel = manualTesterModel.getManualTestExecutionModel();
        this.projectModel = mainModel.getProjectModel();
        this.logger = logger;
        this.tls = tls;
        environment = new Environment();
        environment.loadEnvironment();
        environment.setInteractshModel(mainModel.getInteractshModel());
        this.sequence = sequence;
        this.testName = testName;
        replayResults = new ArrayList<>();
        try {
            testSession = new TestSession(sequence.getEventScript());
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        shutdownRequested = true;
    }
    public ArrayList<TrafficRecord> getReplayResults() {
        return replayResults;
    }



    public void run() {
        environment.setCurrentConversationId(null);
        LOGGER.info(String.format("Running manual test sequence %s", testName));
        testSession.eventNotify(TestSessionEventType.TEST_STARTED);
        manualTesterModel.addTestLogMessage("INFO",testName,String.format("Running manual test sequence %s", testName));
        manualTestExecutionModel.setStatus("STARTED");
        long startTime = System.currentTimeMillis();
        WebsocketClient websocketClient = new WebsocketClient();
        String conversationUUID = UUID.randomUUID().toString();
        boolean serverCloseRequested = false;
        boolean clientCloseRequested = false;
        try {
            int testRunId = projectModel.getProjectDataService().getTestRunIdByName(testName);
            HttpMessage upgradeMsg = environment.process(EnvironmentItemScope.HTTP,sequence.getTestHttpMessage());
            testSession.setUpgradeRequest(upgradeMsg);
            manualTesterModel.addTestLogMessage("INFO",testName, String.format("Upgrading %s", upgradeMsg.getHttpUrl()));
            testSession.eventNotify(TestSessionEventType.BEFORE_CONNECT);
            HttpMessage upgradeResponse = websocketClient.handshake(upgradeMsg);
            manualTesterModel.getManualTestExecutionModel().setUpgradeRequest(upgradeMsg);
            manualTesterModel.getManualTestExecutionModel().setUpgradeResponse(upgradeResponse);
            if ( upgradeResponse != null ) {
                testSession.setUpgradeResponse(upgradeResponse);
                logger.logRFC2616Message(TrafficSource.MANUAL_TEST,upgradeMsg,upgradeResponse,testName,testRunId);
                environment.setCurrentConversationId(upgradeMsg.getMessageUUID());
                if ( upgradeResponse.getStatusCode() == 101 ) {
                    testSession.eventNotify(TestSessionEventType.AFTER_CONNECT);
                    manualTesterModel.addTestLogMessage("INFO",testName, String.format("Connected to %s", upgradeMsg.getHttpUrl().getHost()));
                    for ( int i = 0; i < sequence.getTestSequenceItems().size() && !shutdownRequested; i++ ) {
                        manualTestExecutionModel.setCurrentTestStep(i);
                        TestSequenceItem sequenceItem = sequence.getTestSequenceItems().get(i);
                        /*
                            We're sending a websocket frame
                         */
                        if ( sequenceItem.getTestSequenceItemType().equals(TestSequenceItemType.FRAME)) {
                            String payloadStr = "";
                            if ( sequenceItem.getFrame().getPayloadUnmasked() != null ) {
                                payloadStr = new String(sequenceItem.getFrame().getPayloadUnmasked());
                                if ( payloadStr.length() > 100 ) {
                                    payloadStr = payloadStr.substring(0,100);
                                }
                            }
                            LOGGER.info(String.format("Sending test frame [%s]", payloadStr != null ? payloadStr : "" ));
                            TestUtil.delay(sequenceItem.getDelayMsec());
                            if ( sequenceItem.getFrame().getDirection().equals(WebsocketDirection.OUTBOUND)) {
                                WebsocketFrame curFrame = sequenceItem.getFrame().getCopy();

                                if ( sequenceItem.getFrame().getOpcode().equals(WebsocketFrameType.CLOSE)) {
                                    clientCloseRequested = true;
                                }

                                if ( sequenceItem.getFrame().getOpcode().equals(WebsocketFrameType.TEXT)) {
                                    curFrame = environment.process(EnvironmentItemScope.WEBSOCKET, sequenceItem.getFrame().getCopy());
                                }
                                websocketClient.send(curFrame);
                                testSession.setLastFrame(curFrame);
                                testSession.eventNotify(TestSessionEventType.FRAME_SENT);
                                // update traffic table
                                curFrame.setUpgradeMessageUUID(upgradeMsg.getMessageUUID());
                                curFrame.setConversationUUID(conversationUUID);
                                logger.logRFC6455Message(TrafficSource.MANUAL_TEST,curFrame,testName,testRunId);
                                //GuiUtils.updateWebsocketConversationReplayTable(elements.getJtblReplayHistory(), sequenceItem, testName, curFrame);
                            }
                        }
                        /*
                            We're reading frames and responding to server activities seconds
                         */
                        if ( sequenceItem.getTestSequenceItemType().equals(TestSequenceItemType.IOWAIT)) {
                            LOGGER.info(String.format("IO wait %d ms", sequenceItem.getDelayMsec()));
                            // Read frames
                            long iowaitStart = System.currentTimeMillis();
                            manualTesterModel.addTestLogMessage("INFO",testName, String.format("Handling incoming IO for %d msec", sequenceItem.getDelayMsec()));
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
                                        readFrame.setUpgradeMessageUUID(upgradeMsg.getMessageUUID());
                                        logger.logRFC6455Message(TrafficSource.MANUAL_TEST,readFrame,testName,testRunId);
                                        testSession.setLastFrame(readFrame);
                                        testSession.eventNotify(TestSessionEventType.FRAME_RECEIVED);

                                        // Handle server requested close
                                        if ( readFrame.getOpcode().equals(WebsocketFrameType.CLOSE) && !serverCloseRequested && !clientCloseRequested) {
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
                                            logger.logRFC6455Message(TrafficSource.MANUAL_TEST,closeMsg,testName,testRunId, Color.WHITE);
                                            break;
                                        }

                                        //GuiUtils.updateWebsocketConversationReplayTable(elements.getJtblReplayHistory(), sequenceItem, testName, readFrame);
                                        // if we get a ping we send a pong
                                        if ( readFrame.getOpcode().equals(WebsocketFrameType.PING)) {
                                            WebsocketFrame pong = new WebsocketFrame();
                                            pong.setFin(1);
                                            pong.setDirection(WebsocketDirection.OUTBOUND);
                                            pong.setOpcode(WebsocketFrameType.PONG);
                                            pong.setMasked(1);
                                            pong.setMaskKey(pong.generateMaskBytes());
                                            pong.setPayloadUnmasked(readFrame.getPayloadUnmasked());
                                            responses.add(pong);
                                        }
                                    }
                                    manualTesterModel.addTestLogMessage("INFO",testName, String.format("Received %d frames", readFrames.size()));
                                    if ( serverCloseRequested ) {
                                        break;
                                    }
                                    // Send all the responses
                                    if ( responses.size() > 0 ) {
                                        for ( WebsocketFrame response : responses ) {
                                            websocketClient.send(response);
                                            response.setUpgradeMessageUUID(upgradeMsg.getMessageUUID());
                                            response.setConversationUUID(conversationUUID);
                                            logger.logRFC6455Message(TrafficSource.MANUAL_TEST,response,testName,testRunId);
                                        }
                                        manualTesterModel.addTestLogMessage("INFO",testName, String.format("Sent %d frames", readFrames.size()));
                                    }
                                }
                            } while ( System.currentTimeMillis()-iowaitStart<sequenceItem.getDelayMsec() && !shutdownRequested);
                            manualTesterModel.addTestLogMessage("INFO",testName, String.format("%d msec wait complete", sequenceItem.getDelayMsec()));
                            if ( serverCloseRequested ) {
                                break;
                            }
                        }
                    }
                    if ( shutdownRequested ) {
                        manualTesterModel.addTestLogMessage("INFO",testName, "Test aborted");
                    }
                }
                else {
                    manualTesterModel.addTestLogMessage("ERROR",testName, String.format("Unable to complete upgrade request to %s", upgradeMsg.getHttpUrl().getHost()));
                }
                this.projectModel.getPassiveAnomalyScanQueue().add(new AnomalyScanRequest(conversationUUID));
            }
        } catch (WebsocketException e) {
            manualTesterModel.addTestLogMessage("ERROR",testName, String.format("Error connecting to HTTP server: %s", e.getMessage()));
            LOGGER.severe("Websocket communication error while testing");
        } catch (ScriptException e) {
            manualTesterModel.addTestLogMessage("ERROR",testName, String.format("Error running helper script: %s", e.getMessage()));
            LOGGER.severe("Helper script error while testing");
        } catch (ProjectDataServiceException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            websocketClient.disconenct();
            testSession.eventNotify(TestSessionEventType.AFTER_DISCONNECT);
            manualTesterModel.addTestLogMessage("INFO",testName, String.format("Disconnected from %s", sequence.getHttpMessage().getHttpUrl().getHost()));
        }
        long testRuntime = (System.currentTimeMillis()-startTime)/1000;
        manualTesterModel.addTestLogMessage("INFO",testName, String.format("Test completed in %d sec", testRuntime));
        LOGGER.info(String.format("Test completed in %d sec", testRuntime));
    }
}
