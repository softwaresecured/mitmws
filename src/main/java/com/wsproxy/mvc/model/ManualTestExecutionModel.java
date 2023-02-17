package com.wsproxy.mvc.model;

import com.wsproxy.httpproxy.HttpMessage;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class ManualTestExecutionModel {

    private String status = "STOPPED";
    private int currentTestStep = 0;
    private HttpMessage upgradeRequest = null;
    private HttpMessage upgradeResponse = null;
    private SwingPropertyChangeSupport eventEmitter;

    public ManualTestExecutionModel() {
        eventEmitter = new SwingPropertyChangeSupport(this);
    }

    public HttpMessage getUpgradeRequest() {
        return upgradeRequest;
    }

    public void setUpgradeRequest(HttpMessage upgradeRequest) {
        HttpMessage oldVal = this.upgradeRequest;
        this.upgradeRequest = upgradeRequest;
        eventEmitter.firePropertyChange("ManualTestExecutionModel.upgradeRequest", oldVal, this.upgradeRequest);
    }

    public HttpMessage getUpgradeResponse() {
        return upgradeResponse;
    }

    public void setUpgradeResponse(HttpMessage upgradeResponse) {
        HttpMessage oldVal = this.upgradeResponse;
        this.upgradeResponse = upgradeResponse;
        eventEmitter.firePropertyChange("ManualTestExecutionModel.upgradeResponse", oldVal, this.upgradeResponse);
    }

    public void addLogMessage(String message ) {
        eventEmitter.firePropertyChange("ManualTestExecutionModel.message", null, message);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        String oldVal = this.status;
        this.status = status;
        eventEmitter.firePropertyChange("ManualTestExecutionModel.status", oldVal, this.status);
    }

    public int getCurrentTestStep() {
        return currentTestStep;
    }

    public void setCurrentTestStep(int currentTestStep) {
        int oldVal = this.currentTestStep;
        this.currentTestStep = currentTestStep;
        eventEmitter.firePropertyChange("ManualTestExecutionModel.currentTestStep", oldVal, this.currentTestStep);
    }


    public SwingPropertyChangeSupport getEventEmitter() {
        return eventEmitter;
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }

}
