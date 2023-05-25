package com.mitmws.analyzer.models;

import com.mitmws.httpproxy.trafficlogger.WebsocketDirection;
import com.mitmws.analyzer.DataType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class AnalyzerParameterModel {
    private final int MAX_VALUE_VARIANT_COUNT = 32;
    private String name;
    private ArrayList<String> valueVariants = new ArrayList<String>();
    private ArrayList<String> tags = new ArrayList<String>();
    private ArrayList<DataType> observedDataTypes = new ArrayList<DataType>();
    public AnalyzerParameterModel(String name) {
        this.name = name;
    }

    public void addTag( String tag ) {
        if ( !tags.contains(tag)) {
            tags.add(tag);
        }
    }
    public ArrayList<String> getTags() {
        return tags;
    }

    private void addObservedDataType ( DataType dataType ) {
        if ( !observedDataTypes.contains(dataType )) {
            observedDataTypes.add(dataType);
        }
    }
    public ArrayList<DataType> getObservedDataTypes() {
        return observedDataTypes;
    }


    public void addValueVariant(String value ) {
        if ( value != null ) {
            if ( valueVariants.size() < MAX_VALUE_VARIANT_COUNT ) {
                addObservedDataType(getDataType(value));
                valueVariants.add(value);
            }
        }
    }
    public ArrayList<String> getValueVariants() {
        return valueVariants;
    }

    public String getName() {
        return name;
    }

    private DataType getDataType ( String data ) {
        DataType dataType = DataType.UNKNOWN;
        if ( data.matches("\\d+")) {
            dataType = DataType.NUMBER;
        }

        // url
        if ( !observedDataTypes.contains(DataType.URL)) {
            try {
                URL testUrl = new URL(data);
                observedDataTypes.add(DataType.URL);
            } catch (MalformedURLException e) {
                ;
            }
        }

        if ( data.matches("(?i)[0-9a-f]{32}")) {
            dataType = DataType.MD5_HASH;
        }

        if ( data.matches("(?i)[0-9a-f]{40}")) {
            dataType = DataType.SHA1_HASH;
        }

        if ( data.matches("(?i)\\d{10}")) {
            dataType = DataType.UNIX_TIMESTAMP_SEC;
        }

        if ( data.matches("(?i)\\d{13}")) {
            dataType = DataType.UNIX_TIMESTAMP_MSEC;
        }


        // TODO Other tests...
        // Catchall
        if ( data.matches(".*")) {
            dataType = DataType.TEXT;
        }
        return dataType;
    }

}
