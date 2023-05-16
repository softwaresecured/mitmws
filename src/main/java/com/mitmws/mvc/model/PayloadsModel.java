package com.mitmws.mvc.model;

import com.mitmws.configuration.ApplicationConfig;
import com.mitmws.tester.PayloadList;
import com.mitmws.util.TestUtil;

import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.table.DefaultTableModel;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class PayloadsModel {
    private ApplicationConfig applicationConfig = new ApplicationConfig();
    public ArrayList<PayloadList> payloadLibrary = new ArrayList<>();
    private PayloadList customPayloadList = new PayloadList();
    private DefaultTableModel payloadsTableModel = new DefaultTableModel();
    private int enabledPayloadCount = 0;

    private SwingPropertyChangeSupport eventEmitter;
    public PayloadsModel() {
        for ( String col: new String[] { "Enabled", "Name", "Description"}) {
            payloadsTableModel.addColumn(col);
        }
        customPayloadList.loadSamples();
        eventEmitter = new SwingPropertyChangeSupport(this);
    }

    public int getEnabledPayloadCount() {
        return enabledPayloadCount;
    }

    public void setEnabledPayloadCount(int enabledPayloadCount) {
        this.enabledPayloadCount = enabledPayloadCount;
        eventEmitter.firePropertyChange("PayloadsModel.enabledPayloadCount", null, enabledPayloadCount);
    }

    public DefaultTableModel getPayloadsTableModel() {
        return payloadsTableModel;
    }

    public void reloadPayloads( boolean customOnly ) {
        payloadLibrary.add(customPayloadList);
        payloadLibrary = TestUtil.reloadPayloadLibrary( customPayloadList, applicationConfig.getConfigDirPath() );
        if ( customOnly ) {
            for ( PayloadList payloadList : payloadLibrary) {
                if ( !payloadList.getPayloadListName().equals("Custom")) {
                    payloadList.setEnabled(false);
                }
            }
        }
        payloadsTableModel.setRowCount(0);
        for ( PayloadList payloadList : payloadLibrary ) {
            payloadsTableModel.addRow(new Object[] {
                    payloadList.isEnabled(),
                    payloadList.getPayloadListName(),
                    payloadList.getPayloadDescription(),
            });
        }
        updateEnabledPayloadCount();
    }
    public void reloadPayloads() {
        reloadPayloads(false);
    }

    public void updateEnabledPayloadCount() {
        int payloadCount = 0;
        for ( PayloadList payloadList : payloadLibrary ) {
            if ( payloadList.isEnabled() ) {
                payloadCount += payloadList.getPayloads().size();
            }
        }
        setEnabledPayloadCount(payloadCount);
    }

    public ArrayList<PayloadList> getPayloadLibrary() {
        return payloadLibrary;
    }

    public void setPayloadLibrary(ArrayList<PayloadList> payloadLibrary) {
        this.payloadLibrary = payloadLibrary;
    }

    public PayloadList getCustomPayloadList() {
        return customPayloadList;
    }

    public void updateCustomPayloadList(String payloadListText ) {
        String lines[] = payloadListText.split("\n");
        customPayloadList.getPayloads().clear();
        for ( String line : lines ) {
            if ( line.length() > 0 ) {
                customPayloadList.getPayloads().add(line);
            }
        }
        updateEnabledPayloadCount();
        eventEmitter.firePropertyChange("PayloadsModel.customPayloadList", null, enabledPayloadCount);
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
