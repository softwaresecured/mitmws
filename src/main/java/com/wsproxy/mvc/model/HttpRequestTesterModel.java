package com.wsproxy.mvc.model;

import com.wsproxy.client.HttpClient;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class HttpRequestTesterModel {
    private SwingPropertyChangeSupport eventEmitter;
    private String errorText = null;
    public HttpRequestTesterModel() {
        eventEmitter = new SwingPropertyChangeSupport(this);
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
        eventEmitter.firePropertyChange("HttpRequestTesterModel.errorText", null, errorText);
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
