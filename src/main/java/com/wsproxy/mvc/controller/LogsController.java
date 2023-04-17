package com.wsproxy.mvc.controller;

import com.wsproxy.mvc.model.LogModel;
import com.wsproxy.mvc.view.frames.FrmLogsView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class LogsController implements PropertyChangeListener {

    private LogModel logModel;
    private FrmLogsView frmLogsView;

    public LogsController(LogModel logModel, FrmLogsView frmLogsView) {
        this.logModel = logModel;
        this.frmLogsView = frmLogsView;
        this.logModel.addListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if ( "LogModel.lastMessage".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() != null ) {
                frmLogsView.jtxtLogs.append(String.format("%s\n", (String)propertyChangeEvent.getNewValue()));
            }
        }
    }
}
