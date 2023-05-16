package com.mitmws.mvc.model;

import com.mitmws.environment.Environment;

import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.table.DefaultTableModel;
import java.beans.PropertyChangeListener;

public class EnvironmentModel {
    public DefaultTableModel envVarTestTableModel; // results for regex tester
    private DefaultTableModel environmentTableModel;
    private Environment environment;
    private SwingPropertyChangeSupport eventEmitter;
    private EnvironmentVariableModel currentEnvironmentVariable;
    public EnvironmentModel(ProjectModel projectModel) {
        environment = new Environment();
        currentEnvironmentVariable = new EnvironmentVariableModel();
        envVarTestTableModel = new DefaultTableModel();
        for ( String col: new String[] { "Source", "Operation", "Text"}) {
            envVarTestTableModel.addColumn(col);
        }
        environmentTableModel = new DefaultTableModel();
        for ( String col: new String[] { "Enabled", "Type", "Scope","Name"}) {
            environmentTableModel.addColumn(col);
        }
        eventEmitter = new SwingPropertyChangeSupport(this);
    }


    public DefaultTableModel getEnvironmentTableModel() {
        return environmentTableModel;
    }

    public DefaultTableModel getEnvVarTestTableModel() {
        return envVarTestTableModel;
    }

    public EnvironmentVariableModel getCurrentEnvironmentVariable() {
        return currentEnvironmentVariable;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment ) {
        this.environment = environment;
        eventEmitter.firePropertyChange("EnvironmentModel.environment", null, environment);
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
