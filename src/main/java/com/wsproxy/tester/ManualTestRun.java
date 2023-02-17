package com.wsproxy.tester;

import java.io.Serializable;

public class ManualTestRun implements Serializable {
    private int id = -1;
    private String testName = null;
    private TestSequence testSequence = null;
    public ManualTestRun( int id, String testName, TestSequence testSequence ) {
        this.id = id;
        this.testSequence = testSequence;
        this.testName = testName;
    }
    public ManualTestRun(String testName, TestSequence testSequence ) {
        this.testSequence = testSequence;
        this.testName = testName;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTestName() {
        return testName;
    }
    public TestSequence getTestSequence() {
        return testSequence;
    }

    @Override
    public String toString() {
        return "ManualTestRun{" +
                "id=" + id +
                ", testName='" + testName + '\'' +
                ", testSequence=" + testSequence +
                '}';
    }
}
