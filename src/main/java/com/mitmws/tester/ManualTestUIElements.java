package com.mitmws.tester;

import javax.swing.*;

public class ManualTestUIElements {
    private JTable jtblConversation = null;
    private JTable jtblReplayHistory = null;
    private JTextArea jtxtUpgradeResponse = null;
    private JTable jtblAppLog = null;
    private JButton jbtnRun = null;
    public ManualTestUIElements() {

    }

    public JButton getJbtnRun() {
        return jbtnRun;
    }

    public void setJbtnRun(JButton jbtnRun) {
        this.jbtnRun = jbtnRun;
    }

    public JTable getJtblConversation() {
        return jtblConversation;
    }

    public void setJtblConversation(JTable jtblConversation) {
        this.jtblConversation = jtblConversation;
    }

    public JTable getJtblReplayHistory() {
        return jtblReplayHistory;
    }

    public void setJtblReplayHistory(JTable jtblReplayHistory) {
        this.jtblReplayHistory = jtblReplayHistory;
    }

    public JTextArea getJtxtUpgradeResponse() {
        return jtxtUpgradeResponse;
    }

    public void setJtxtUpgradeResponse(JTextArea jtxtUpgradeResponse) {
        this.jtxtUpgradeResponse = jtxtUpgradeResponse;
    }

    public JTable getJtblAppLog() {
        return jtblAppLog;
    }

    public void setJtblAppLog(JTable jtblAppLog) {
        this.jtblAppLog = jtblAppLog;
    }
}
