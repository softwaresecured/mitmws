package com.wsproxy.mvc.controller;

import com.wsproxy.anomalydetection.DetectedAnomaly;
import com.wsproxy.jsonobjects.TldDataModel;
import com.wsproxy.mvc.model.InteractshModel;
import com.wsproxy.mvc.model.MainModel;
import com.wsproxy.mvc.view.panels.interactsh.PnlInteractsh;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class InteractshController implements PropertyChangeListener {

    private MainModel mainModel;
    private InteractshModel interactshModel;
    private PnlInteractsh pnlInteractsh;

    public InteractshController(InteractshModel interactshModel, MainModel mainModel, PnlInteractsh pnlInteractsh) {
        this.interactshModel = interactshModel;
        this.pnlInteractsh = pnlInteractsh;
        this.mainModel = mainModel;
        this.interactshModel.addListener(this);
        initEventListeners();
    }

    public void initEventListeners() {
        interactshModel.getInteractionsTableModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if ( pnlInteractsh.jtblInteractions.getSelectedRow() == -1 ) {

                }
            }
        });

        pnlInteractsh.jtblInteractions.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                pnlInteractsh.jtxtRequest.setText("");
                int row = pnlInteractsh.jtblInteractions.getSelectedRow();
                if ( row >= 0 ) {
                    String id = (String) pnlInteractsh.jtblInteractions.getValueAt(row,0);
                    if ( id != null ) {
                        for (TldDataModel tldDataModel : interactshModel.getPollData()) {
                            if ( tldDataModel.getId().equals(id)) {
                                pnlInteractsh.jtxtRequest.setText(tldDataModel.getRawRequest());
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if ( "InteractshModel.interaction".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() != null ) {
                String conversation = interactshModel.getPayloadConversationMap().get((String)propertyChangeEvent.getNewValue());
                if ( conversation != null ) {
                    String parts[] = conversation.split("\t");
                    DetectedAnomaly detectedAnomaly = new DetectedAnomaly(Integer.parseInt(parts[2]), "HIGH", "Interactsh", "CWE-000", "Interact-sh interaction detected", "A callback was detected by the interactsh server");
                    detectedAnomaly.setDetector("Interactsh");
                    detectedAnomaly.setConversationUuid(parts[0]);
                    detectedAnomaly.setTestName(parts[1]);
                    mainModel.getAnomaliesModel().addAnomaly(detectedAnomaly);
                    mainModel.getProjectModel().getDetectedAnomalies().add(detectedAnomaly);
                }
            }
        }
    }
}
