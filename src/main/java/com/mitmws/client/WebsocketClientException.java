package com.mitmws.client;
class WebsocketClientException extends Exception {
    public WebsocketClientException(String errorMessage) {
        super(errorMessage);
    }
}