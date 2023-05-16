package com.mitmws.mvc.thread;

import com.mitmws.anomalydetection.AnomalyScanRequest;
import com.mitmws.anomalydetection.DetectedAnomaly;
import com.mitmws.anomalydetection.DetectionLibrary;
import com.mitmws.httpproxy.trafficlogger.*;
import com.mitmws.logging.AppLog;
import com.mitmws.mvc.model.MainModel;
import com.mitmws.projects.ProjectDataServiceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
/*
    Runs on single conversation, looks up conversationUUID
    - Every message will be checked by these rules
    - Good for regex tests, pattern finding etc
 */
public class PassiveAnomalyDetectorThread extends Thread {

    private Logger LOGGER = AppLog.getLogger(PassiveAnomalyDetectorThread.class.getName());
    boolean shutdownRequested = false;
    private MainModel mainModel;
    public PassiveAnomalyDetectorThread( MainModel mainModel ) {
        this.mainModel = mainModel;
    }
    public void shutdown() {
        shutdownRequested = true;
    }

    private ArrayList<WebsocketTrafficRecord> getTrafficRecords(String testSequenceId ) {
        ArrayList<WebsocketTrafficRecord> records = new ArrayList<>();
        try {
            records.addAll(mainModel.getProjectModel().getProjectDataService().getWebsocketTrafficRecordByConversationUUID(testSequenceId));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ProjectDataServiceException e) {
            e.printStackTrace();
        }
        return records;
    }
    public void run() {
        LOGGER.info("Passive anomaly detector started");
        while ( !shutdownRequested ) {
            try {
                AnomalyScanRequest scanRequest = (AnomalyScanRequest) mainModel.getProjectModel().getPassiveAnomalyScanQueue().poll();
                if ( scanRequest != null ) {
                    for ( String conversationUuid : scanRequest.getConversationUuids() ) {
                        ArrayList<WebsocketTrafficRecord> websocketTrafficRecords = getTrafficRecords(conversationUuid);
                        if ( websocketTrafficRecords.size() > 0 ) {
                            String testName = mainModel.getProjectModel().getProjectDataService().getTestNameByConversationUUID(conversationUuid);
                            DetectionLibrary detectionLibrary = mainModel.getRulesModel().getPassiveRules();
                            ArrayList<DetectedAnomaly> anomalies = detectionLibrary.detectAnomalies(websocketTrafficRecords, testName);
                            if ( anomalies != null ) {
                                mainModel.getProjectModel().getDetectedAnomalies().addAll(anomalies);
                                for ( DetectedAnomaly anomaly : anomalies ) {
                                    anomaly.setConversationUuid(conversationUuid);
                                    LOGGER.info(String.format("Logged anomaly %s for sequence %s", anomaly.getAnomalyId(), conversationUuid));
                                    mainModel.getAnomaliesModel().addAnomaly(anomaly);
                                }
                            }
                            else {
                                System.out.println("Detected 0 anomalies");
                            }
                        }
                    }
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ProjectDataServiceException e) {
                e.printStackTrace();
            }
        }
        LOGGER.info("Passive anomaly detector stopped");
    }
}