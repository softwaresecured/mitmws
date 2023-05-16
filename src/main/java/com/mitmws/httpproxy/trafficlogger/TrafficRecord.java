package com.mitmws.httpproxy.trafficlogger;

import java.awt.*;
import java.io.Serializable;

public class TrafficRecord implements Serializable {
    public long queueTime = 0;
    //private String id = null;
    private int id = -1;
    private int testRunId = -1;
    private String testName = null;
    private TrafficSource trafficSource = null;
    private HttpTrafficRecord httpTrafficRecord = null;
    private WebsocketTrafficRecord websocketTrafficRecord = null;
    private Color highlightColour = Color.WHITE;

    public TrafficRecord( TrafficSource trafficSource ) {
        //id = UUID.randomUUID().toString();
        this.trafficSource = trafficSource;
        queueTime = System.currentTimeMillis();
    }

    public int getTestRunId() {
        return testRunId;
    }

    public void setTestRunId(int testRunId) {
        this.testRunId = testRunId;
    }

    public int getId() {
        return id;
    }
    public String getIdStr() {
        return String.format("%d", id); // TODO
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


    public TrafficSource getTrafficSource() {
        return trafficSource;
    }

    public void setTrafficSource(TrafficSource trafficSource) {
        this.trafficSource = trafficSource;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setQueueTime( long q) {
        queueTime = q;
    }
    public long getQueueTime() {
        return queueTime;
    }

    public HttpTrafficRecord getHttpTrafficRecord() {
        return httpTrafficRecord;
    }

    public void setHttpTrafficRecord(HttpTrafficRecord httpTrafficRecord) {
        this.httpTrafficRecord = httpTrafficRecord;
    }

    public WebsocketTrafficRecord getWebsocketTrafficRecord() {
        return websocketTrafficRecord;
    }

    public void setWebsocketTrafficRecord(WebsocketTrafficRecord websocketTrafficRecord) {
        this.websocketTrafficRecord = websocketTrafficRecord;
    }

    @Override
    public String toString() {
        return "TrafficRecord{" +
                "queueTime=" + queueTime +
                ", id='" + id + '\'' +
                ", httpTrafficRecord=" + httpTrafficRecord +
                ", websocketTrafficRecord=" + websocketTrafficRecord +
                '}';
    }
}
