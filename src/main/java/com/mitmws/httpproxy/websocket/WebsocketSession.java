package com.mitmws.httpproxy.websocket;

import com.mitmws.conversations.WebsocketConversation;
import com.mitmws.environment.Environment;
import com.mitmws.environment.EnvironmentItemScope;
import com.mitmws.httpproxy.trafficlogger.TrafficLogger;
import com.mitmws.httpproxy.trafficlogger.TrafficSource;
import com.mitmws.httpproxy.trafficlogger.WebsocketDirection;
import com.mitmws.httpproxy.websocket.extensions.WebsocketExtension;
import com.mitmws.mvc.model.BreakpointModel;
import com.mitmws.util.ExtensionUtil;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class WebsocketSession {
    private final int WEBSOCKET_READ_CHECK_WAIT_TIME_MS=10;
    private Socket clientSocket = null;
    private Socket serverSocket = null;
    private TrafficLogger trafficLogger = null;
    private String upgradeMsgUUID = null;
    private boolean disableLog = false;
    private boolean paused = false;
    private TrafficSource trafficSource = TrafficSource.PROXY;
    private ArrayBlockingQueue immediateInjectQueue = new ArrayBlockingQueue(100);
    private Environment environment = new Environment(true); // why?
    private String testName = null;
    private ArrayList<WebsocketExtension> extensions;
    private BreakpointModel breakpointModel;
    private String upgradeUrl = null;

    private ArrayList<String> trappedFrameIds = new ArrayList<String>();

    public WebsocketSession ( ArrayList<WebsocketExtension> extensions, String upgradeMsgUUID, Socket client, Socket server, TrafficLogger trafficLogger, BreakpointModel breakpointModel, String upgradeUrl ) {
        this.trafficLogger = trafficLogger;
        clientSocket = client;
        serverSocket = server;
        this.upgradeMsgUUID = upgradeMsgUUID;
        this.extensions = extensions;
        this.breakpointModel = breakpointModel;
        this.upgradeUrl = upgradeUrl;
    }



    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    public void terminate() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processTrapedFrames( Socket clientSocket, Socket serverSocket ) throws WebsocketException {
        removeDroppedFrames();
        for ( String id : trappedFrameIds ) {
            WebsocketFrame releasedFrame = breakpointModel.frameRelease(id);
            if ( releasedFrame != null ) {
                if ( releasedFrame.getDirection().equals(WebsocketDirection.INBOUND)) {
                    WebsocketConversation.writeFrame(clientSocket,releasedFrame);
                }
                else {
                    WebsocketConversation.writeFrame(serverSocket,releasedFrame);
                }
            }
        }
    }

    // Wrapper for read / write that includes breakpoint check
    private void writeFrame ( Socket s, WebsocketFrame frame) throws WebsocketException {
        if ( breakpointModel.checkBreakpoints(frame, upgradeUrl) ) {
            frame.setUpgradeUrl(upgradeUrl);
            breakpointModel.frameTrap(frame);
            trappedFrameIds.add(frame.getMessageUUID());
        }
        else {
            WebsocketConversation.writeFrame(s,frame);
        }
    }

    private ArrayList<WebsocketFrame> getReleasedFrames() {
        ArrayList<WebsocketFrame> frames = new ArrayList<WebsocketFrame>();
        for ( String frameId : trappedFrameIds ) {
            WebsocketFrame releasedFrame = breakpointModel.frameRelease(frameId);
            if ( releasedFrame != null ) {
                frames.add(releasedFrame);
            }
        }
        return frames;
    }

    private void removeDroppedFrames() {
        for ( String id : trappedFrameIds ) {
            breakpointModel.dropFrame(id);
        }
    }

    private WebsocketFrame readFrame ( Socket s) throws WebsocketException {
        return WebsocketConversation.readFrame(s);
    }

    public void enableImmediate() {
        trafficSource = TrafficSource.IMMEDIATE;
    }

    public void injectImmediateFrame( ArrayList<WebsocketFrame> frames ) {
        for ( WebsocketFrame frame : frames ) {
            immediateInjectQueue.add(frame);
        }
    }

    public String getSessionName() {
        return String.format("%s:%d - %s:%d",
                clientSocket.getLocalAddress().toString().split("/")[1],
                clientSocket.getPort(),
                serverSocket.getInetAddress().toString().split("/")[1],
                serverSocket.getPort());
    }

    public String getUpgradeMsgUUID() {
        return upgradeMsgUUID;
    }

    public void disableLogging() {
        disableLog = true;
    }

    public void startSession() throws SocketException, WebsocketException {
        clientSocket.setSoTimeout(WEBSOCKET_READ_CHECK_WAIT_TIME_MS);
        serverSocket.setSoTimeout(WEBSOCKET_READ_CHECK_WAIT_TIME_MS);
        try {
            int closeFrames = 0;
            while ( closeFrames < 2 ) {
                processTrapedFrames(clientSocket,serverSocket);

                WebsocketFrame inboundFrame = null;
                WebsocketFrame outboundFrame = null;
                WebsocketFrame injectedFrame = (WebsocketFrame) immediateInjectQueue.poll();
                if ( injectedFrame != null ) {
                    if ( injectedFrame.getDirection().equals(WebsocketDirection.INBOUND)) {
                        injectedFrame = ExtensionUtil.processExtensions(extensions,injectedFrame,WebsocketDirection.INBOUND);
                        writeFrame(clientSocket,environment.process(EnvironmentItemScope.WEBSOCKET, injectedFrame));
                    }
                    else {
                        injectedFrame = ExtensionUtil.processExtensions(extensions,injectedFrame,WebsocketDirection.OUTBOUND);
                        writeFrame(serverSocket,environment.process( EnvironmentItemScope.WEBSOCKET, injectedFrame));
                    }
                }

                // Read from the server
                inboundFrame = readFrame(serverSocket);
                // Send to the client
                if ( inboundFrame != null ) {
                    inboundFrame.setUpgradeMessageUUID(upgradeMsgUUID);
                    inboundFrame.setDirection(WebsocketDirection.INBOUND);
                    inboundFrame = ExtensionUtil.processExtensions(extensions,inboundFrame,WebsocketDirection.INBOUND);
                    writeFrame(clientSocket,environment.process( EnvironmentItemScope.WEBSOCKET, inboundFrame));
                }


                // Read from the client
                outboundFrame = readFrame(clientSocket);
                // Send to the server
                if ( outboundFrame != null ) {
                    outboundFrame.setUpgradeMessageUUID(upgradeMsgUUID);
                    outboundFrame.setDirection(WebsocketDirection.OUTBOUND);
                    outboundFrame = ExtensionUtil.processExtensions(extensions,outboundFrame,WebsocketDirection.OUTBOUND);
                    writeFrame(serverSocket,environment.process( EnvironmentItemScope.WEBSOCKET, outboundFrame));
                }

                // log
                if ( !disableLog ) {
                    if ( injectedFrame != null ) {
                        trafficLogger.logRFC6455Message(trafficSource,injectedFrame,testName);
                        if ( injectedFrame.getOpcode().equals(WebsocketFrameType.CLOSE)) {
                            closeFrames += 1;
                        }
                    }
                    if ( inboundFrame != null ) {
                        trafficLogger.logRFC6455Message(trafficSource,inboundFrame,testName);
                        if ( inboundFrame.getOpcode().equals(WebsocketFrameType.CLOSE)) {
                            closeFrames += 1;
                        }
                    }

                    if ( outboundFrame != null ) {
                        trafficLogger.logRFC6455Message(trafficSource,outboundFrame,testName);
                        if ( outboundFrame.getOpcode().equals(WebsocketFrameType.CLOSE)) {
                            closeFrames += 1;
                        }
                    }
                }
                Thread.sleep(WEBSOCKET_READ_CHECK_WAIT_TIME_MS);
            }
        } catch ( InterruptedException e) {
            e.printStackTrace();
        }
    }
}
