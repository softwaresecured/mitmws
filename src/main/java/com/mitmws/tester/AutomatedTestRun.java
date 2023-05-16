package com.mitmws.tester;

import java.io.Serializable;
import java.util.UUID;

/*
    Describes an automated test run
    "Elements", "Tests", "% complete"
 */
public class AutomatedTestRun implements Serializable {
    private int id = -1;
    private String testId = UUID.randomUUID().toString();
    private String testName = "Untitled";
    private long testRunStartTime = 0;
    private long testRunStopTime = 0;
    private TestSequence testSequence = null;
    private int stepCount = 0;
    private int testCount = 0;
    private int pctComplete = 0;
    private int testsCompleted = 0;
    private String status = "STOPPED";
    private boolean reuseConnection = false;
    private boolean continueReplayAfterTestInsertion = true;
    private boolean dryRun = false;
    private double fuzzRatio = 0.1;
    private int fuzzSeedStart = 0;
    private int fuzzSeedEnd = 1000;


    public AutomatedTestRun(int id, String testName, long testRunStartTime, TestSequence testSequence, int stepCount, int testCount, int pctComplete, int testsCompleted, String status, boolean reuseConnection, boolean continueReplayAfterTestInsertion) {
        this.id = id;
        this.testName = testName;
        this.testRunStartTime = testRunStartTime;
        this.testSequence = testSequence;
        this.stepCount = stepCount;
        this.testCount = testCount;
        this.pctComplete = pctComplete;
        this.testsCompleted = testsCompleted;
        this.status = status;
        this.reuseConnection = reuseConnection;
        this.continueReplayAfterTestInsertion = continueReplayAfterTestInsertion;
    }

    public AutomatedTestRun() {

    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public long getTestRunStopTime() {
        return testRunStopTime;
    }

    public void setTestRunStopTime(long testRunStopTime) {
        this.testRunStopTime = testRunStopTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTestsCompleted() {
        return testsCompleted;
    }

    public void setTestsCompleted(int testsCompleted) {
        this.testsCompleted = testsCompleted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isReuseConnection() {
        return reuseConnection;
    }

    public void setReuseConnection(boolean reuseConnection) {
        this.reuseConnection = reuseConnection;
    }

    public boolean isContinueReplayAfterTestInsertion() {
        return continueReplayAfterTestInsertion;
    }

    public void setContinueReplayAfterTestInsertion(boolean continueReplayAfterTestInsertion) {
        this.continueReplayAfterTestInsertion = continueReplayAfterTestInsertion;
    }

    public double getFuzzRatio() {
        return fuzzRatio;
    }

    public void setFuzzRatio(double fuzzRatio) {
        this.fuzzRatio = fuzzRatio;
    }

    public int getFuzzSeedStart() {
        return fuzzSeedStart;
    }

    public void setFuzzSeedStart(int fuzzSeedStart) {
        this.fuzzSeedStart = fuzzSeedStart;
    }

    public int getFuzzSeedEnd() {
        return fuzzSeedEnd;
    }

    public void setFuzzSeedEnd(int fuzzSeedEnd) {
        this.fuzzSeedEnd = fuzzSeedEnd;
    }

    public long getTestRunStartTime() {
        return testRunStartTime;
    }

    public void setTestRunStartTime(long testRunStartTime) {
        this.testRunStartTime = testRunStartTime;
    }

    public String getTestId() {
        return testId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public TestSequence getTestSequence() {
        return testSequence;
    }

    public void setTestSequence(TestSequence testSequence) {
        this.testSequence = testSequence;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public int getTestCount() {
        return testCount;
    }

    public void setTestCount(int testCount) {
        this.testCount = testCount;
    }

    public int getPctComplete() {
        return pctComplete;
    }

    public void setPctComplete(int pctComplete) {
        this.pctComplete = pctComplete;
    }

    public AutomatedTestRun getCopy() {
        AutomatedTestRun testRunCopy = new AutomatedTestRun();
        testRunCopy.setTestRunStartTime(getTestRunStartTime());
        testRunCopy.setTestName(getTestName());
        testRunCopy.setTestSequence(getTestSequence());
        testRunCopy.setStepCount(getStepCount());
        testRunCopy.setTestCount(getTestCount());
        testRunCopy.setTestsCompleted(getTestsCompleted());
        testRunCopy.setReuseConnection(reuseConnection);
        testRunCopy.setContinueReplayAfterTestInsertion(continueReplayAfterTestInsertion);
        return testRunCopy;
    }
}
