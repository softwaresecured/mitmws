package com.mitmws.analyzer.models;

import com.mitmws.httpproxy.trafficlogger.WebsocketDirection;
import com.mitmws.analyzer.DataType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class AnalyzerParameterModel {
    private final int MAX_VALUE_VARIANT_COUNT = 32;
    private String name;
    private ArrayList<WebsocketDirection> observedDirections = new ArrayList<WebsocketDirection>();
    private ArrayList<String> valueVariants = new ArrayList<String>();
    private ArrayList<String> tags = new ArrayList<String>();
    private ArrayList<String> assosicatedConversationPatterns = new ArrayList<String>();
    private ArrayList<DataType> observedDataTypes = new ArrayList<DataType>();
    public AnalyzerParameterModel(String name) {
        this.name = name;
    }

    public ArrayList<WebsocketDirection> getObservedDirections() {
        return observedDirections;
    }

    public void addObservedDirection( WebsocketDirection direction ) {
        if ( !observedDirections.contains(direction)) {
            observedDirections.add(direction);
        }
    }

    public void updateDataTypes() {

    }

    public ArrayList<DataType> getObservedDataTypes() {
        return observedDataTypes;
    }

    public void addValueVariant(String value ) {

    }

    public ArrayList<String> getAssosicatedConversationPatterns() {
        return assosicatedConversationPatterns;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getValueVariants() {
        return valueVariants;
    }

    public ArrayList<String> getTags() {
        return tags;
    }
}
