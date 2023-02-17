package com.wsproxy.mvc.controller;

import com.wsproxy.configuration.ApplicationConfigException;
import com.wsproxy.mvc.model.MainModel;
import com.wsproxy.mvc.model.SettingsModel;
import com.wsproxy.mvc.view.panels.settings.PnlSettingsView;
import com.wsproxy.util.NetUtils;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;

public class SettingsController implements PropertyChangeListener {
    private MainModel mainModel;
    private PnlSettingsView pnlSettingsView;

    public SettingsController(MainModel mainModel, PnlSettingsView pnlSettingsView) {
        this.mainModel = mainModel;
        this.pnlSettingsView = pnlSettingsView;
        initEventListeners();
        loadProperties();
    }

    // Loads the properties into the table
    public void loadProperties() {
        mainModel.getSettingsModel().getSettingsTableModel().setRowCount(0);
        for ( Object obj : mainModel.getSettingsModel().getApplicationConfig().getProperties().keySet()) {
            String propName = (String)obj;
            if ( mainModel.getSettingsModel().getApplicationConfig().getConfigPropertyTypeMap().get(propName) != null ) {
                mainModel.getSettingsModel().getSettingsTableModel().addRow(new Object[] {
                        true,
                        propName,
                        mainModel.getSettingsModel().getApplicationConfig().getProperty(propName),
                        mainModel.getSettingsModel().getPropertyDescription(propName)
                });
            }
        }
    }

    public void initEventListeners() {
        pnlSettingsView.btnTest.addActionListener( actionEvent -> {
            validateSettings();
        });
    }

    public boolean validateSettings() {
        boolean valid = true;
        for ( int i = 0; i < mainModel.getSettingsModel().getSettingsTableModel().getRowCount(); i++ ) {
            //boolean valid = (boolean) settingsModel.getSettingsTableModel().getValueAt(i,0);
            String key = (String)mainModel.getSettingsModel().getSettingsTableModel().getValueAt(i,1);
            String value = (String)mainModel.getSettingsModel().getSettingsTableModel().getValueAt(i,2);
            try {
                mainModel.getSettingsModel().getApplicationConfig().validateProperty(key,value);
                mainModel.getSettingsModel().getSettingsTableModel().setValueAt(true,i,0);
            } catch (ApplicationConfigException e) {
                mainModel.getSettingsModel().getSettingsTableModel().setValueAt(false,i,0);
                valid = false;
            }
        }
        // select the first item that needs review
        if ( !valid ) {
            for( int i = 0; i < pnlSettingsView.tblSettings.getRowCount(); i++ ) {
                if ( (boolean)pnlSettingsView.tblSettings.getValueAt(i,0) == false ) {
                    pnlSettingsView.tblSettings.getSelectionModel().setSelectionInterval(i,i);
                    break;
                }
            }
            pnlSettingsView.tblSettings.invalidate();
        }
        return valid;
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

    }
}
