package com.mitmws.analyzer.models;

import com.mitmws.analyzer.CacheItem;
import com.mitmws.httpproxy.trafficlogger.HttpTrafficRecord;
import com.mitmws.httpproxy.trafficlogger.TrafficRecord;
import com.mitmws.httpproxy.trafficlogger.WebsocketTrafficRecord;
import com.mitmws.tester.TargetLocator;

import java.util.ArrayList;
import java.util.HashMap;

public class AnalyzerDataModel {
    private HashMap<String, AnalyzerHostModel> analyzerHostModel = new HashMap<String,AnalyzerHostModel>();
    private HashMap<String, CacheItem> hostCache = new HashMap<String,CacheItem>();
    private HashMap<String, AnalyzerWebsocketMessageModel> analyzerMessageModel = new HashMap<String, AnalyzerWebsocketMessageModel>();
    private HashMap<String, AnalyzerConversationModel> analyzerConversationModel = new HashMap<String,AnalyzerConversationModel>();
    private TargetLocator targetLocator = new TargetLocator();
    public AnalyzerDataModel() {

    }

    public void analyzeMessage(TrafficRecord rec ) {
        System.out.println(String.format("Analyzing record ---> %s", rec.toString()));
        if ( rec.getWebsocketTrafficRecord() != null ) {
            try {
                AnalyzerWebsocketMessageModel analyzerWebsocketMessageModel = new AnalyzerWebsocketMessageModel(rec.getWebsocketTrafficRecord());
            } catch (AnalyzerException e) {
                ;
            }
        }
    }

    public void analyzeConversation( HttpTrafficRecord upgradeHttpTrafficRecord, ArrayList<WebsocketTrafficRecord> websocketTrafficRecords ) {
        System.out.println(String.format("Analyzing conversation -> %s", upgradeHttpTrafficRecord.getRequest().getMessageUUID()));
    }


}
