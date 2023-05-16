package com.mitmws.httpproxy.websocket.extensions;

import com.mitmws.httpproxy.HttpMessage;
import com.mitmws.httpproxy.websocket.WebsocketFrame;

public interface WebsocketExtension {
    public void init(HttpMessage req, HttpMessage response );
    public WebsocketFrame processWebsocketFrameIn(WebsocketFrame frame );
    public WebsocketFrame processWebsocketFrameOut(WebsocketFrame frame );
    public WebsocketFrame processWebsocketFrame(WebsocketFrame frame );
}
