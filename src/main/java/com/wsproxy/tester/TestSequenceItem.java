package com.wsproxy.tester;

import com.wsproxy.httpproxy.websocket.WebsocketFrame;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class TestSequenceItem implements Serializable {
    private int id = 0;
    private ArrayList<TestTarget> testTargets = new ArrayList<>();
    private String testId = null;
    private TestSequenceItemType testSequenceItemType = null;
    private int delayMsec = 0;
    private String actionType = null;
    private WebsocketFrame frame = null;
    private int stepOrder = -1;

    public TestSequenceItem( int id, ArrayList<TestTarget> testTargets, String testId, TestSequenceItemType testSequenceItemType, int delayMsec, String actionType, WebsocketFrame frame, int stepOrder) {
        this.id = id;
        this.testTargets = testTargets;
        this.testId = testId;
        this.testSequenceItemType = testSequenceItemType;
        this.delayMsec = delayMsec;
        this.actionType = actionType;
        this.frame = frame;
        this.stepOrder = stepOrder;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(int stepOrder) {
        this.stepOrder = stepOrder;
    }

    public TestSequenceItem() {
        testId = UUID.randomUUID().toString();
    }

    public String getTestId() {
        return testId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public TestSequenceItemType getTestSequenceItemType() {
        return testSequenceItemType;
    }

    public void setTestSequenceItemType(TestSequenceItemType testSequenceItemType) {
        this.testSequenceItemType = testSequenceItemType;
    }

    public int getDelayMsec() {
        return delayMsec;
    }

    public void setDelayMsec(int delayMsec) {
        this.delayMsec = delayMsec;
    }

    public WebsocketFrame getFrame() {
        return frame;
    }

    public void setFrame(WebsocketFrame frame) {
        this.frame = frame;
    }

    public ArrayList<TestTarget> getTestTargets() {
        return testTargets;
    }

    public void setTestTargets(ArrayList<TestTarget> testTargets) {
        this.testTargets = testTargets;
    }

    @Override
    public String toString() {
        return "TestSequenceItem{" +
                "id=" + id +
                ", testTargets=" + testTargets +
                ", testId='" + testId + '\'' +
                ", testSequenceItemType=" + testSequenceItemType +
                ", delayMsec=" + delayMsec +
                ", actionType='" + actionType + '\'' +
                ", frame=" + frame +
                ", stepOrder=" + stepOrder +
                '}';
    }
}
