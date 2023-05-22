package com.mitmws.mvc.model;

import com.mitmws.analyzer.models.AnalyzerDataModel;
import com.mitmws.configuration.ApplicationConfig;
import com.mitmws.httpproxy.trafficlogger.TrafficRecord;
import com.mitmws.mvc.thread.AnalyzerWorkerThread;
import com.mitmws.projects.ProjectDataService;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;

public class AnalyzerModel {
    private AnalyzerDataModel analyzerDataModel = new AnalyzerDataModel();
    private SwingPropertyChangeSupport eventEmitter;
    private AnalyzerWorkerThread analyzerWorkerThread = null;
    public AnalyzerModel() {
        eventEmitter = new SwingPropertyChangeSupport(this);
    }

    public void init(ProjectDataService projectDataService) {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        if ( applicationConfig.getProperty("betafeatures.enable-analyzer").equals("true")) {
            analyzerWorkerThread = new AnalyzerWorkerThread(projectDataService,analyzerDataModel);
            analyzerWorkerThread.start();
        }
    }

    public void submitRecord(TrafficRecord rec ) {
        if ( analyzerWorkerThread != null ) {
            analyzerWorkerThread.submitRecord(rec);
        }
    }

    public void submitConversation( String conversationUuid ) {
        if ( analyzerWorkerThread != null ) {
            analyzerWorkerThread.submitConversation(conversationUuid);
        }
    }

    public AnalyzerWorkerThread getAnalyzerWorkerThread() {
        return analyzerWorkerThread;
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
