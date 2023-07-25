package com.mitmws.analyzer.models;

import com.mitmws.httpproxy.trafficlogger.HttpTrafficRecord;
import com.mitmws.tester.TargetLocator;
import com.mitmws.tester.TestTarget;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class AnalyzerHttpMessageModel {
    private String id = UUID.randomUUID().toString();
    private URL url = null;
    private String baseUrl = null;
    private String path = null; // just the path, no query parameters
    private HashMap<String,AnalyzerParameterModel> queryParameterModel = new HashMap<String,AnalyzerParameterModel>();
    private HashMap<String,AnalyzerParameterModel> cookieParameterModel = new HashMap<String,AnalyzerParameterModel>();
    private HashMap<String,AnalyzerParameterModel> headerParameterModel = new HashMap<String,AnalyzerParameterModel>();
    private String tokenizedUrl = null;
    public AnalyzerHttpMessageModel(HttpTrafficRecord httpTrafficRecord ) throws AnalyzerException {
        analyzeMessage(httpTrafficRecord);
    }

    public String getId() {
        return id;
    }

    private void analyzeMessage(HttpTrafficRecord httpTrafficRecord) throws AnalyzerException {
        try {
            url = new URL(httpTrafficRecord.getRequest().getUrl());
            baseUrl = String.format("%s://%s%s", url.getProtocol(),url.getHost(),url.getPort() > 0 ? String.format(":%s", url.getPort()) : "");
            path = url.getPath();
        } catch (MalformedURLException e) {
            throw new AnalyzerException(String.format("Error parsing URL: %s", e.getMessage()));
        }
    }

    public void merge( AnalyzerHttpMessageModel model ) {
        // Query params
        for ( String parameter : getQueryParameterModel().keySet() ) {
            if ( getQueryParameterModel().get(parameter) == null ) {
                getQueryParameterModel().put(parameter,model.getQueryParameterModel().get(parameter));
            }
            else {
                getQueryParameterModel().get(parameter).merge(model.getQueryParameterModel().get(parameter));
            }
        }
        // Headers
        for ( String parameter : getHeaderParameterModel().keySet() ) {
            if ( getHeaderParameterModel().get(parameter) == null ) {
                getHeaderParameterModel().put(parameter,model.getHeaderParameterModel().get(parameter));
            }
            else {
                getHeaderParameterModel().get(parameter).merge(model.getHeaderParameterModel().get(parameter));
            }
        }
        // Cookies
        for ( String parameter : getCookieParameterModel().keySet() ) {
            if ( getCookieParameterModel().get(parameter) == null ) {
                getCookieParameterModel().put(parameter,model.getCookieParameterModel().get(parameter));
            }
            else {
                getCookieParameterModel().get(parameter).merge(model.getCookieParameterModel().get(parameter));
            }
        }


    }

    private void extractMessage(HttpTrafficRecord httpTrafficRecord) {
        // Query parameters
        TargetLocator targetLocator = new TargetLocator();
        ArrayList<TestTarget> targets;
        String cookie = null;
        if ( url.getQuery() != null ) {
            targets = targetLocator.getAllTargets(url.getQuery());
            for ( TestTarget target : targets ) {
                String val = url.getQuery().substring(target.getStartPos(),target.getEndPos());
                queryParameterModel.put(target.getTargetName(),new AnalyzerParameterModel(target.getTargetName(),val));
            }
        }
        // Headers - both request and response but not cookie
        for ( String header : httpTrafficRecord.getRequest().getHeaders()) {
            if ( header.matches("(?i)cookie")) {
                cookie = httpTrafficRecord.getRequest().getHeaderValue(header);
                continue;
            }
            headerParameterModel.put(header,new AnalyzerParameterModel(header,httpTrafficRecord.getRequest().getHeaderValue(header)));
        }
        for ( String header : httpTrafficRecord.getResponse().getHeaders()) {
            headerParameterModel.put(header,new AnalyzerParameterModel(header,httpTrafficRecord.getResponse().getHeaderValue(header)));
        }
        // Cookies
        if ( cookie != null ) {
            for ( String curCookie : cookie.split(";")) {
                String cookieName = curCookie.substring(0,curCookie.indexOf("=")-1);
                String cookieVal = curCookie.substring(curCookie.indexOf("=")+1);
                cookieParameterModel.put(cookieName,new AnalyzerParameterModel(cookieName,cookieVal));
            }
        }

    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getPath() {
        return path;
    }

    public HashMap<String, AnalyzerParameterModel> getQueryParameterModel() {
        return queryParameterModel;
    }

    public HashMap<String, AnalyzerParameterModel> getCookieParameterModel() {
        return cookieParameterModel;
    }

    public HashMap<String, AnalyzerParameterModel> getHeaderParameterModel() {
        return headerParameterModel;
    }

    public String getTokenizedUrl() {
        return tokenizedUrl;
    }
}
