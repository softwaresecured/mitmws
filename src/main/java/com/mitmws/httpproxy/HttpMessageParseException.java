package com.mitmws.httpproxy;
public class HttpMessageParseException extends Exception {
    public HttpMessageParseException(String errorMessage) {
        super(errorMessage);
    }
}