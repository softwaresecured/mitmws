package com.wsproxy.util;

import com.wsproxy.httpproxy.HttpMessage;
import com.wsproxy.httpproxy.HttpMessageParseException;
import com.wsproxy.httpproxy.HttpMessagePart;

public final class HttpMessageUtil {
    public static String getRequestResponseString(HttpMessage req, HttpMessage res ) {
        String reqResp = null;
        if ( req != null && res != null ) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%s\n", new String(req.getHeaderBytes())));
            if ( req.getBodyBytes() != null ) {
                sb.append(new String(req.getBodyBytes()));
            }
            String reqStr = sb.toString();
            String resStr = new String(res.getBytes());
            reqResp = String.format("%s%s", reqStr,resStr);
        }
        return reqResp;
    }
    public static HttpMessage buildResponse(int status, String message, String body ) {
        HttpMessage httpMessage = new HttpMessage();
        if ( body == null ) {
            body = "";
        }
        try {
            httpMessage.fromBytes(String.format("HTTP/1.1 %d %s \r\nContent-length: %d\r\nConnection: close\r\n\r\n%s", status, message,body.length(), body).getBytes());
        } catch (HttpMessageParseException e) {
            e.printStackTrace();
        }
        return httpMessage;
    }
}
