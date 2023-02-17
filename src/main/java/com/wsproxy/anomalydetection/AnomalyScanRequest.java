package com.wsproxy.anomalydetection;

import java.util.ArrayList;

public class AnomalyScanRequest {
    private int ruleId = 0;
    private ArrayList<String> conversationUuids = new ArrayList<String>();
    private String testName = null;

    /*
        used for active
     */
    public AnomalyScanRequest(int ruleId, ArrayList<String> conversationUuid, String testName) {
        this.ruleId = ruleId;
        this.conversationUuids = conversationUuid;
        this.testName = testName;
    }

    /*
        used for passive
     */
    public AnomalyScanRequest(String conversationUuid) {
        this.ruleId = ruleId;
        conversationUuids.add(conversationUuid);
        this.testName = testName;
    }

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }

    public ArrayList<String> getConversationUuids() {
        return conversationUuids;
    }

    public void setConversationUuids(ArrayList<String> conversationUuids) {
        this.conversationUuids = conversationUuids;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }
}
