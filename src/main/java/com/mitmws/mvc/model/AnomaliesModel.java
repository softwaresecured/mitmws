package com.mitmws.mvc.model;

import com.mitmws.anomalydetection.DetectedAnomaly;

import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.table.DefaultTableModel;
import java.beans.PropertyChangeListener;

public class AnomaliesModel {
    private DefaultTableModel anomaliesTableModel;
    private DefaultTableModel conversationTableModel;
    private DetectedAnomaly currentAnomaly;
    private SwingPropertyChangeSupport eventEmitter;
    public AnomaliesModel() {
        currentAnomaly = null;
        anomaliesTableModel = new DefaultTableModel();
        conversationTableModel = new DefaultTableModel();
        for ( String col: new String[] { "id", "Detector", "Source","Credibility", "Test name", "CWE", "title"}) {
            anomaliesTableModel.addColumn(col);
        }
        for ( String col: new String[] { "id", "Time","--","OPCODE","LEN","Payload" }) {
            conversationTableModel.addColumn(col);
        }
        eventEmitter = new SwingPropertyChangeSupport(this);
    }


    public DefaultTableModel getAnomaliesTableModel() {
        return anomaliesTableModel;
    }

    public DefaultTableModel getConversationTableModel() {
        return conversationTableModel;
    }

    public DetectedAnomaly getCurrentAnomaly() {
        return currentAnomaly;
    }

    public void setCurrentAnomaly(DetectedAnomaly currentAnomaly) {
        this.currentAnomaly = currentAnomaly;
        eventEmitter.firePropertyChange("AnomaliesModel.currentAnomaly", null, currentAnomaly);
    }

    public void addAnomaly(DetectedAnomaly anomaly) {
        anomaliesTableModel.addRow(new Object[]{
                anomaly.getAnomalyId(),
                anomaly.getDetector(),
                anomaly.getSource(),
                anomaly.getCredibility(),
                anomaly.getTestName(),
                anomaly.getCWE(),
                anomaly.getTitle()
        });

        eventEmitter.firePropertyChange("AnomaliesModel.anomaliesTableModel", null, anomaly);
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
