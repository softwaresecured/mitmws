package com.wsproxy.mvc.thread;
import com.wsproxy.client.WebsocketClient;
import com.wsproxy.environment.Environment;
import com.wsproxy.environment.EnvironmentItemScope;
import com.wsproxy.httpproxy.*;
import com.wsproxy.httpproxy.trafficlogger.TrafficLogger;
import com.wsproxy.httpproxy.trafficlogger.TrafficSource;
import com.wsproxy.httpproxy.websocket.WebsocketException;
import com.wsproxy.httpproxy.websocket.WebsocketFrame;
import com.wsproxy.httpproxy.websocket.WebsocketFrameType;
import com.wsproxy.httpproxy.websocket.WebsocketSession;
import com.wsproxy.httpserver.CustomWebsocketFrame;
import com.wsproxy.integrations.python.Script;
import com.wsproxy.integrations.python.ScriptManager;
import com.wsproxy.logging.AppLog;
import com.wsproxy.mvc.model.ImmediateModel;
import com.wsproxy.mvc.model.ProjectModel;
import com.wsproxy.projects.ProjectDataServiceException;
import com.wsproxy.util.HttpMessageUtil;
import com.wsproxy.util.WebsocketUtil;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class ImmediateThread extends Thread {
    private Logger LOGGER = AppLog.getLogger(ImmediateThread.class.getName());
    boolean shutdownRequested = false;
    private ImmediateModel immediateModel;
    private ScriptManager scriptManager = new ScriptManager();
    private String testName;
    private TrafficLogger logger;
    private ProjectModel projectModel;
    private WebsocketSession session = null;

    private ArrayBlockingQueue sendQueue = new ArrayBlockingQueue(100);
    public ImmediateThread(ImmediateModel immediateModel, ProjectModel projectModel, TrafficLogger logger, String testName ) {
        this.projectModel = projectModel;
        this.testName = testName;
        this.logger = logger;
        this.immediateModel = immediateModel;
        this.session = session;
    }

    public void shutdown() {
        shutdownRequested = true;
        System.out.println("STOPPING IMMEDIATE THREAD");
    }

    private HttpMessage getUpgradeHttpMessage() throws ScriptException {
        HttpMessage msg = null;
        if ( immediateModel.getRequestResponseModel().isUseUpgradeScript() ) {
            if ( immediateModel.getRequestResponseModel().getUpgradeScriptName() != null ) {
                Script upgradeScript = scriptManager.getScript("upgrade", immediateModel.getRequestResponseModel().getUpgradeScriptName());
                String upgradeRequestStr = (String) upgradeScript.executeFunction("execute");
                if ( upgradeRequestStr != null ) {
                    msg = new HttpMessage();
                    try {
                        msg.fromBytes(upgradeRequestStr.getBytes());
                    } catch ( IllegalArgumentException | HttpMessageParseException e) {
                        throw new ScriptException("Could not parse HTTP request created by helper script");
                    }
                }
            }
        }
        else {
            msg = immediateModel.getRequestResponseModel().buildHttpMessage();
        }
        return msg;
    }

    public void enqueueFrame ( WebsocketFrame frame ) {
        sendQueue.add(frame);
    }
    public void logFrames( ArrayList<WebsocketFrame> frames, int testRunId ) {
        for ( WebsocketFrame frame : frames ) {
            logger.logRFC6455Message(TrafficSource.IMMEDIATE, frame, testName, testRunId);
        }
    }
    public void run() {
        if ( immediateModel.getWebsocketSession() == null ) {
            // Starts a new immediate session becuase there is no existing session
            runImmediate();
        }
        else {
            // Takes over an existing session
            runDropIn();
        }
        LOGGER.info("Stopped");
    }

    public void runDropIn() {
        LOGGER.info("Starting drop-in session");
        while ( !shutdownRequested && immediateModel.getWebsocketSession() != null ) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        immediateModel.getWebsocketSession().terminate();
    }
    public void runImmediate() {
        LOGGER.info("Starting immediate session");
        Environment environment = new Environment();
        environment.loadEnvironment();
        HttpMessage upgradeMsg = null;
        boolean closeRequested = false;
        try {
            upgradeMsg = getUpgradeHttpMessage();
            int testRunId = projectModel.getProjectDataService().getTestRunIdByName(testName);
            if ( upgradeMsg != null ) {
                WebsocketClient websocketClient = new WebsocketClient();
                HttpMessage processedMessage = environment.process(EnvironmentItemScope.HTTP,upgradeMsg);
                HttpMessage upgradeResponse = websocketClient.handshake(processedMessage);
                immediateModel.getRequestResponseModel().setResponse(new String(upgradeResponse.getBytes()));
                immediateModel.getRequestResponseModel().setRequestResponse(
                        HttpMessageUtil.getRequestResponseString(processedMessage, upgradeResponse)
                );

                while ( websocketClient.isConnected() && !shutdownRequested && !closeRequested ) {
                    // read from server
                    ArrayList<WebsocketFrame> outboundFrames = new ArrayList<WebsocketFrame>();
                    ArrayList<WebsocketFrame> inboundFrames = new ArrayList<WebsocketFrame>();
                    ArrayList<WebsocketFrame> curInboundFrames = null;
                    do {
                        curInboundFrames = websocketClient.recv();
                        if ( curInboundFrames != null ) {
                            for ( WebsocketFrame frame : curInboundFrames) {
                                inboundFrames.add(environment.process( EnvironmentItemScope.WEBSOCKET, frame));
                                if ( frame.getOpcode().equals(WebsocketFrameType.CLOSE)) {
                                    WebsocketFrame closeFrame = WebsocketUtil.customWebsocketFrameToWebsocketFrame(new CustomWebsocketFrame(1, 0,0,0,"PING", null));
                                    outboundFrames.add(environment.process( EnvironmentItemScope.WEBSOCKET, closeFrame));
                                    websocketClient.send(closeFrame);
                                    logger.logRFC6455Message(TrafficSource.IMMEDIATE, closeFrame, testName, testRunId);
                                    closeRequested = true;
                                }
                            }
                        }
                    } while ( curInboundFrames != null && !closeRequested );
                    logFrames(inboundFrames,testRunId);
                    if ( !closeRequested ) {
                        // add the user's frames
                        WebsocketFrame userFrame;
                        do {
                            userFrame = (WebsocketFrame) sendQueue.poll();
                            if ( userFrame != null ) {
                                outboundFrames.add(environment.process(EnvironmentItemScope.WEBSOCKET, userFrame));
                            }
                        } while ( userFrame != null );


                        // process the inbound frames

                        // handle ping / pong if enabled
                        if ( immediateModel.isAutoPingPong() ) {
                            for ( WebsocketFrame frame : inboundFrames ) {
                                // respond to pings
                                if ( frame.getOpcode().equals(WebsocketFrameType.PING)) {
                                    WebsocketFrame pongFrame = WebsocketUtil.customWebsocketFrameToWebsocketFrame(new CustomWebsocketFrame(1, 0,0,0,"PONG", frame.getPayloadUnmasked()));
                                    outboundFrames.add(environment.process( EnvironmentItemScope.WEBSOCKET, pongFrame));
                                }
                            }
                        }

                        // send whatever we can
                        for ( WebsocketFrame frame : outboundFrames ) {
                            websocketClient.send(frame);
                        }
                        // log
                        logFrames(outboundFrames,testRunId);
                    }
                    Thread.sleep(100);
                }
                LOGGER.info(String.format("Immediate thread disconnecting"));
                websocketClient.disconenct();
            }
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (WebsocketException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ProjectDataServiceException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}