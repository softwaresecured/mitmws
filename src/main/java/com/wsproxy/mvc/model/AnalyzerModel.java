package com.wsproxy.mvc.model;

import com.wsproxy.configuration.ApplicationConfig;
import com.wsproxy.httpproxy.trafficlogger.TrafficRecord;
import com.wsproxy.projects.ProjectDataService;
import com.wsproxy.trafficanalysis.Analyzer;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;

public class AnalyzerModel {
    private Analyzer analyzer = null;
    private SwingPropertyChangeSupport eventEmitter;
    public AnalyzerModel() {
        eventEmitter = new SwingPropertyChangeSupport(this);
    }

    public void init(ProjectDataService projectDataService) {
        System.out.println(String.format("Analyzer init: %s", projectDataService.getDbFilePath()));
        ApplicationConfig applicationConfig = new ApplicationConfig();
        if ( applicationConfig.getProperty("betafeatures.enable-analyzer").equals("true")) {
            analyzer = new Analyzer(projectDataService);
        }
    }

    public void submitRecord(TrafficRecord rec ) {
        if ( analyzer != null ) {
            analyzer.submit(rec);
        }
    }
    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
