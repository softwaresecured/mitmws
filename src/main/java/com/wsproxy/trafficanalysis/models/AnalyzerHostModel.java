package com.wsproxy.trafficanalysis.models;

import java.util.HashMap;

public class AnalyzerHostModel {
    private HashMap<String,AnalyzerMessageModel> messageModel = new HashMap<String,AnalyzerMessageModel>();
    public AnalyzerHostModel() {

    }

    public HashMap<String, AnalyzerMessageModel> getMessageModel() {
        return messageModel;
    }

}
