package com.wsproxy.httpproxy.websocket;
class WebsocketFrameParseException extends Exception {
    public WebsocketFrameParseException(String errorMessage) {
        super(errorMessage);
    }
}