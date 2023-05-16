package com.mitmws.mvc.thread;
import com.mitmws.httpproxy.trafficlogger.*;
import com.mitmws.logging.AppLog;
import com.mitmws.mvc.model.MainModel;
import com.mitmws.projects.ProjectDataServiceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class TrafficLoaderThread extends  Thread {
    private final Logger LOGGER = AppLog.getLogger(TrafficLoaderThread.class.getName());
    boolean shutdownRequested = false;
    private MainModel mainModel;
    private int LOAD_BATCH_SIZE = 10;
    public TrafficLoaderThread(MainModel mainModel ) {
        this.mainModel = mainModel;
    }
    public void shutdown() {
        shutdownRequested = true;
    }
    public void run() {
        try {
            loadTraffic();
        } catch (IOException e) {
            e.printStackTrace();
        } catch ( ProjectDataServiceException e) {
            e.printStackTrace();
        }
    }
    public void loadTraffic() throws IOException, ProjectDataServiceException {
        // load proxy traffic
        int rowCount = mainModel.getProjectModel().getProjectDataService().countHttpTrafficRecordBySource(TrafficSource.PROXY);
        for ( int i = 0; i < rowCount; i+=LOAD_BATCH_SIZE ) {
            ArrayList<HttpTrafficRecord> records = mainModel.getProjectModel().getProjectDataService().getHttpTrafficRecordsBySource(TrafficSource.PROXY,i,LOAD_BATCH_SIZE);
            for ( HttpTrafficRecord rec : records ) {
                mainModel.getTrafficModel().addHttpTraffic(rec);
            }
        }
        rowCount = mainModel.getProjectModel().getProjectDataService().countWebsocketTrafficRecordBySource(TrafficSource.PROXY);
        for ( int i = 0; i < rowCount; i+=LOAD_BATCH_SIZE ) {
            ArrayList<WebsocketTrafficRecord> records = mainModel.getProjectModel().getProjectDataService().getWebsocketTrafficRecordBySource(TrafficSource.PROXY,i,LOAD_BATCH_SIZE);
            for ( WebsocketTrafficRecord rec : records ) {
                mainModel.getTrafficModel().addWebsocketTraffic(rec.getFrame(), null);
            }
        }
        // load manual test traffic
        rowCount = mainModel.getProjectModel().getProjectDataService().countWebsocketTrafficRecordBySource(TrafficSource.MANUAL_TEST);
        for ( int i = 0; i < rowCount; i+=LOAD_BATCH_SIZE ) {
            ArrayList<WebsocketTrafficRecord> records = mainModel.getProjectModel().getProjectDataService().getWebsocketTrafficRecordBySource(TrafficSource.MANUAL_TEST,i,LOAD_BATCH_SIZE);
            for ( WebsocketTrafficRecord rec : records ) {
                mainModel.getManualTesterModel().addWebsocketTraffic(rec.getFrame(),rec.getTestName(),rec.getHighlightColour());
            }
        }
        // load immediate traffic
        rowCount = mainModel.getProjectModel().getProjectDataService().countWebsocketTrafficRecordBySource(TrafficSource.IMMEDIATE);
        for ( int i = 0; i < rowCount; i+=LOAD_BATCH_SIZE ) {
            ArrayList<WebsocketTrafficRecord> records = mainModel.getProjectModel().getProjectDataService().getWebsocketTrafficRecordBySource(TrafficSource.IMMEDIATE,i,LOAD_BATCH_SIZE);
            for ( WebsocketTrafficRecord rec : records ) {
                mainModel.getImmediateModel().addWebsocketTraffic(rec.getFrame(),rec.getTestName());
            }
        }
    }
}
