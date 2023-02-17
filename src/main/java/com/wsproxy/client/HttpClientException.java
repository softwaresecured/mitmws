package com.wsproxy.client;
public class HttpClientException extends Exception {
    public HttpClientException(String errorMessage) {
        super(errorMessage);
    }
}