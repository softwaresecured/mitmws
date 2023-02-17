package com.wsproxy.tester;

import com.wsproxy.httpproxy.HttpMessage;
import com.wsproxy.httpproxy.websocket.WebsocketFrame;
import com.wsproxy.integrations.python.Script;
import com.wsproxy.integrations.python.ScriptManager;

import javax.script.ScriptException;

public class TestSession {

    // Session variables
    private long startTime = System.currentTimeMillis();
    private int port;
    private String host;
    private HttpMessage upgradeRequest = null;
    private HttpMessage upgradeResponse = null;
    private String conversationId;
    private Script eventScript = null;
    private WebsocketFrame lastFrame = null;
    public TestSession( String eventScriptFileName ) throws ScriptException {
        if ( eventScriptFileName != null ) {
            ScriptManager scriptManager = new ScriptManager();
            eventScript = scriptManager.getScript("events",eventScriptFileName);
            eventScript.executeFunction("init",this);
        }
    }

    public void eventNotify(TestSessionEventType testSessionEventType) {
        if ( eventScript != null ) {
            try {
                eventScript.executeFunction("process_event",testSessionEventType.toString(),this);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }

    }

    public long getStartTime() {
        return startTime;
    }

    public WebsocketFrame getLastFrame() {
        return lastFrame;
    }

    public void setLastFrame(WebsocketFrame lastFrame) {
        this.lastFrame = lastFrame;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public HttpMessage getUpgradeResponse() {
        return upgradeResponse;
    }

    public void setUpgradeResponse(HttpMessage upgradeResponse) {
        this.upgradeResponse = upgradeResponse;
    }

    public HttpMessage getUpgradeRequest() {
        return upgradeRequest;
    }

    public void setUpgradeRequest(HttpMessage upgradeRequest) {
        this.upgradeRequest = upgradeRequest;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
