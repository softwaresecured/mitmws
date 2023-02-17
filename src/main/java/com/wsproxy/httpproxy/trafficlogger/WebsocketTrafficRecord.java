package com.wsproxy.httpproxy.trafficlogger;

import com.wsproxy.httpproxy.websocket.WebsocketFrame;

import java.awt.*;
import java.io.Serializable;

public class WebsocketTrafficRecord implements Serializable {
    private int id = -1;
    private WebsocketFrame frame = null;
    private Color highlightColour = null;
    private String testName = null;

    private TrafficSource trafficSource;

    public WebsocketTrafficRecord( WebsocketFrame frame ) {
        this.frame = frame;
    }
    public WebsocketTrafficRecord( int id, WebsocketFrame frame ) {
        this.id = id;
        this.frame = frame;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Color getHighlightColour() {
        return highlightColour;
    }

    public void setHighlightColour(Color highlightColour) {
        this.highlightColour = highlightColour;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public WebsocketFrame getFrame() {
        return frame;
    }
    public void setFrame(WebsocketFrame frame) {
        this.frame = frame;
    }

    public TrafficSource getTrafficSource() {
        return trafficSource;
    }

    public void setTrafficSource(TrafficSource trafficSource) {
        this.trafficSource = trafficSource;
    }
}
