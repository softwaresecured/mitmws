package com.mitmws.mvc.controller;

import com.mitmws.configuration.ApplicationConfigException;
import com.mitmws.mvc.model.MainModel;
import com.mitmws.mvc.view.frames.FrmSettingsView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

public class SettingsController implements PropertyChangeListener {
    private MainModel mainModel;
    private FrmSettingsView frmSettingsView;

    public SettingsController(MainModel mainModel, FrmSettingsView frmSettingsView) {
        this.mainModel = mainModel;
        this.frmSettingsView = frmSettingsView;
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
        frmSettingsView.btnTest.addActionListener(actionEvent -> {
            validateSettings();
        });
    }

    public boolean validateSettings() {
        String[] optionalKeys = new String[] {
                "rules.enabled.active",
                "rules.enabled.passive",
                "interactsh.serverurl",
                "interactsh.token"
        };
        boolean valid = true;
        for ( int i = 0; i < mainModel.getSettingsModel().getSettingsTableModel().getRowCount(); i++ ) {
            String key = (String)mainModel.getSettingsModel().getSettingsTableModel().getValueAt(i,1);
            String value = (String)mainModel.getSettingsModel().getSettingsTableModel().getValueAt(i,2);
            // Some keys are optional
            System.out.println(String.format("Validating %s/[%s]", key,value));
            if ( Arrays.stream(optionalKeys).anyMatch(key::equals) && value.length() == 0 ) {
                continue;
            }
            try {
                mainModel.getSettingsModel().getApplicationConfig().validateProperty(key,value);
                mainModel.getSettingsModel().getSettingsTableModel().setValueAt(true,i,0);
            } catch (ApplicationConfigException e) {
                mainModel.getSettingsModel().getSettingsTableModel().setValueAt(false,i,0);
                System.out.println(String.format("Key %s/%s is not valid", key,value));
                valid = false;
            }
        }
        // select the first item that needs review
        if ( !valid ) {
            for(int i = 0; i < frmSettingsView.tblSettings.getRowCount(); i++ ) {
                if ( (boolean) frmSettingsView.tblSettings.getValueAt(i,0) == false ) {
                    frmSettingsView.tblSettings.getSelectionModel().setSelectionInterval(i,i);
                    break;
                }
            }
            frmSettingsView.tblSettings.invalidate();
        }
        return valid;
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

    }
}
