package com.mitmws.tester;

import javax.swing.*;

public class AutomatedTestUIElements {
    private JTable jtblTblTestRuns = null;
    private JButton jbtnRun = null;

    public AutomatedTestUIElements() {

    }

    public JTable getJtblTblTestRuns() {
        return jtblTblTestRuns;
    }

    public void setJtblTblTestRuns(JTable jtblTblTestRuns) {
        this.jtblTblTestRuns = jtblTblTestRuns;
    }

    public JButton getJbtnRun() {
        return jbtnRun;
    }

    public void setJbtnRun(JButton jbtnRun) {
        this.jbtnRun = jbtnRun;
    }
}
