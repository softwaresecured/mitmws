package com.mitmws.tester;

import com.mitmws.util.GuiUtils;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class TestTarget implements Serializable {

    /*
        Test targets are identified by their position in the string
        They are applied before the environment is applied
     */
    private int id = -1;
    private final String targetUUID = UUID.randomUUID().toString();
    private String targetName = "Untitled";
    private boolean enabled = true;
    private int testableStepIdx = -1;
    private int startPos = -1;
    private int endPos = -1;
    private Color highlightColour = Color.WHITE;
    private int testSeqId = -1;
    private ArrayList<PayloadEncoding> payloadEncodings = new ArrayList<>();


    public TestTarget(int id, int testSeqId, String targetName, boolean enabled, int startPos, int endPos, Color highlightColour, ArrayList<PayloadEncoding> payloadEncodings) {
        this.id = id;
        this.testSeqId = testSeqId;
        this.targetName = targetName;
        this.enabled = enabled;
        this.startPos = startPos;
        this.endPos = endPos;
        this.highlightColour = highlightColour;
        this.payloadEncodings = payloadEncodings;
    }

    public TestTarget() {
        highlightColour = GuiUtils.generateColour();
    }
    public TestTarget( int testableStepIdx, int startPos, int endPos ) {
        this.testableStepIdx = testableStepIdx;
        this.startPos = startPos;
        this.endPos = endPos;
        highlightColour = GuiUtils.generateColour();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEncodings(ArrayList<PayloadEncoding> payloadEncodings) {
        this.payloadEncodings = payloadEncodings;
    }

    public ArrayList<PayloadEncoding> getEnabledEncodings() {
        return payloadEncodings;
    }

    public void setHighlightColour( int r, int g, int b ) {
        highlightColour = new Color(r,g,b);
    }

    public Color getHighlightColour() {
        return highlightColour;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getTestableStepIdx() {
        return testableStepIdx;
    }

    public void setTestableStepIdx(int testableStepIdx) {
        this.testableStepIdx = testableStepIdx;
    }

    public int getStartPos() {
        return startPos;
    }

    public void setStartPos(int startPos) {
        this.startPos = startPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }

    public String getTargetUUID() {
        return targetUUID;
    }

    public int getTestSeqId() {
        return testSeqId;
    }

    public void setTestSeqId(int testSeqId) {
        this.testSeqId = testSeqId;
    }

    @Override
    public String toString() {
        return "TestTarget{" +
                "targetId='" + targetUUID + '\'' +
                ", targetName='" + targetName + '\'' +
                ", enabled=" + enabled +
                ", testableStepIdx=" + testableStepIdx +
                ", startPos=" + startPos +
                ", endPos=" + endPos +
                ", highlightColour=" + highlightColour +
                ", payloadEncodings=" + payloadEncodings.toString() +
                '}';
    }
}
