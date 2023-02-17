package com.wsproxy.mvc.controller;

import com.wsproxy.mvc.model.LogModel;
import com.wsproxy.mvc.view.panels.logs.PnlLogs;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class LogsController implements PropertyChangeListener {

    private LogModel logModel;
    private PnlLogs pnlLogs;

    public LogsController(LogModel logModel, PnlLogs pnlLogs) {
        this.logModel = logModel;
        this.pnlLogs = pnlLogs;
        this.logModel.addListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if ( "LogModel.lastMessage".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() != null ) {
                pnlLogs.jtxtLogs.append(String.format("%s\n", (String)propertyChangeEvent.getNewValue()));
            }
        }
    }
}
