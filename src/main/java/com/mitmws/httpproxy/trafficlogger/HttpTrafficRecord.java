package com.mitmws.httpproxy.trafficlogger;

import com.mitmws.httpproxy.HttpMessage;

import java.io.Serializable;

public class HttpTrafficRecord implements Serializable  {
    private int id = -1;
    private HttpMessage request = null;
    private HttpMessage response = null;

    public HttpTrafficRecord(HttpMessage request, HttpMessage response) {
        this.request = request;
        this.response = response;
    }

    public HttpTrafficRecord(int id, HttpMessage request, HttpMessage response) {
        this.id = id;
        this.request = request;
        this.response = response;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public HttpMessage getRequest() {
        return request;
    }

    public void setRequest(HttpMessage request) {
        this.request = request;
    }

    public HttpMessage getResponse() {
        return response;
    }

    public void setResponse(HttpMessage response) {
        this.response = response;
    }
}
