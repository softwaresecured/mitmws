package com.mitmws.trafficanalysis;

import com.mitmws.httpproxy.trafficlogger.*;
import com.mitmws.projects.ProjectDataService;
import com.mitmws.projects.ProjectDataServiceException;
import com.mitmws.trafficanalysis.models.AnalyzerHostModel;
import com.mitmws.trafficanalysis.models.AnalyzerMessageModel;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/*

    - When a close frame is encountered the conversation is analyzed


    Types of messages parsed:
        - xml
        - json
        - kvp
        ( whatever can be auto-targeted )

    - Create an index of message types based on the structure of the message


    - Extract parameters and analyze how they're used
    Messages might be related if:
    - ( strong ) Data dependant: Message B contains parameters provided by message A
        Ex:
            A < {"test": 12345}
            B > {"test": 12345, "status": "complete"}
    - ( weak ) The next continuous stream of inbound messages that are not PING|PONG|CLOSE|CONTINUATION is seen after the message
        Ex:
            A > {"cmd": "healthcheck"}
            B < {"oilpressure": "low"}
            C < {"oiltemp": "high"}
            D < {"altitude": 12054}

    - Messages are added by the traffic log queue processor thread
    - Conversations are analyzed when a close is encountered

 */
public class Analyzer {
    private HashMap<String, AnalyzerHostModel> analyzerHostModel = new HashMap<String,AnalyzerHostModel>();
    private HashMap<String,CacheItem> hostCache = new HashMap<String,CacheItem>();
    private HashMap<String,String> conversationIdHostMap = new HashMap<String,String>();
    private ProjectDataService projectDataService;
    public Analyzer( ProjectDataService projectDataService ) {
        this.projectDataService = projectDataService;
    }

    public String getEndpointKey( String urlStr ) throws MalformedURLException {
        URL url = new URL(urlStr);
        return url.getHost();
    }


    public void submit (TrafficRecord trafficRecord ) {
        /*
            Analyze each message as it is added by the traffic log queue processor thread
         */
        if ( trafficRecord.getWebsocketTrafficRecord() != null ) {
            if ( trafficRecord.getWebsocketTrafficRecord().getFrame() != null ) {
                try {
                    analyzeMessage(trafficRecord.getWebsocketTrafficRecord(),trafficRecord.getWebsocketTrafficRecord().getFrame().getConversationUUID());
                } catch (NoSuchAlgorithmException | IOException | ProjectDataServiceException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void analyzeMessage( WebsocketTrafficRecord record, String conversationId ) throws NoSuchAlgorithmException, IOException, ProjectDataServiceException {
        AnalyzerMessageModel analyzerMessageModel = new AnalyzerMessageModel(record);
        if ( analyzerMessageModel.getParameterizedMessage() != null ) {
            // Map the conversationId to a host id ( found in upgrade message )
            if ( conversationIdHostMap.get(conversationId) == null ) {
                HttpTrafficRecord httpTrafficRecord = projectDataService.getHttpTrafficRecordByUUID(conversationId);
                String endpointKey = getEndpointKey(httpTrafficRecord.getRequest().getUrl());
                conversationIdHostMap.put(conversationId,endpointKey);
                // Update the host cache
                if ( hostCache.get(conversationId) == null ) {
                    hostCache.put(conversationId,new CacheItem(System.currentTimeMillis(),conversationIdHostMap.get(conversationId)));

                }
            }

            // Create a host message model if it doesn't exist
            if ( analyzerHostModel.get(conversationIdHostMap.get(conversationId)) == null ) {
                analyzerHostModel.put(conversationIdHostMap.get(conversationId),new AnalyzerHostModel());
            }

            // Add the message to the model if it doesn't exist, add the new sample otherwise
            if ( analyzerHostModel.get(conversationIdHostMap.get(conversationId)).getMessageModel().get(analyzerMessageModel.getId()) == null ) {
                analyzerHostModel.get(conversationIdHostMap.get(conversationId)).getMessageModel().put(analyzerMessageModel.getId(),analyzerMessageModel);
            }
            else {
                analyzerHostModel.get(conversationIdHostMap.get(conversationId)).getMessageModel().get(analyzerMessageModel.getId()).addSample(record);
            }
        }
    }
}
