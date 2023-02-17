package com.wsproxy.httpproxy;

import com.wsproxy.httpproxy.trafficlogger.WebsocketDirection;
import com.wsproxy.httpproxy.websocket.WebsocketFrame;
import com.wsproxy.httpproxy.websocket.WebsocketFrameType;

import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BreakPointItem {
    private String id = UUID.randomUUID().toString();
    private String name = "Untitled";
    private Pattern conversationScope = null;
    private Pattern payloadScope = null;
    private ArrayList<WebsocketFrameType> frameTypeScope = new ArrayList<WebsocketFrameType>();
    private WebsocketDirection websocketDirection = WebsocketDirection.BOTH;

    public BreakPointItem(Pattern conversationScope, Pattern payloadScope, WebsocketDirection websocketDirection) {
        this.conversationScope = conversationScope;
        this.payloadScope = payloadScope;
        this.websocketDirection = websocketDirection;
    }

    public void resetFrameTypeScope() {
        frameTypeScope = new ArrayList<WebsocketFrameType>();
    }
    public void addWebsocketFrameType( WebsocketFrameType websocketFrameType ) {
        if ( !frameTypeScope.contains(websocketFrameType )) {
            frameTypeScope.add(websocketFrameType);
        }
    }

    public void removeWebsocketFrameType( WebsocketFrameType websocketFrameType ) {
        if ( frameTypeScope.contains(websocketFrameType)) {
            frameTypeScope.remove(websocketFrameType);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Pattern getConversationScope() {
        return conversationScope;
    }

    public void setConversationScope(Pattern conversationScope) {
        this.conversationScope = conversationScope;
    }

    public Pattern getPayloadScope() {
        return payloadScope;
    }

    public void setPayloadScope(Pattern payloadScope) {
        this.payloadScope = payloadScope;
    }

    public ArrayList<WebsocketFrameType> getFrameTypeScope() {
        return frameTypeScope;
    }

    public void setFrameTypeScope(ArrayList<WebsocketFrameType> frameTypeScope) {
        this.frameTypeScope = frameTypeScope;
    }

    public WebsocketDirection getWebsocketDirection() {
        return websocketDirection;
    }

    public void setWebsocketDirection(WebsocketDirection websocketDirection) {
        this.websocketDirection = websocketDirection;
    }

    public boolean match(WebsocketFrame frame, String upgradeUrl ) {
        if ( frame.getDirection().equals(websocketDirection) || websocketDirection.equals(WebsocketDirection.BOTH)) {
            if ( frameTypeScope.contains(frame.getOpcode())) {
                if ( payloadScope != null ) {
                    Matcher m = payloadScope.matcher(frame.getPayloadString());
                    if ( m.find() ) {
                        return true;
                    }
                }
                if ( conversationScope != null ) {
                    Matcher m = conversationScope.matcher(upgradeUrl);
                    if ( m.find() ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getId() {
        return id;
    }
}
