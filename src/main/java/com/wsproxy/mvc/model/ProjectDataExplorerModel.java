package com.wsproxy.mvc.model;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class ProjectDataExplorerModel {
    private String currentError = null;
    private SwingPropertyChangeSupport eventEmitter;
    public ProjectDataExplorerModel() {
        eventEmitter = new SwingPropertyChangeSupport(this);
    }


    public void setError( String err ) {
        this.currentError = currentError;
        eventEmitter.firePropertyChange("ProjectDataExplorerModel.currentError", null, err);
    }


    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
