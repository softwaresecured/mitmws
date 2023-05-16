package com.mitmws.httpserver;

import com.mitmws.httpproxy.HttpMessage;
import com.mitmws.httpproxy.HttpMessageParseException;

import java.util.ArrayList;

public class CustomHttpResponse {
    private int statusCode = 200;
    private String statusMessage = null;
    private ArrayList<String> headers = new ArrayList<String>();
    private String body = null;
    private String contentType = null;
    public CustomHttpResponse() {

    }

    public CustomHttpResponse(int statusCode, String statusMessage, String body, String contentType) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.body = body;
        this.contentType = contentType;
    }

    public void addHeader( String header ) {
        headers.add(header);
    }

    public void setHeaders( String headersArr[] ) {
        headers = new ArrayList<String>();
        for ( int i = 0; i < headersArr.length; i++ ) {
            headers.add(headersArr[i]);
        }
    }

    public String[] getHeaders() {
        String headerArr[] = new String[headers.size()];
        for ( int i = 0; i < headerArr.length; i++ ) {
            headerArr[i] = headers.get(i);
        }
        return headerArr;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public HttpMessage toHttpMessage() throws HttpMessageParseException {
        HttpMessage response = null;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("HTTP/1.1 %d", statusCode));
        if ( statusMessage != null ) {
            sb.append(String.format(" %s", statusMessage));
        }
        sb.append("\r\n");
        for ( String header : headers ) {
            sb.append(String.format("%s\r\n", header));
        }
        sb.append(String.format("Content-type: %s\r\n", contentType));
        if ( body != null ) {
            sb.append(String.format("Content-length: %d\r\n", body.length()));
        }
        sb.append("\r\n");
        if ( body != null ) {
            sb.append(body);
        }
        response = new HttpMessage();
        response.fromBytes(sb.toString().getBytes());
        return response;
    }
}
