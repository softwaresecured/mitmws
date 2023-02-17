package com.wsproxy.trafficanalysis.models;

import com.wsproxy.httpproxy.trafficlogger.WebsocketDirection;
import com.wsproxy.trafficanalysis.DataType;

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
        observedDataTypes.add(DataType.UNKNOWN);
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
        for ( String variant : valueVariants ) {
            if ( variant.matches("\\d+") && !observedDataTypes.contains(DataType.NUMBER)) {
                observedDataTypes.add(DataType.NUMBER);
            }


            // url
            if ( !observedDataTypes.contains(DataType.URL)) {
                try {
                    URL testUrl = new URL(variant);
                    observedDataTypes.add(DataType.URL);
                } catch (MalformedURLException e) {
                    ;
                }
            }

            // TODO Other tests...


            // Catchall
            if ( variant.matches(".*") && !observedDataTypes.contains(DataType.TEXT)) {
                observedDataTypes.add(DataType.TEXT);
            }
        }
    }

    public ArrayList<DataType> getObservedDataTypes() {
        return observedDataTypes;
    }

    public void addValueVariant(String value ) {
        if ( value != null ) {
            if ( !valueVariants.contains(value)) {
                valueVariants.add(value);
                if ( valueVariants.size() > MAX_VALUE_VARIANT_COUNT ) {
                    valueVariants.remove(0);
                }
            }
            updateDataTypes();
        }
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
