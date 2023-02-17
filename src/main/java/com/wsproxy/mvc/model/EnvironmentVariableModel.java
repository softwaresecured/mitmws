package com.wsproxy.mvc.model;

import com.wsproxy.environment.EnvironmentItemScope;
import com.wsproxy.environment.EnvironmentItemType;
import com.wsproxy.environment.EnvironmentVariable;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class EnvironmentVariableModel {
    private EnvironmentVariable environmentVariable;
    private SwingPropertyChangeSupport eventEmitter;
    private ArrayList<String> validationIssues;
    public EnvironmentVariableModel() {
        validationIssues = new ArrayList<>();
        environmentVariable = new EnvironmentVariable();
        eventEmitter = new SwingPropertyChangeSupport(this);
    }

    public ArrayList<String> getValidationIssues() {
        return validationIssues;
    }

    public void setValidationIssues(ArrayList<String> validationIssues) {
        this.validationIssues = validationIssues;
        eventEmitter.firePropertyChange("EnvironmentVariableModel.validationIssues", null, this.validationIssues);
    }

    public EnvironmentVariable getEnvironmentVariable() {
        return environmentVariable;
    }

    public void setEnvironmentVariable(EnvironmentVariable val ) {
        this.environmentVariable = val;
        eventEmitter.firePropertyChange("EnvironmentVariableModel.environmentVariable", null, this.environmentVariable);
    }

    public void setName( String val ) {
        String oldVal = environmentVariable.getName();
        environmentVariable.setName(val);
        eventEmitter.firePropertyChange("EnvironmentVariableModel.name", oldVal, environmentVariable.getName());
    }

    public void setDescription( String val ) {
        String oldVal = environmentVariable.getDescription();
        environmentVariable.setDescription(val);
        eventEmitter.firePropertyChange("EnvironmentVariableModel.description", oldVal, environmentVariable.getDescription());
    }

    public void setEnabled( boolean val ) {
        boolean oldVal = environmentVariable.isEnabled();
        environmentVariable.setEnabled(val);
        eventEmitter.firePropertyChange("EnvironmentVariableModel.enabled", oldVal, environmentVariable.isEnabled());
    }

    public void setEnvironmentItemScope( EnvironmentItemScope val ) {
        EnvironmentItemScope oldVal = environmentVariable.getEnvironmentItemScope();
        environmentVariable.setEnvironmentItemScope(val);
        eventEmitter.firePropertyChange("EnvironmentVariableModel.environmentItemScope", oldVal, environmentVariable.getEnvironmentItemType());
    }

    public void setEnvironmentItemType( EnvironmentItemType val ) {
        EnvironmentItemType oldVal = environmentVariable.getEnvironmentItemType();
        environmentVariable.setEnvironmentItemType(val);
        eventEmitter.firePropertyChange("EnvironmentVariableModel.environmentItemType", oldVal, environmentVariable.getEnvironmentItemType());
    }

    public void setInputRegexMatchGroup( int val ) {
        int oldVal = environmentVariable.getInputRegexMatchGroup();
        environmentVariable.setInputRegexMatchGroup(val);
        eventEmitter.firePropertyChange("EnvironmentVariableModel.inputRegexMatchGroup", oldVal, environmentVariable.getInputRegexMatchGroup());
    }

    public void setOutputRegexMatchGroup( int val ) {
        int oldVal = environmentVariable.getOutputRegexMatchGroup();
        environmentVariable.setOutputRegexMatchGroup(val);
        eventEmitter.firePropertyChange("EnvironmentVariableModel.outputRegexMatchGroup", oldVal, environmentVariable.getOutputRegexMatchGroup());
    }

    public void setInputRegexPattern( Pattern val ) {
        Pattern oldVal = environmentVariable.getInputRegexPattern();
        environmentVariable.setInputRegexPattern(val);
        eventEmitter.firePropertyChange("EnvironmentVariableModel.inputRegexPattern", oldVal, environmentVariable.getInputRegexPattern());
    }

    public void setOutputRegexPattern( Pattern val ) {
        Pattern oldVal = environmentVariable.getOutputRegexPattern();
        environmentVariable.setOutputRegexPattern(val);
        eventEmitter.firePropertyChange("EnvironmentVariableModel.outputRegexPattern", oldVal, environmentVariable.getOutputRegexPattern());
    }

    public void setStoredVariable( String val ) {
        String oldVal = environmentVariable.getStoredVariable();
        environmentVariable.setStoredVariable(val);
        eventEmitter.firePropertyChange("EnvironmentVariableModel.storedVariable", oldVal, environmentVariable.getStoredVariable());
    }

    public void setStringReplacementText( String val ) {
        String oldVal = environmentVariable.getRegexStringReplacementText();
        environmentVariable.setRegexStringReplacementText(val);
        eventEmitter.firePropertyChange("EnvironmentVariableModel.regexStringReplacementText", oldVal, environmentVariable.getRegexStringReplacementText());
    }

    public void setStringReplacementMatchText( String val ) {
        String oldVal = environmentVariable.getStringReplacementMatchText();
        environmentVariable.setStringReplacementMatchText(val);
        eventEmitter.firePropertyChange("EnvironmentVariableModel.stringReplacementMatchText", oldVal, environmentVariable.getStringReplacementMatchText());
    }

    public void setMatchRegexPattern( Pattern val ) {
        Pattern oldVal = environmentVariable.getMatchRegexPattern();
        environmentVariable.setMatchRegexPattern(val);
        eventEmitter.firePropertyChange("EnvironmentVariableModel.matchRegexPattern", oldVal, environmentVariable.getMatchRegexPattern());
    }

    public void setMatchRegexGroup( int val ) {
        int oldVal = environmentVariable.getMatchRegexGroup();
        environmentVariable.setMatchRegexGroup(val);
        eventEmitter.firePropertyChange("EnvironmentVariableModel.matchRegexGroup", oldVal, environmentVariable.getMatchRegexGroup());
    }

    public void setRegexMatchGroupEnabled( boolean val ) {
        boolean oldVal = environmentVariable.getRegexMatchGroupEnabled();
        environmentVariable.setRegexMatchGroupEnabled(val);
        eventEmitter.firePropertyChange("EnvironmentVariableModel.regexMatchGroupEnabled", oldVal, environmentVariable.getRegexMatchGroupEnabled());
    }

    public void setRegexStringReplacementText( String val ) {
        String oldVal = environmentVariable.getRegexStringReplacementText();
        environmentVariable.setRegexStringReplacementText(val);
        eventEmitter.firePropertyChange("EnvironmentVariableModel.regexStringReplacementText", oldVal, environmentVariable.getRegexStringReplacementText());
    }

    public void setScriptMatchRegex( Pattern val ) {
        Pattern oldVal = environmentVariable.getScriptMatchRegex();
        environmentVariable.setScriptMatchRegex(val);
        eventEmitter.firePropertyChange("EnvironmentVariableModel.scriptMatchRegex", oldVal, environmentVariable.getMatchRegexPattern());
    }

    public void setScriptName( String val ) {
        String oldVal = environmentVariable.getScriptName();
        environmentVariable.setScriptName(val);
        eventEmitter.firePropertyChange("EnvironmentVariableModel.scriptName", oldVal, environmentVariable.getScriptName());
    }

    public void setScriptMatchRegexGroup( int val ) {
        int oldVal = environmentVariable.getScriptMatchRegexGroup();
        environmentVariable.setScriptMatchRegexGroup(val);
        eventEmitter.firePropertyChange("EnvironmentVariableModel.scriptMatchRegexGroup", oldVal, environmentVariable.getScriptMatchRegexGroup());
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }

}
