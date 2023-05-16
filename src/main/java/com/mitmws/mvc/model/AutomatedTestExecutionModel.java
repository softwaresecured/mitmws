package com.mitmws.mvc.model;

import com.mitmws.tester.AutomatedTestRun;
import com.mitmws.tester.TestSequence;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;

public class AutomatedTestExecutionModel {
    private AutomatedTestRun currentTestRun;
    private String status = "STOPPED";
    private SwingPropertyChangeSupport eventEmitter;

    public AutomatedTestExecutionModel() {
        currentTestRun = null;
        eventEmitter = new SwingPropertyChangeSupport(this);
    }

    public AutomatedTestRun getCurrentTestRun() {
        return currentTestRun;
    }

    public void setCurrentTestRun(AutomatedTestRun currentTestRun) {
        this.currentTestRun = currentTestRun;
        eventEmitter.firePropertyChange("AutomatedTestExecutionModel.currentTestRun", null, this.currentTestRun);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        String oldVal = this.status;
        this.status = status;
        eventEmitter.firePropertyChange("AutomatedTestExecutionModel.status", oldVal, this.status);
    }

    /*
        Wrapper for automated test to fire events
    */

    public String getTestId() {
        return currentTestRun.getTestId();
    }

    public String getTestName() {
        return currentTestRun.getTestName();
    }

    public void setTestName(String testName) {
        currentTestRun.setTestName(testName);
        eventEmitter.firePropertyChange("AutomatedTestExecutionModel.testName", null, testName);
    }

    public TestSequence getTestSequence() {
        return currentTestRun.getTestSequence();
    }

    public void setTestSequence(TestSequence testSequence) {
        currentTestRun.setTestSequence(testSequence);
        eventEmitter.firePropertyChange("AutomatedTestExecutionModel.testSequence", null, testSequence);
    }

    public int getStepCount() {
        return currentTestRun.getStepCount();
    }

    public void setStepCount(int stepCount) {
        currentTestRun.setStepCount(stepCount);
        eventEmitter.firePropertyChange("AutomatedTestExecutionModel.stepCount", null, stepCount);
    }

    public int getTestCount() {
        return currentTestRun.getTestCount();
    }

    public void setTestCount(int testCount) {
        currentTestRun.setTestCount(testCount);
        eventEmitter.firePropertyChange("AutomatedTestExecutionModel.testCount", null, testCount);
    }

    public int getPctComplete() {
        return currentTestRun.getPctComplete();
    }

    public void setPctComplete(int pctComplete) {
        currentTestRun.setPctComplete(pctComplete);
        eventEmitter.firePropertyChange("AutomatedTestExecutionModel.pctComplete", null, pctComplete);
    }

    public int getTestsCompleted() {
        return currentTestRun.getTestsCompleted();
    }

    public void setTestsCompleted(int testsCompleted) {
        currentTestRun.setTestsCompleted(testsCompleted);
        eventEmitter.firePropertyChange("AutomatedTestExecutionModel.testsCompleted", null, testsCompleted);
    }

    public SwingPropertyChangeSupport getEventEmitter() {
        return eventEmitter;
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }

}
