package com.mitmws.mvc.model;

import com.mitmws.configuration.ApplicationConfig;
import com.mitmws.httpproxy.trafficlogger.TrafficRecord;
import com.mitmws.projects.ProjectDataService;
import com.mitmws.trafficanalysis.Analyzer;

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
