package com.mitmws.mvc.model;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;

public class MainStatusBarModel {
    private int httpServerActiveSessions = 0;
    private int proxyServerActiveSessions = 0;
    private int httpCount = 0;
    private int websocketCount = 0;
    private int websocketManualTestCount = 0;
    private int websocketAutomatedTestCount = 0;
    private int websocketImmediateTestCount = 0;
    private String proxyListenAddr = null;
    private String httpListenAddr = null;
    private String statusMessage = ""; // Used for a status such as loading project, updating rules etc

    private SwingPropertyChangeSupport eventEmitter;

    public MainStatusBarModel() {
        eventEmitter = new SwingPropertyChangeSupport(this);
    }

    public String getProxyListenAddr() {
        return proxyListenAddr;
    }

    public void setProxyListenAddr(String proxyListenAddr) {
        String oldVal = this.proxyListenAddr;
        this.proxyListenAddr = proxyListenAddr;
        eventEmitter.firePropertyChange("MainStatusBarModel.proxyListenAddr", oldVal, this.proxyListenAddr);
    }

    public int getHttpServerActiveSessions() {
        return httpServerActiveSessions;
    }

    public void setHttpServerActiveSessions(int httpServerActiveSessions) {
        int oldVal = this.httpServerActiveSessions;
        this.httpServerActiveSessions = httpServerActiveSessions;
        eventEmitter.firePropertyChange("MainStatusBarModel.httpServerActiveSessions", oldVal, this.httpServerActiveSessions);
    }

    public int getProxyServerActiveSessions() {
        return proxyServerActiveSessions;
    }

    public void setProxyServerActiveSessions(int proxyServerActiveSessions) {
        int oldVal = this.proxyServerActiveSessions;
        this.proxyServerActiveSessions = proxyServerActiveSessions;
        eventEmitter.firePropertyChange("MainStatusBarModel.proxyServerActiveSessions", oldVal, this.proxyServerActiveSessions);
    }

    public String getHttpListenAddr() {
        return httpListenAddr;
    }

    public void setHttpListenAddr(String httpListenAddr) {
        String oldVal = this.httpListenAddr;
        this.httpListenAddr = httpListenAddr;
        eventEmitter.firePropertyChange("MainStatusBarModel.httpListenAddr", oldVal, this.httpListenAddr);
    }

    public void incHttpCount() {
        setHttpCount(getHttpCount()+1);
    }

    public void incWebsocketCount() {
        setWebsocketCount(getWebsocketCount()+1);
    }

    public void incManualTestWebsocketCount() {
        setWebsocketManualTestCount(getWebsocketManualTestCount()+1);
    }

    public void incAutomatedTestWebsocketCount() {
        setWebsocketAutomatedTestCount(getWebsocketAutomatedTestCount()+1);
    }

    public void incImmediateTestWebsocketCount() {
        setWebsocketImmediateTestCount(getWebsocketImmediateTestCount()+1);
    }

    public int getHttpCount() {
        return httpCount;
    }

    public void setHttpCount(int httpCount) {
        int oldVal = this.httpCount;
        this.httpCount = httpCount;
        eventEmitter.firePropertyChange("MainStatusBarModel.httpCount", oldVal, this.httpCount);
    }

    public int getWebsocketCount() {
        return websocketCount;
    }

    public void setWebsocketCount(int websocketCount) {
        int oldVal = this.websocketCount;
        this.websocketCount = websocketCount;
        eventEmitter.firePropertyChange("MainStatusBarModel.websocketCount", oldVal, this.websocketCount);
    }

    public int getWebsocketManualTestCount() {
        return websocketManualTestCount;
    }

    public void setWebsocketManualTestCount(int websocketManualTestCount) {
        int oldVal = this.websocketManualTestCount;
        this.websocketManualTestCount = websocketManualTestCount;
        eventEmitter.firePropertyChange("MainStatusBarModel.websocketManualTestCount", oldVal, this.websocketManualTestCount);
    }

    public int getWebsocketAutomatedTestCount() {
        return websocketAutomatedTestCount;
    }

    public void setWebsocketAutomatedTestCount(int websocketAutomatedTestCount) {
        int oldVal = this.websocketAutomatedTestCount;
        this.websocketAutomatedTestCount = websocketAutomatedTestCount;
        eventEmitter.firePropertyChange("MainStatusBarModel.websocketAutomatedTestCount", oldVal, this.websocketAutomatedTestCount);
    }

    public int getWebsocketImmediateTestCount() {
        return websocketImmediateTestCount;
    }

    public void setWebsocketImmediateTestCount(int websocketImmediateTestCount) {
        int oldVal = this.websocketImmediateTestCount;
        this.websocketImmediateTestCount = websocketImmediateTestCount;
        eventEmitter.firePropertyChange("MainStatusBarModel.websocketImmediateTestCount", oldVal, this.websocketImmediateTestCount);
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        String oldVal = this.statusMessage;
        this.statusMessage = statusMessage;
        eventEmitter.firePropertyChange("MainStatusBarModel.statusMessage", oldVal, this.statusMessage);
    }

    public SwingPropertyChangeSupport getEventEmitter() {
        return eventEmitter;
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }

}
