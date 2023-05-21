package com.mitmws.analyzer.models;

import com.mitmws.httpproxy.trafficlogger.WebsocketTrafficRecord;
import com.mitmws.tester.TargetLocator;
import com.mitmws.tester.TestTarget;
import com.mitmws.util.AnalyzerUtil;
import com.mitmws.util.HashUtils;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class AnalyzerMessageModel {

    private String id;
    private Pattern pattern;
    private String parameterizedMessage;
    private TargetLocator targetLocator = new TargetLocator();
    private HashMap<String,AnalyzerParameterModel> parameters = new HashMap<>();
    public AnalyzerMessageModel( WebsocketTrafficRecord websocketTrafficRecord ) throws NoSuchAlgorithmException {

    }

    public String getPatternKey( String text ) {
        String patternKey = null;
        return patternKey;
    }

    public void addSample( WebsocketTrafficRecord websocketTrafficRecord ) {

    }

    public String getId() {
        return id;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public String getParameterizedMessage() {
        return parameterizedMessage;
    }

    public void setParameterizedMessage(String parameterizedMessage) {
        this.parameterizedMessage = parameterizedMessage;
    }

    public HashMap<String, AnalyzerParameterModel> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, AnalyzerParameterModel> parameters) {
        this.parameters = parameters;
    }
}
