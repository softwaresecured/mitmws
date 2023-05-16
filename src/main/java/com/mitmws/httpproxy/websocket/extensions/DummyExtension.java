package com.mitmws.httpproxy.websocket.extensions;

import com.mitmws.httpproxy.HttpMessage;
import com.mitmws.httpproxy.websocket.WebsocketFrame;

public class DummyExtension implements WebsocketExtension {
    private boolean enabled = false;
    public DummyExtension() {

    }

    @Override
    public void init(HttpMessage req, HttpMessage response) {
        String extHeaderValue = response.getHeaderValue("sec-websocket-extensions");
        if ( extHeaderValue != null ) {
            if ( extHeaderValue.matches(".*dummytest.*")) {
                enabled = true;
            }
        }
    }

    @Override
    public WebsocketFrame processWebsocketFrameIn(WebsocketFrame frame) {
        return frame;
    }

    @Override
    public WebsocketFrame processWebsocketFrameOut(WebsocketFrame frame) {
        return frame;
    }

    @Override
    public WebsocketFrame processWebsocketFrame(WebsocketFrame frame) {
        return frame;
    }
}
