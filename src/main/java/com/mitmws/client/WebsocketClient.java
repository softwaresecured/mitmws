package com.mitmws.client;

import com.mitmws.configuration.ApplicationConfig;
import com.mitmws.conversations.WebsocketConversation;
import com.mitmws.httpproxy.*;
import com.mitmws.httpproxy.trafficlogger.WebsocketDirection;
import com.mitmws.httpproxy.websocket.WebsocketException;
import com.mitmws.httpproxy.websocket.WebsocketFrame;
import com.mitmws.httpproxy.websocket.extensions.WebsocketExtension;
import com.mitmws.logging.AppLog;
import com.mitmws.tester.RawWebsocketFrame;
import com.mitmws.util.ExtensionUtil;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.logging.Logger;


public class WebsocketClient {
    private Logger LOGGER = AppLog.getLogger(HttpClient.class.getName());
    private ApplicationConfig applicationConfig = new ApplicationConfig();
    private HttpClient httpClient = new HttpClient();
    private boolean websocketConnectionEstablished = false;
    private HttpMessage upgradeRequest = null;
    private ArrayList<WebsocketExtension> extensions = null;
    public WebsocketClient() {
    }

    public HttpMessage handshake (HttpMessage upgradeMessage) throws WebsocketException {
        disconenct();
        httpClient = new HttpClient();
        HttpMessage response = null;
        try {
            response = httpClient.send(upgradeMessage);
            if ( response != null ) {
                if  ( response.getStatusCode() == 101 ) {
                    websocketConnectionEstablished = true;
                    extensions = ExtensionUtil.initExtensions(upgradeRequest,response);
                    this.upgradeRequest = upgradeMessage;
                    httpClient.getSocket().setSoTimeout(100);
                }
            }
        } catch (HttpClientException e) {
            LOGGER.severe(String.format("Error connecting to HTTP server: %s", e.getMessage()));
            throw new WebsocketException(e.getMessage());
        } catch (SocketException e) {
            LOGGER.severe(String.format("Error connecting to HTTP server: %s", e.getMessage()));
            throw new WebsocketException(e.getMessage());
        }
        return response;
    }

    public boolean isConnected() {
        return httpClient.isConnected() && websocketConnectionEstablished;
    }
    public void disconenct() {
        if ( httpClient.getSocket() != null ) {
            httpClient.disconenct();
        }
        websocketConnectionEstablished = false;
    }

    public void send (WebsocketFrame frame ) throws WebsocketException {
        frame.setDirection(WebsocketDirection.OUTBOUND);
        frame.setUpgradeMessageUUID(getUpgradeRequest().getMessageUUID());
        frame = ExtensionUtil.processExtensions(extensions,frame,WebsocketDirection.OUTBOUND);
        WebsocketConversation.writeFrame(httpClient.getSocket(),frame);
    }

    public void sendRaw (RawWebsocketFrame frame ) throws WebsocketException {
        WebsocketConversation.writeRawFrame(httpClient.getSocket(),frame);
    }

    public ArrayList<WebsocketFrame> recv() throws WebsocketException {
        ArrayList<WebsocketFrame> frames = null;
        try {
            WebsocketFrame frame = null;
            do {
                frame = WebsocketConversation.readFrame(httpClient.getSocket());
                if ( frame != null ) {
                    if ( frames == null ) {
                        frames = new ArrayList<>();
                    }
                    frame.setUpgradeMessageUUID(getUpgradeRequest().getMessageUUID());
                    frame.setDirection(WebsocketDirection.INBOUND);
                    frame = ExtensionUtil.processExtensions(extensions,frame,WebsocketDirection.INBOUND);
                    frames.add(frame);
                }
            } while ( frame != null && httpClient.getSocket().getInputStream().available() > 0 );
        }
        catch ( IOException e ) {
            throw new WebsocketException(String.format("Exception while reading: %s", e.getMessage()));
        }
        return frames;
    }

    public HttpMessage getUpgradeRequest() {
        return upgradeRequest;
    }
}
