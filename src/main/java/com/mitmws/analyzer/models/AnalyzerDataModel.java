package com.mitmws.analyzer.models;
import com.mitmws.httpproxy.trafficlogger.HttpTrafficRecord;
import com.mitmws.httpproxy.trafficlogger.TrafficRecord;
import com.mitmws.httpproxy.trafficlogger.WebsocketTrafficRecord;
import com.mitmws.tester.TargetLocator;

import java.util.ArrayList;
import java.util.HashMap;

public class AnalyzerDataModel {
    private HashMap<String, AnalyzerWebsocketMessageModel> analyzerWebsocketMessageModels = new HashMap<String, AnalyzerWebsocketMessageModel>();
    private HashMap<String, AnalyzerHttpMessageModel> analyzerHttpMessageModels = new HashMap<String, AnalyzerHttpMessageModel>();
    private HashMap<String, AnalyzerConversationModel> analyzerConversationModel = new HashMap<String,AnalyzerConversationModel>();
    private HashMap<String,String> upgradeMessageIdHttpMessageModelAssociationCache = new HashMap<String,String>();
    private TargetLocator targetLocator = new TargetLocator();

    public AnalyzerDataModel() {

    }

    public void analyzeMessage(TrafficRecord rec ) {
        String assocUpgradeId;

        // HTTP
        if ( rec.getHttpTrafficRecord() != null ) {
            assocUpgradeId = upgradeMessageIdHttpMessageModelAssociationCache.get(rec.getHttpTrafficRecord().getRequest().getMessageUUID());
            AnalyzerHttpMessageModel analyzerHttpMessageModel = null;
            try {
                analyzerHttpMessageModel = new AnalyzerHttpMessageModel(rec.getHttpTrafficRecord());
                if ( assocUpgradeId == null ) {
                    upgradeMessageIdHttpMessageModelAssociationCache.put(rec.getHttpTrafficRecord().getRequest().getMessageUUID(),analyzerHttpMessageModel.getId());
                    analyzerHttpMessageModels.put(assocUpgradeId,analyzerHttpMessageModel);
                }
                else {
                    analyzerHttpMessageModels.get(assocUpgradeId).merge(analyzerHttpMessageModel);
                }
            } catch (AnalyzerException e) {
                e.printStackTrace();
            }
        }

        // Websocket
        if ( rec.getWebsocketTrafficRecord() != null ) {
            try {
                assocUpgradeId = upgradeMessageIdHttpMessageModelAssociationCache.get(rec.getWebsocketTrafficRecord().getFrame().getUpgradeMessageUUID());
                if ( assocUpgradeId != null ) {
                    AnalyzerWebsocketMessageModel analyzerWebsocketMessageModel = new AnalyzerWebsocketMessageModel(rec.getWebsocketTrafficRecord(),assocUpgradeId);
                    AnalyzerWebsocketMessageModel existingAnalyzerWebsocketMessageModel = analyzerWebsocketMessageModels.get(analyzerWebsocketMessageModel.getTokenizedMessageHash());
                    if ( existingAnalyzerWebsocketMessageModel == null ) {
                        analyzerWebsocketMessageModels.put(analyzerWebsocketMessageModel.getTokenizedMessageHash(),analyzerWebsocketMessageModel);
                    }
                    else {
                        existingAnalyzerWebsocketMessageModel.merge(analyzerWebsocketMessageModel);
                    }
                }
                else {
                    // Should not happen?
                    throw new AnalyzerException("Missing associated upgrade message Id in upgrade association cache");
                }
            } catch (AnalyzerException e) {
                ;
            }
        }
    }

    public void analyzeConversation( HttpTrafficRecord upgradeHttpTrafficRecord, ArrayList<WebsocketTrafficRecord> websocketTrafficRecords ) {
        // Remove it from the upgradeMessageIdHttpMessageModelAssociationCache since we'll never see it again
        upgradeMessageIdHttpMessageModelAssociationCache.remove(upgradeHttpTrafficRecord.getRequest().getMessageUUID());
        printModelStats();
    }

    // Debug
    public void printModelStats() {
        int analyzerWebsocketMessageModelsSize = analyzerWebsocketMessageModels.size();
        int analyzerHttpMessageModelsSize = analyzerHttpMessageModels.size();
        int analyzerConversationModelSize = analyzerConversationModel.size();
        int upgradeMessageIdHttpMessageModelAssociationCacheSize = upgradeMessageIdHttpMessageModelAssociationCache.size();
        System.out.println(String.format(String.format("%d %d %d",
                analyzerWebsocketMessageModelsSize,
                analyzerHttpMessageModelsSize,
                analyzerConversationModelSize,
                upgradeMessageIdHttpMessageModelAssociationCacheSize
                )));
    }

    /*
        Questions we can ask the model
     */

    /*
        Returns a list of trackable parameters from the model for a given endpoint
        Endpoint is a baseurl + path
     */
    public HashMap<ParameterLocation,AnalyzerParameterModel> getTrackableParameters( String endpoint ) {
        HashMap<ParameterLocation,AnalyzerParameterModel> trackableParameters = new HashMap<ParameterLocation,AnalyzerParameterModel>();
        return trackableParameters;
    }

}
