package com.wsproxy.httpproxy.websocket.extensions;

import com.wsproxy.httpproxy.HttpMessage;
import com.wsproxy.httpproxy.HttpMessagePart;
import com.wsproxy.httpproxy.websocket.WebsocketFrame;

public interface WebsocketExtension {
    public void init(HttpMessage req, HttpMessage response );
    public WebsocketFrame processWebsocketFrameIn(WebsocketFrame frame );
    public WebsocketFrame processWebsocketFrameOut(WebsocketFrame frame );
    public WebsocketFrame processWebsocketFrame(WebsocketFrame frame );
}
