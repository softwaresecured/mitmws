package com.mitmws.mvc.thread;

import com.mitmws.analyzer.models.AnalyzerDataModel;
import com.mitmws.httpproxy.trafficlogger.HttpTrafficRecord;
import com.mitmws.httpproxy.trafficlogger.TrafficRecord;
import com.mitmws.httpproxy.trafficlogger.WebsocketTrafficRecord;
import com.mitmws.logging.AppLog;
import com.mitmws.projects.ProjectDataService;
import com.mitmws.projects.ProjectDataServiceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class AnalyzerWorkerThread extends Thread {
    private final int MESSAGE_ANALYZER_BACKLOG_LEN = 1024;
    private final int CONVERSATION_ANALYZER_BACKLOG_LEN = 1024;
    private boolean shutdownRequested = false;
    private ProjectDataService projectDataService;
    private AnalyzerDataModel analyzerDataModel;
    private ArrayBlockingQueue conversationAnalysisQueue = new ArrayBlockingQueue(CONVERSATION_ANALYZER_BACKLOG_LEN);
    private ArrayBlockingQueue messageAnalysisQueue = new ArrayBlockingQueue(MESSAGE_ANALYZER_BACKLOG_LEN);
    private Logger LOGGER = AppLog.getLogger(LogTailerThread.class.getName());

    public AnalyzerWorkerThread(ProjectDataService projectDataService, AnalyzerDataModel analyzerDataModel) {
        this.projectDataService = projectDataService;
        this.analyzerDataModel = analyzerDataModel;
    }
    public void shutdown() {
        shutdownRequested = true;
    }

    /*
        Individual messages are submitted as they flow through the proxy
        This analysis is focused on the messages
     */
    public void submitRecord(TrafficRecord rec ) {
        try {
            messageAnalysisQueue.put(rec);
        } catch (InterruptedException e) {
            LOGGER.severe(String.format("Error queuing message for analysis - %s", e.getMessage()));
        }
    }

    /*
        Entire conversations are analyzed upon completion
        This analysis is focused on the conversation, the individual messages would have already been seen at this point
     */
    public void submitConversation( String conversationUuid ) {
        try {
            conversationAnalysisQueue.put(conversationUuid);
        } catch (InterruptedException e) {
            LOGGER.severe(String.format("Error queuing conversation for analysis - %s", e.getMessage()));
        }
    }


    public void run() {
        LOGGER.info("Analyzer worker thread started");
        while ( !shutdownRequested ) {

            // Do analyzer stuff

            // Get new messages to analyze
            TrafficRecord trafficRecord = null;
            do {
                trafficRecord = (TrafficRecord) messageAnalysisQueue.poll();
                if ( trafficRecord != null ) {
                    analyzerDataModel.analyzeMessage(trafficRecord);
                }
            } while ( trafficRecord != null );

            // Get new conversations to analyze
            String conversationUuid = null;
            do {
                conversationUuid = (String) conversationAnalysisQueue.poll();
                if ( conversationUuid != null ) {
                    try {
                        HttpTrafficRecord websocketUpgradeRequest = projectDataService.getHttpTrafficRecordByUUID(conversationUuid);
                        ArrayList<WebsocketTrafficRecord> websocketTrafficRecords = projectDataService.getWebsocketTrafficRecordsByUpgradeMessageUUID ( conversationUuid );
                        if ( websocketUpgradeRequest != null && websocketTrafficRecords != null ) {
                            if ( websocketTrafficRecords.size() > 0 ) {
                                analyzerDataModel.analyzeConversation(websocketUpgradeRequest, websocketTrafficRecords);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ProjectDataServiceException e) {
                        e.printStackTrace();
                    }
                }
            } while ( conversationUuid != null );

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LOGGER.info("Analyzer worker thread stopping");
    }
}