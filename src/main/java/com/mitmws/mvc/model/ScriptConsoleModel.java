package com.mitmws.mvc.model;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;

public class ScriptConsoleModel {
    private String scriptContent = "";
    private String executionOutput = "";
    private SwingPropertyChangeSupport eventEmitter;
    public ScriptConsoleModel() {

        eventEmitter = new SwingPropertyChangeSupport(this);
    }

    public String getScriptContent() {
        return scriptContent;
    }

    public void setScriptContent(String scriptContent) {
        this.scriptContent = scriptContent;
        eventEmitter.firePropertyChange("ScriptConsoleModel.scriptContent", null, scriptContent);
    }

    public String getExecutionOutput() {
        return executionOutput;
    }

    public void setExecutionOutput(String executionOutput) {
        this.executionOutput = executionOutput;
        eventEmitter.firePropertyChange("ScriptConsoleModel.executionOutput", null, executionOutput);
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
