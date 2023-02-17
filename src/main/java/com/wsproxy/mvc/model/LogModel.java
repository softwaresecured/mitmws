package com.wsproxy.mvc.model;

import com.wsproxy.logging.AppLog;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class LogModel {
    private String appLog;
    private final int LOG_TAIL_LEN = 1000;
    private final boolean FILTER_CLASS_NAME = true;
    private ArrayList<String> logMessages;
    private SwingPropertyChangeSupport eventEmitter;
    private String lastMessage = null;
    public LogModel() {
        logMessages = new ArrayList<String>();
        eventEmitter = new SwingPropertyChangeSupport(this);
    }

    public String getLogTail() {
        return String.join("\n",logMessages);

    }

    public void addLogMsg(String msg ) {
        if ( FILTER_CLASS_NAME ) {
            msg = msg.replaceAll("\\scom.*?\\s.*?\\s"," ");
        }
        logMessages.add(msg);
        if ( logMessages.size() > LOG_TAIL_LEN ) {
            logMessages.remove(0);
        }
        setLastMessage(msg);
    }

    public void setLastMessage( String lastMessage ) {
        this.lastMessage = lastMessage;
        eventEmitter.firePropertyChange("LogModel.lastMessage", null, lastMessage);
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public ArrayList<String> getLogMessages() {
        return logMessages;
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
