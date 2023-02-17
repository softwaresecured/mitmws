package com.wsproxy.anomalydetection;

import com.wsproxy.httpproxy.trafficlogger.WebsocketTrafficRecord;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class DetectedAnomaly implements Serializable {
    private int id = -1;
    private String anomalyId = UUID.randomUUID().toString();
    private int ruleId = -1;
    private String conversationUuid = null;
    private String websocketMsgId = null;
    private int highlightPosStart = 0;
    private int highlightPosEnd = 0;
    private String detector = null;
    private String source = null;
    private String credibility = null;
    private String testName = null;
    private String CWE = null;
    private String title = null;
    private String description = null;
    private String testPayloadHexStr = null;
    private ArrayList<WebsocketTrafficRecord> records = null; // for fuzz results since the entire test isn't saved
    public DetectedAnomaly(int ruleId, String credibility, String source, String CWE, String title, String description ) {
        this.ruleId = ruleId;
        this.credibility = credibility;
        this.source = source;
        this.CWE = CWE;
        this.title = title;
        this.description = description;
    }

    public DetectedAnomaly(int id, String anomalyId, int ruleId, String conversationUuid, String websocketMsgId, int highlightPosStart, int highlightPosEnd, String detector, String source, String credibility, String testName, String CWE, String title, String description) {
        this.id = id;
        this.anomalyId = anomalyId;
        this.ruleId = ruleId;
        this.conversationUuid = conversationUuid;
        this.websocketMsgId = websocketMsgId;
        this.highlightPosStart = highlightPosStart;
        this.highlightPosEnd = highlightPosEnd;
        this.detector = detector;
        this.source = source;
        this.credibility = credibility;
        this.testName = testName;
        this.CWE = CWE;
        this.title = title;
        this.description = description;
    }

    public String getTestPayloadHexStr() {
        return testPayloadHexStr;
    }

    public void setTestPayloadHexStr(String testPayloadHexStr) {
        this.testPayloadHexStr = testPayloadHexStr;
    }

    public ArrayList<WebsocketTrafficRecord> getRecords() {
        return records;
    }

    public void setRecords(ArrayList<WebsocketTrafficRecord> records) {
        this.records = records;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAnomalyId(String anomalyId) {
        this.anomalyId = anomalyId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }

    public String getDetector() {
        return detector;
    }

    public void setDetector(String detector) {
        this.detector = detector;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCredibility() {
        return credibility;
    }

    public void setCredibility(String credibility) {
        this.credibility = credibility;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getCWE() {
        return CWE;
    }

    public void setCWE(String CWE) {
        this.CWE = CWE;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAnomalyId() {
        return anomalyId;
    }

    public String getConversationUuid() {
        return conversationUuid;
    }

    public void setConversationUuid(String conversationUuid) {
        this.conversationUuid = conversationUuid;
    }

    public String getWebsocketMsgId() {
        return websocketMsgId;
    }

    public void setWebsocketMsgId(String websocketMsgId) {
        this.websocketMsgId = websocketMsgId;
    }

    public int getHighlightPosStart() {
        return highlightPosStart;
    }

    public void setHighlightPosStart(int highlightPosStart) {
        this.highlightPosStart = highlightPosStart;
    }

    public int getHighlightPosEnd() {
        return highlightPosEnd;
    }

    public void setHighlightPosEnd(int highlightPosEnd) {
        this.highlightPosEnd = highlightPosEnd;
    }

    @Override
    public String toString() {
        return "DetectedAnomaly{" +
                "id=" + id +
                ", anomalyId='" + anomalyId + '\'' +
                ", ruleId=" + ruleId +
                ", conversationUuid='" + conversationUuid + '\'' +
                ", websocketMsgId='" + websocketMsgId + '\'' +
                ", highlightPosStart=" + highlightPosStart +
                ", highlightPosEnd=" + highlightPosEnd +
                ", detector='" + detector + '\'' +
                ", source='" + source + '\'' +
                ", credibility='" + credibility + '\'' +
                ", testName='" + testName + '\'' +
                ", CWE='" + CWE + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", records=" + records +
                '}';
    }
}
