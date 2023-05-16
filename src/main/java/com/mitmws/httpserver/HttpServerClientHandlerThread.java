package com.mitmws.httpserver;

import com.mitmws.configuration.ApplicationConfig;
import com.mitmws.conversations.HttpConversation;
import com.mitmws.conversations.WebsocketConversation;
import com.mitmws.httpproxy.*;
import com.mitmws.httpproxy.trafficlogger.WebsocketDirection;
import com.mitmws.httpproxy.websocket.WebsocketException;
import com.mitmws.httpproxy.websocket.WebsocketFrame;
import com.mitmws.httpproxy.websocket.WebsocketFrameType;
import com.mitmws.integrations.python.Script;
import com.mitmws.logging.AppLog;
import com.mitmws.util.HttpMessageUtil;
import com.mitmws.util.NetUtils;
import com.mitmws.util.WebsocketUtil;

import javax.script.ScriptException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class HttpServerClientHandlerThread extends Thread {
    private final int WEBSOCKET_IDLE_TIMEOUT_MS=10*1000;
    private final int WEBSOCKET_PINGPONG_INTERVAL_SEC=2;
    private final int WEBSOCKET_PINGPONG_RESPONSE_WAIT_SEC=10;
    private final int WEBSOCKET_READ_CHECK_WAIT_TIME_MS=10;
    private long lastPongTime = System.currentTimeMillis();
    private long lastActivityTime = System.currentTimeMillis();
    private long lastPingSent = -1;

    private Socket clientSocket;
    private ApplicationConfig applicationConfig = new ApplicationConfig();
    private HashMap<String, Script> routes;
    private boolean isWebsocketConversation = false;
    private static Logger LOGGER = AppLog.getLogger(HttpServerClientHandlerThread.class.getName());
    public HttpServerClientHandlerThread( Socket clientSocket, HashMap<String, Script> routes ) {
        this.routes = routes;
        this.clientSocket = clientSocket;
    }



    public HttpMessage buildScriptedHttpMessage( Script script, HttpMessage httpRequest ) throws ScriptException, HttpMessageParseException {
        String bodyStr = "";
        if ( httpRequest.getBodyBytes() != null ) {
            new String(httpRequest.getBodyBytes());
        }
        CustomHttpResponse customHttpResponse = (CustomHttpResponse) script.executeFunction("onHttpRequest",httpRequest.getHttpMethod(),httpRequest.getPath(),httpRequest.getHeaders(),bodyStr);
        HttpMessage httpMessage = customHttpResponse.toHttpMessage();
        return httpMessage;
    }

    public boolean isUpgradeRequest ( HttpMessage req ) {
        if ( req.getHeaderValue("connection") != null && req.getHeaderValue("upgrade") != null && req.getHeaderValue("sec-webSocket-key") != null  ) {
            return true;
        }
        return false;
    }

    public void startScriptedWebsocketSession( Socket clientsSocket, Script handlerScript ) {
        try {
            clientSocket.setSoTimeout(WEBSOCKET_READ_CHECK_WAIT_TIME_MS);
            LOGGER.info(String.format("[INTERNAL-WS:%s] Script based websocket session started", NetUtils.getFrieldyClientName(clientSocket)));
            ArrayList<CustomWebsocketFrame> inboundFrames;
            ArrayList<CustomWebsocketFrame> outboundFrames;
            boolean closeRequested = false;
            List<CustomWebsocketFrame> welcomeFrames = (List<CustomWebsocketFrame>) handlerScript.executeFunction("onWebsocketConnect");
            for( CustomWebsocketFrame welcomeFrame : welcomeFrames ) {
                WebsocketFrame frame = WebsocketUtil.customWebsocketFrameToWebsocketFrame(welcomeFrame);
                if ( frame != null ) {
                    frame.setDirection(WebsocketDirection.OUTBOUND);
                    WebsocketConversation.writeFrame(clientsSocket,frame);
                    clientsSocket.getOutputStream().flush();
                }
            }
            do {
                inboundFrames = new ArrayList<CustomWebsocketFrame>();
                outboundFrames = new ArrayList<CustomWebsocketFrame>();
                WebsocketFrame inboundFrame = null;
                // Read inbound frames
                if ( clientsSocket.getInputStream().available() > 0 ) {
                    do {
                        inboundFrame = WebsocketConversation.readFrame(clientsSocket);
                        if ( inboundFrame != null ) {
                            inboundFrames.add(WebsocketUtil.websocketFrameToCustomWebsocketFrame(inboundFrame));
                            if ( inboundFrame.getOpcode().equals(WebsocketFrameType.CLOSE)) {
                                closeRequested = true;
                                WebsocketFrame frame = WebsocketUtil.customWebsocketFrameToWebsocketFrame(new CustomWebsocketFrame(1, 0,0,0,"CLOSE", null));
                                if ( frame != null ) {
                                    frame.setDirection(WebsocketDirection.OUTBOUND);
                                    WebsocketConversation.writeFrame(clientsSocket,frame);
                                    clientsSocket.getOutputStream().flush();
                                }
                            }
                            lastActivityTime = System.currentTimeMillis();
                        }
                        if ( closeRequested ) {
                            break;
                        }
                    } while ( inboundFrame != null && clientsSocket.getInputStream().available() > 0);
                    if ( closeRequested ) {
                        break;
                    }
                }


                // Process inbound frames
                for ( CustomWebsocketFrame customWebsocketFrame : inboundFrames ) {
                    // respond to pings
                    if ( customWebsocketFrame.getOpcode().equals("PING")) {
                        outboundFrames.add(new CustomWebsocketFrame(1, 0,0,0,"PONG", customWebsocketFrame.getPayload()));
                    }
                    // accept our pongs
                    if ( customWebsocketFrame.getOpcode().equals("PONG")) {
                        lastPingSent = -1;
                        lastPongTime = System.currentTimeMillis();
                    }
                    // run the handler
                    List<CustomWebsocketFrame> responseFrames = (List<CustomWebsocketFrame>) handlerScript.executeFunction("onWebsocketMessage",customWebsocketFrame);
                    for ( CustomWebsocketFrame curResponse : responseFrames ) {
                        outboundFrames.add(curResponse);
                    }
                }

                // Insert a ping if it has been too long
                if ( System.currentTimeMillis()-lastPongTime > WEBSOCKET_PINGPONG_INTERVAL_SEC*1000 && lastPingSent < 0) {
                    outboundFrames.add(new CustomWebsocketFrame(1, 0,0,0,"PING", null));
                    lastPingSent = System.currentTimeMillis();
                }

                // Write responses
                for ( CustomWebsocketFrame outboundFrame : outboundFrames ) {
                    WebsocketFrame frame = WebsocketUtil.customWebsocketFrameToWebsocketFrame(outboundFrame);
                    if ( frame != null ) {
                        WebsocketConversation.writeFrame(clientsSocket,frame);
                    }
                }

                // Timeouts
                if ( System.currentTimeMillis()-lastActivityTime > WEBSOCKET_IDLE_TIMEOUT_MS ) {
                    LOGGER.info("TIMEOUT: WEBSOCKET_IDLE_TIMEOUT_MS");
                    break;
                }
                if ( lastPingSent > 0 ) {
                    if ( System.currentTimeMillis()-lastPingSent > WEBSOCKET_PINGPONG_RESPONSE_WAIT_SEC*1000 ) {
                        LOGGER.info("TIMEOUT: WEBSOCKET_PINGPONG_RESPONSE_WAIT_SEC");
                        break;
                    }
                }
                Thread.sleep(100);
            } while (clientsSocket.isConnected());
            LOGGER.info(String.format("[INTERNAL-WS:%s] Session complete", NetUtils.getFrieldyClientName(clientSocket)));
        } catch (WebsocketException e) {
            LOGGER.warning(String.format("[INTERNAL-WS:%s] Websocket exception: %s", NetUtils.getFrieldyClientName(clientSocket),e.getMessage()));
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCustomResponse(int httpCode, String httpStatus, String protocol, byte[] body) {
        HttpMessage customResponse = new HttpMessage();
        try {
            customResponse.responseFromParams(
                    httpCode,
                    httpStatus,
                    new String[] {
                            String.format("Date: %s",HttpUtil.getHttpDate())
                    },
                    body,
                    protocol,
                    false
            );
            HttpConversation.writeHttpMessage(clientSocket,customResponse,HttpMessagePart.ALL, HttpMessageFormat.DIRECT, applicationConfig);
        } catch (HttpMessageParseException | IOException e) {
            //e.printStackTrace();
        }
    }
    public void run() {
        try {
            HttpMessage httpRequest = HttpConversation.readHttpMessage(clientSocket, HttpMessagePart.ALL,Integer.parseInt(applicationConfig.getProperty("http.max_header_size")),applicationConfig);
            if ( httpRequest != null ) {
                HttpMessage httpResponse = HttpMessageUtil.buildResponse(404, "Not found",null);
                String path = httpRequest.getPath().trim().split("\\?")[0];
                if ( path != null ) {
                    if ( path.startsWith("/api")) {
                        ApiHandler apiHandler = new ApiHandler();
                        httpResponse = apiHandler.processApiRequest(httpRequest);
                        clientSocket.getOutputStream().flush();
                        HttpConversation.writeHttpMessage(clientSocket,httpResponse,HttpMessagePart.ALL, HttpMessageFormat.DIRECT, applicationConfig);
                    }
                    else {
                        System.out.println(String.format("Getting handler script for %s", path));
                        Script handlerScript = routes.get(path);
                        if (handlerScript != null) {
                            httpResponse = buildScriptedHttpMessage(handlerScript, httpRequest);
                            // Check if this is an upgrade request
                            if ( isUpgradeRequest(httpRequest)) {
                                String secWebsocketKey = httpRequest.getHeaderValue("sec-websocket-key");
                                String secWebsocketKeyAnswer = WebsocketUtil.getWebsocketKeyAnswer(secWebsocketKey);
                                CustomHttpResponse customHttpResponse = new CustomHttpResponse(
                                        101, "Switching Protocols", null, "text/html");
                                customHttpResponse.addHeader("Upgrade: websocket");
                                customHttpResponse.addHeader("Connection: Upgrade");
                                customHttpResponse.addHeader(String.format("Sec-WebSocket-Accept: %s", secWebsocketKeyAnswer));
                                httpResponse = customHttpResponse.toHttpMessage();
                                isWebsocketConversation = true;
                            }
                        }
                        LOGGER.info(String.format("INTERNAL-WEB-%d%s %d %s %s", getId(), clientSocket.getRemoteSocketAddress().toString(),httpResponse.getStatusCode(),httpRequest.getHttpMethod(),httpRequest.getPath()));

                        clientSocket.getOutputStream().flush();
                        HttpConversation.writeHttpMessage(clientSocket,httpResponse,HttpMessagePart.ALL, HttpMessageFormat.DIRECT, applicationConfig);
                        if ( isWebsocketConversation ) {
                            startScriptedWebsocketSession(clientSocket,handlerScript);
                        }
                    }
                }

                if ( clientSocket != null ) {
                    clientSocket.close();
                }
            }
            // The request was malformed or could not be parsed
            else {
                sendCustomResponse(400,"Bad request","1.1","Could not read request".getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (HttpMessageParseException e) {
            sendCustomResponse(400,"Bad request","1.1","Bad request".getBytes(StandardCharsets.UTF_8));
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }
}
