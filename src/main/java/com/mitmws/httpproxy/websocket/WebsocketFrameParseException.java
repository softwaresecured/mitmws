package com.mitmws.httpproxy.websocket;
class WebsocketFrameParseException extends Exception {
    public WebsocketFrameParseException(String errorMessage) {
        super(errorMessage);
    }
}