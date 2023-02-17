package com.wsproxy.httpproxy;
public class HttpMessageParseException extends Exception {
    public HttpMessageParseException(String errorMessage) {
        super(errorMessage);
    }
}