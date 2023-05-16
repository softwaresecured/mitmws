package com.mitmws.mvc.model;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;

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
