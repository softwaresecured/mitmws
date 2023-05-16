package com.mitmws.httpserver;
class HttpServerException extends Exception {
    public HttpServerException(String errorMessage) {
        super(errorMessage);
    }
}