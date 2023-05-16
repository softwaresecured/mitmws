package com.mitmws.mvc.controller;

import com.mitmws.anomalydetection.DetectionRule;
import com.mitmws.httpproxy.HttpMessage;
import com.mitmws.httpproxy.websocket.WebsocketFrameType;
import com.mitmws.mvc.model.MainModel;
import com.mitmws.projects.ProjectDataServiceException;
import com.mitmws.util.GuiUtils;
import com.mitmws.httpproxy.trafficlogger.*;
import com.mitmws.logging.AppLog;
import com.mitmws.mvc.model.AutomatedTesterModel;
import com.mitmws.mvc.model.ProjectModel;
import com.mitmws.mvc.thread.AutomatedTestManagerActivityThread;
import com.mitmws.mvc.thread.AutomatedTestManagerCleanupThread;
import com.mitmws.mvc.view.panels.automatedtester.PnlAutomatedTesterView;
import com.mitmws.tester.*;

import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

public class AutomatedTesterController implements PropertyChangeListener {
    private MainModel mainModel;
    private AutomatedTesterModel automatedTesterModel;
    private ProjectModel projectModel;
    private TrafficLogger trafficLogger;
    private ArrayList<AutomatedTestManagerActivityThread> testManagerWorkers;
    private AutomatedTestManagerCleanupThread cleanupThread;
    private PnlAutomatedTesterView pnlAutomatedTesterView;
    private static Logger LOGGER = AppLog.getLogger(PnlAutomatedTesterView.class.getName());
    public AutomatedTesterController(MainModel mainModel,TrafficLogger trafficLogger, PnlAutomatedTesterView pnlAutomatedTesterView) {
        testManagerWorkers = new ArrayList<AutomatedTestManagerActivityThread>();
        this.mainModel = mainModel;
        this.automatedTesterModel = mainModel.getAutomatedTesterModel();
        this.automatedTesterModel.addListener(this);
        this.automatedTesterModel.getAutomatedTestExecutionModel().addListener(this);
        this.projectModel = mainModel.getProjectModel();
        this.trafficLogger = trafficLogger;
        this.pnlAutomatedTesterView = pnlAutomatedTesterView;
        syncTestRunsTable();
        initEventListeners();
        cleanupStart();
        projectModel.addListener(this);
        mainModel.getRulesModel().addListener(this);
        mainModel.getPayloadsModel().addListener(this);
        debug(Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    public void debug(String str) {
        //System.out.println(String.format("DEBUG: %s", str));
    }

    // Start / stop the cleanup thread for automated tester activity thread
    public void cleanupStart() {
        debug(Thread.currentThread().getStackTrace()[1].getMethodName());
        cleanupThread = new AutomatedTestManagerCleanupThread(automatedTesterModel,testManagerWorkers);
        cleanupThread.start();
    }

    public void cleanupStop() {
        debug(Thread.currentThread().getStackTrace()[1].getMethodName());
        cleanupThread.shutdown();
        try {
            cleanupThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Start / stop a test
    public void runTest(AutomatedTestRun testRun) {
        debug(Thread.currentThread().getStackTrace()[1].getMethodName());
        AutomatedTestManagerActivityThread thread = new AutomatedTestManagerActivityThread(
                automatedTesterModel,
                mainModel.getInteractshModel(),
                projectModel,
                trafficLogger,
                testRun,
                mainModel.getRulesModel().getActiveRules().getRules(),
                mainModel.getPayloadsModel().getPayloadLibrary());
        testManagerWorkers.add(thread);
        thread.start();
    }

    public void stopTest() {
        debug(Thread.currentThread().getStackTrace()[1].getMethodName());
        for ( AutomatedTestManagerActivityThread worker : testManagerWorkers ) {
            worker.shutdown();
        }
    }

    public void resetUi() {
        debug(Thread.currentThread().getStackTrace()[1].getMethodName());
        pnlAutomatedTesterView.pnlAutomatedTesterTestOutput.resetUi();
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.resetUi();
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterToolbar.resetUi();
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTesterTargetsToolbar.resetUi();
    }


    /*
        Loads the test traffic for the currently selected test run
    */
    public void loadTestTraffic() {
        debug(Thread.currentThread().getStackTrace()[1].getMethodName());
        if ( automatedTesterModel.getTestSequence() != null ) {
            automatedTesterModel.getTestTrafficModel().setRowCount(0);
            try {
                String testName = automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun().getTestName();
                ArrayList<WebsocketTrafficRecord> records = projectModel.getProjectDataService().getWebsocketTrafficRecordByTestName(testName);
                for ( WebsocketTrafficRecord rec : records ) {
                    automatedTesterModel.addWebsocketTraffic(testName,rec.getFrame(),rec.getHighlightColour());
                }
                GuiUtils.tableSelectFirst(pnlAutomatedTesterView.pnlAutomatedTesterTestOutput.jtblTestTraffic);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ProjectDataServiceException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Loads the current test run & the config for it
    */
    public void loadTestRunById( String id ) {
        debug(Thread.currentThread().getStackTrace()[1].getMethodName());
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterToolbar.jlblURL.setText("");
        for ( AutomatedTestRun testRun : projectModel.getAutomatedTestRuns() ) {
            if ( testRun.getTestId().equals(id)) {
                automatedTesterModel.getAutomatedTestExecutionModel().setCurrentTestRun(testRun);
                automatedTesterModel.setTestSequence(testRun.getTestSequence());
                try {
                    HttpMessage msg = testRun.getTestSequence().getTestHttpMessage();
                    if ( msg != null ) {
                        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterToolbar.jlblURL.setText(msg.getUrl());
                    }
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
                loadTestTraffic();
                break;
            }
        }
    }

    /*
        Create a new test run based on the supplied test sequence
     */
    public void createFromManualTest(TestSequence testSequence, String testRunName ) {
        debug(Thread.currentThread().getStackTrace()[1].getMethodName());
        AutomatedTestRun testRun = new AutomatedTestRun();
        try {
            testRun.setTestName(projectModel.getProjectDataService().getNextTestRunName(testRunName));
        } catch (ProjectDataServiceException e) {
            e.printStackTrace();
        }
        testRun.setTestSequence(testSequence);
        testRun.getTestSequence().setTestTargets(new ArrayList<>());
        projectModel.getAutomatedTestRuns().add(testRun);
        loadTestRunById(testRun.getTestId());
        syncTestRunsTable();
        selectTargetById(testRun.getTestId());
        GuiUtils.tableSelectFirst(pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets);
        projectModel.save();
        GuiUtils.tableSelectLast(pnlAutomatedTesterView.pnlAutomatedTesterTestOutput.jtblTestRuns);
    }


    public void updatePayloadEncodings() {
        debug(Thread.currentThread().getStackTrace()[1].getMethodName());
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlPayloadEncodings.jchkEncodingBase64.setSelected(false);
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlPayloadEncodings.jchkEncodingHtml.setSelected(false);
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlPayloadEncodings.jchkEncodingJavascript.setSelected(false);
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlPayloadEncodings.jchkEncodingURL.setSelected(false);
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlPayloadEncodings.jchkEncodingXml.setSelected(false);

        if ( automatedTesterModel.getCurrentTestTarget() != null ) {
            for( PayloadEncoding payloadEncoding : automatedTesterModel.getCurrentTestTarget().getEnabledEncodings()) {
                if ( payloadEncoding.equals(PayloadEncoding.BASE64) ) {
                    pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlPayloadEncodings.jchkEncodingBase64.setSelected(true);
                }

                if ( payloadEncoding.equals(PayloadEncoding.HTML) ) {
                    pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlPayloadEncodings.jchkEncodingHtml.setSelected(true);
                }

                if ( payloadEncoding.equals(PayloadEncoding.JAVASCRIPT) ) {
                    pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlPayloadEncodings.jchkEncodingJavascript.setSelected(true);
                }

                if ( payloadEncoding.equals(PayloadEncoding.URL) ) {
                    pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlPayloadEncodings.jchkEncodingURL.setSelected(true);
                }

                if ( payloadEncoding.equals(PayloadEncoding.XML) ) {
                    pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlPayloadEncodings.jchkEncodingXml.setSelected(true);
                }
            }
        }
    }

    /*
        Gets the currently selected target id
     */
    public String getSelectedTargetId() {
        String tid = null;
        JTable tbl = pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets;
        if ( tbl.getSelectedRow() >= 0 ) {
            tid = (String) tbl.getValueAt(tbl.getSelectedRow(),0);
        }
        return tid;
    }

    /*
        Selects the row in the targets jtable where the targetId matches
     */
    public void selectTargetById( String targetId ) {
        JTable tbl = pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets;
        debug(Thread.currentThread().getStackTrace()[1].getMethodName());
        if ( tbl.getRowCount() > 0 ) {
            for ( int i = 0; i < tbl.getRowCount(); i++ ) {
                String tid = (String) tbl.getValueAt(i,0);
                if ( tid.equals(targetId )) {
                    if ( tbl.getSelectedRow() != i ) {
                        tbl.getSelectionModel().setSelectionInterval(i, i);
                    }
                    break;
                }
            }
        }
    }
    public void loadTargetById( String targetId ) {
        debug(Thread.currentThread().getStackTrace()[1].getMethodName());
        for ( TestTarget target : automatedTesterModel.getTestSequence().getTestTargets() ) {
            if ( target.getTargetUUID().equals(targetId)) {
                automatedTesterModel.setCurrentTestTarget(target);
                break;
            }
        }
    }

    public void resetSelections() {
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubject.select(0,0);
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHexStr.select(0,0);
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubject.setSelectedTextColor(Color.BLACK);
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHexStr.setSelectedTextColor(Color.BLACK);
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubject.setSelectionColor(Color.WHITE);
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHexStr.setSelectionColor(Color.WHITE);
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubject.revalidate();
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHexStr.revalidate();
    }

    public void updateTargetTextHighlighting() {
        resetSelections();
        String curHex = GuiUtils.getHexEditorContent(pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHex.getText());
        if ( pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets.getSelectedRow() >= 0 ) {
            if ( automatedTesterModel.getCurrentTestTarget() != null ) {
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHex.setText(GuiUtils.getHexEditorDocument(curHex,automatedTesterModel.getCurrentTestTarget().getStartPos(),automatedTesterModel.getCurrentTestTarget().getEndPos(),automatedTesterModel.getCurrentTestTarget().getHighlightColour()));
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubject.requestFocus();
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubject.setSelectionStart(automatedTesterModel.getCurrentTestTarget().getStartPos());
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubject.setSelectionEnd(automatedTesterModel.getCurrentTestTarget().getEndPos());
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubject.setSelectionColor(automatedTesterModel.getCurrentTestTarget().getHighlightColour());
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHexStr.requestFocus();
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHexStr.setSelectionStart(automatedTesterModel.getCurrentTestTarget().getStartPos());
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHexStr.setSelectionEnd(automatedTesterModel.getCurrentTestTarget().getEndPos());
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHexStr.setSelectionColor(automatedTesterModel.getCurrentTestTarget().getHighlightColour());

                if ( automatedTesterModel.getCurrentTestTarget() != null ) {
                    curHex = GuiUtils.getHexEditorContent(pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHex.getText());
                    pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHex.setText(GuiUtils.getHexEditorDocument(curHex,automatedTesterModel.getCurrentTestTarget().getStartPos(),automatedTesterModel.getCurrentTestTarget().getEndPos(),automatedTesterModel.getCurrentTestTarget().getHighlightColour()));
                }
            }
        }
    }

    public void onClickEncodingCheckBox(ActionEvent actionEvent, PayloadEncoding encoding) {
        debug(Thread.currentThread().getStackTrace()[1].getMethodName());
        ArrayList<PayloadEncoding> encodings = automatedTesterModel.getCurrentTestTarget().getEnabledEncodings();
        if (((JCheckBox) actionEvent.getSource()).isSelected()) {
            if (!encodings.contains(encoding)) encodings.add(encoding);
        } else {
            encodings.remove(encoding);
        }
        automatedTesterModel.setCurrentTestTargetEncodings(encodings);
        updateTargetTextHighlighting();
    }

    /*
        Clear the test run table and make it reflect the runs in the project model
     */
    public void syncTestRunsTable() {
        debug(Thread.currentThread().getStackTrace()[1].getMethodName());
        automatedTesterModel.getRunsTableModel().setRowCount(0);
        // "testRunId", "Time","Test name","Elements", "Tests", "% complete", "Status"
        for ( AutomatedTestRun testRun : projectModel.getAutomatedTestRuns() ) {
            automatedTesterModel.getRunsTableModel().addRow(new Object[] {
                    testRun.getTestId(),
                    GuiUtils.trafficTimeFmt.format(new Date(testRun.getTestRunStartTime())),
                    testRun.getTestName(),
                    String.format("%d/%d", testRun.getTestsCompleted(),testRun.getTestCount()),
                    String.format("%d%%", testRun.getPctComplete()),
                    testRun.getStatus()
            });
        }
        if (automatedTesterModel.getRunsTableModel().getRowCount() > 0 ) {
            pnlAutomatedTesterView.pnlAutomatedTesterTestOutput.jtblTestRuns.getSelectionModel().setSelectionInterval(0,0);
        }
    }

    public void updateTestSummary() {
        if ( automatedTesterModel.getTestSequence() != null ) {
            pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterToolbar.updateTestSummary(
                    automatedTesterModel.getTestSequence().getTestCount(automatedTesterModel.getTotalPayloadCount()),
                    automatedTesterModel.getTestSequence().getStepCount(automatedTesterModel.getTotalPayloadCount()),
                    automatedTesterModel.getTestSequence().getEtaSec(automatedTesterModel.getTotalPayloadCount()));
        }
    }

    public void updateTotalPayloadCount() {
        int fuzzRange = 0;
        if ( automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun() != null ) {
            fuzzRange = automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun().getFuzzSeedEnd()-automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun().getFuzzSeedStart();
        }
        int payloadCount = mainModel.getRulesModel().getEnabledPayloadCount() + mainModel.getPayloadsModel().getEnabledPayloadCount();
        // add in FUZZ-PAYLOAD rules
        for ( int ruleId : mainModel.getRulesModel().getActiveRules().getRules().keySet() ) {
            DetectionRule rule = mainModel.getRulesModel().getActiveRules().getRules().get(ruleId);
            try {
                if ( rule.isEnabled() && rule.getActiveRuleType().equals("PAYLOAD-FUZZ")) {
                    payloadCount += fuzzRange;
                }
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
        automatedTesterModel.setTotalPayloadCount(payloadCount);
    }


    public void saveAutomatedTestRun(AutomatedTestRun automatedTestRun) {
        debug(Thread.currentThread().getStackTrace()[1].getMethodName());
        // save everything related to this test
        try {
            projectModel.getProjectDataService().saveAutomatedTestRun(automatedTestRun);
            // Associate the test targets with test sequences
            for ( TestTarget testTarget : automatedTestRun.getTestSequence().getTestTargets() ) {
                if ( testTarget.getTestableStepIdx() >= 0 && testTarget.getTestableStepIdx() < automatedTestRun.getTestSequence().getTestSequenceItems().size()) {
                    testTarget.setTestSeqId(automatedTestRun.getTestSequence().getTestSequenceItems().get(testTarget.getTestableStepIdx()).getId());
                }
            }
            for ( TestTarget testTarget : automatedTestRun.getTestSequence().getTestTargets() ) {
                if ( testTarget.getTestSeqId() > 0 ) {
                    projectModel.getProjectDataService().saveTestTarget(testTarget, testTarget.getTestSeqId());
                }
            }
        } catch (ProjectDataServiceException e) {
            e.printStackTrace();
        }
    }

    public void initEventListeners() {
        debug(Thread.currentThread().getStackTrace()[1].getMethodName());
        /*
            Settings at the bottom
         */
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTestConfig.jchkReuseConnection.addActionListener(actionEvent -> {
            if( automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun() != null ) {
                automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun().setReuseConnection(pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTestConfig.jchkReuseConnection.isSelected());
                //testLibrary.saveAll();
            }
        });
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTestConfig.jchkContinueReplayAfterTestInsertion.addActionListener(actionEvent -> {
            if( automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun() != null ) {
                automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun().setContinueReplayAfterTestInsertion(pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTestConfig.jchkContinueReplayAfterTestInsertion.isSelected());
                //testLibrary.saveAll();
            }
        });

        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTestConfig.jspnFuzzSeedStart.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if ( automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun() != null ) {
                    int start = (int) pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTestConfig.jspnFuzzSeedStart.getValue();
                    automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun().setFuzzSeedStart(start);
                    updateTestSummary();
                }
            }
        });

        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTestConfig.jspnFuzzSeedEnd.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if ( automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun() != null ) {
                    int end = (int) pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTestConfig.jspnFuzzSeedEnd.getValue();
                    automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun().setFuzzSeedEnd(end);
                    updateTestSummary();
                }
            }
        });

        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTestConfig.jspnRatio.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if ( automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun() != null ) {
                    double ratio = (double) pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTestConfig.jspnFuzzSeedStart.getValue();
                    automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun().setFuzzRatio(ratio);
                }
            }
        });
        /*
            Run the current test
         */
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterToolbar.jbtnRun.addActionListener(actionEvent -> {
            if ( !automatedTesterModel.getAutomatedTestExecutionModel().getStatus().equals("STARTED")) {
                GuiUtils.clearTable(pnlAutomatedTesterView.pnlAutomatedTesterTestOutput.jtblTestTraffic);
                projectModel.save();
                saveAutomatedTestRun(automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun());
                if ( automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun().getPctComplete() < 100 ) {
                    automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun().setDryRun(pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterToolbar.jchkDryRun.isSelected());
                    runTest(automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun());
                    LOGGER.info(String.format("Started automated test: %s", automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun().getTestName()));
                }
                else {
                    AutomatedTestRun newTestRun = automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun().getCopy();
                    newTestRun.setDryRun(pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterToolbar.jchkDryRun.isSelected());
                    String testRunName = null;
                    try {
                        testRunName = projectModel.getProjectDataService().getNextTestRunName(
                                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterToolbar.jtxtTestName.getText()
                        );
                    } catch (ProjectDataServiceException e) {
                        e.printStackTrace();
                    }
                    newTestRun.setTestName(testRunName);
                    projectModel.getAutomatedTestRuns().add(newTestRun);
                    syncTestRunsTable();
                    loadTestRunById(newTestRun.getTestId());
                    int lastRow = pnlAutomatedTesterView.pnlAutomatedTesterTestOutput.jtblTestRuns.getRowCount();
                    lastRow-= 1;
                    if ( lastRow >= 0 ) {
                        pnlAutomatedTesterView.pnlAutomatedTesterTestOutput.jtblTestRuns.setRowSelectionInterval(lastRow,lastRow);
                    }
                    saveAutomatedTestRun(newTestRun);
                    runTest(newTestRun);
                    LOGGER.info(String.format("Started automated test: %s", newTestRun.getTestName()));
                }
            }
            else {
                stopTest();
            }
        });

        /*
            Delete the current test
         */
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterToolbar.jbtnDelete.addActionListener(actionEvent -> {
            if ( automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun() != null ) {
                int rowId = pnlAutomatedTesterView.pnlAutomatedTesterTestOutput.jtblTestRuns.getRowCount();
                try {
                    projectModel.getProjectDataService().deleteTestRun(automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun().getId());
                } catch (ProjectDataServiceException e) {
                    e.printStackTrace(); // TODO
                }
                //testLibrary.saveAll();
                int selectedIdx = pnlAutomatedTesterView.pnlAutomatedTesterTestOutput.jtblTestRuns.getRowCount()-1;
                syncTestRunsTable();
                if ( rowId < pnlAutomatedTesterView.pnlAutomatedTesterTestOutput.jtblTestRuns.getRowCount() ) {
                    selectedIdx = rowId;
                }

                if ( selectedIdx >= 0 ) {
                    pnlAutomatedTesterView.pnlAutomatedTesterTestOutput.jtblTestRuns.getSelectionModel().setSelectionInterval(selectedIdx, selectedIdx);
                }
            }
        });

        /*
            Test run table ( top right ) row click
         */
        pnlAutomatedTesterView.pnlAutomatedTesterTestOutput.jtblTestRuns.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            resetSelections();
            automatedTesterModel.setCurrentTestTarget(null);
            int rowId = pnlAutomatedTesterView.pnlAutomatedTesterTestOutput.jtblTestRuns.getSelectedRow();
            if ( rowId >= 0 ) {
                String testId = (String) pnlAutomatedTesterView.pnlAutomatedTesterTestOutput.jtblTestRuns.getValueAt(rowId,0);
                loadTestRunById(testId);
                GuiUtils.tableSelectFirst(pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets);
                GuiUtils.tableSelectFirst(pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblConversation);
            }
        });

        /*
            Test traffic click
         */
        pnlAutomatedTesterView.pnlAutomatedTesterTestOutput.jtblTestTraffic.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            int rowId = pnlAutomatedTesterView.pnlAutomatedTesterTestOutput.jtblTestTraffic.getSelectedRow();
            if ( rowId >= 0 ) {
                String messageId = (String) pnlAutomatedTesterView.pnlAutomatedTesterTestOutput.jtblTestTraffic.getValueAt(rowId, 0);
                if ( messageId != null ) {
                    try {
                        WebsocketTrafficRecord rec = null;
                        rec = projectModel.getProjectDataService().getWebsocketTrafficRecordByUUID(messageId);
                        if ( rec != null ) {
                            String maskStr = "--";
                            if ( rec.getFrame().getMaskKey() != null ) {
                                maskStr = Integer.toHexString(ByteBuffer.wrap(rec.getFrame().getMaskKey()).getInt());
                            }
                            String frameStr = String.format("F:%d/R1:%d/R2:%d/R3:%d/OP:%s/M:%s",
                                    rec.getFrame().getFin(),
                                    rec.getFrame().getRsv1(),
                                    rec.getFrame().getRsv2(),
                                    rec.getFrame().getRsv3(),
                                    rec.getFrame().getOpcode().toString(),
                                    maskStr);
                            pnlAutomatedTesterView.pnlAutomatedTesterTestOutput.pnlFrameViewer.pnlWebsocketFrameViewerToolbar.jlblFrameProperties.setText(frameStr);
                            if ( rec.getFrame().getPayload() != null ) {
                                pnlAutomatedTesterView.pnlAutomatedTesterTestOutput.getWebsocketFrameController().getWebsocketFrameModel().setWebsocketFrame(rec.getFrame());
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ProjectDataServiceException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        /*
            Auto target button
         */
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTesterTargetsToolbar.jbtnAutoTarget.addActionListener(actionEvent -> {

            TargetLocator targetLocator = new TargetLocator();
            ArrayList<TestTarget> targets = targetLocator.getAllTargets(pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubject.getText());
            for ( TestTarget curTarget : targets ) {
                curTarget.setTestableStepIdx(pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblConversation.getSelectedRow());
                loadTargetById(curTarget.getTargetUUID());
                selectTargetById(curTarget.getTargetUUID());

                if ( curTarget.getTargetName().startsWith("json")) {
                    if ( !curTarget.getEnabledEncodings().contains(PayloadEncoding.JAVASCRIPT)) {
                        curTarget.getEnabledEncodings().add(PayloadEncoding.JAVASCRIPT);
                    }
                }

            }
            automatedTesterModel.getTestSequence().getTestTargets().addAll(targets);
            automatedTesterModel.syncTargetsTable();
            GuiUtils.tableSelectFirst(pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets);
        });

        /*
            New target
         */
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTesterTargetsToolbar.jbtnNew.addActionListener(actionEvent -> {
            TestTarget newTarget = new TestTarget();
            newTarget.getTargetUUID();
            newTarget.setTestableStepIdx(pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblConversation.getSelectedRow());
            if ( automatedTesterModel.getTestSequence() != null ) {
                automatedTesterModel.getTestSequence().getTestTargets().add(newTarget); // add to the model
                projectModel.save(); // save the project
                automatedTesterModel.syncTargetsTable(); // sync the targets table
                selectTargetById(newTarget.getTargetUUID());
            }
        });

        /*
            Delete target
         */
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTesterTargetsToolbar.jbtnDelete.addActionListener(actionEvent -> {
            int curRowId = pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets.getSelectedRow();
            if ( curRowId >= 0 ) {
                String selectedTid = (String) pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets.getValueAt(curRowId,0);
                for ( int i = 0; i < automatedTesterModel.getTestSequence().getTestTargets().size(); i++ ) {
                    if ( automatedTesterModel.getTestSequence().getTestTargets().get(i).getTargetUUID().equals(selectedTid)) {
                        automatedTesterModel.getTestSequence().getTestTargets().remove(i);
                        projectModel.save();
                        break;
                    }
                }
            }
            automatedTesterModel.syncTargetsTable();

            if ( curRowId < pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets.getRowCount()) {
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets.getSelectionModel().setSelectionInterval(curRowId,curRowId);
            }
            else {
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets.getSelectionModel().setSelectionInterval(curRowId-1,curRowId-1);
            }

            if ( pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets.getSelectedRow() >= 0 ) {
                loadTargetById(pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets.getValueAt(pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets.getSelectedRow(),0).toString());

            }
            updateTargetTextHighlighting();
        });

        /*
            Test conversation item table row select
         */
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblConversation.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            int rowId = pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblConversation.getSelectedRow();
            resetSelections();
            if ( rowId >= 0 ) {
                WebsocketFrameType targetType = (WebsocketFrameType) pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblConversation.getValueAt(rowId, 0);

                // Set the editor type
                // TODO
                if ( targetType.toString().equals("BINARY") || targetType.toString().equals("CONTINUATION")) {
                    pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlTextViewer.setVisible(false);
                    pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlBinaryViewer.setVisible(true);
                }
                else {
                    pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlTextViewer.setVisible(true);
                    pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlBinaryViewer.setVisible(false);
                }

                ArrayList<TestSequenceItem> testableItems = automatedTesterModel.getTestSequence().getTestableSequenceItems();
                String payloadText = new String(testableItems.get(rowId).getFrame().getPayloadUnmasked());
                String payloadHex = GuiUtils.binToHexStr(testableItems.get(rowId).getFrame().getPayloadUnmasked());
                String payloadHexStr = GuiUtils.getBinPreviewStr(testableItems.get(rowId).getFrame().getPayloadUnmasked());
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubject.setText(payloadText);
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHex.setText(GuiUtils.getHexEditorDocument(payloadHex,0,0,Color.WHITE));
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHexStr.setText(payloadHexStr);
                updateTargetTableRowFilter();
                if ( pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets.getSelectedRow() < 0 ) {
                    GuiUtils.tableSelectFirst(pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets);
                }
                updateTargetTextHighlighting();
            }
        });

        /*
            Target subject text selection ( text )
         */
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubject.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                JTextArea txt = pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubject;
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTesterTargetsToolbar.updateTargetSelectionText(
                        txt.getCaretPosition(),
                        txt.getText().length(),
                        txt.getSelectionStart(),
                        txt.getSelectionEnd());
                if ( txt.getCaret().getDot() != txt.getCaret().getMark()) {
                    automatedTesterModel.setCurrentTestTargetSelectionRange(txt.getSelectionStart(),txt.getSelectionEnd());
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        /*
            Target subject text selection ( hex )
         */
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHexStr.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTesterTargetsToolbar.updateTargetSelectionText(
                        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHexStr.getCaretPosition(),
                        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHexStr.getText().length(),
                        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHexStr.getSelectionStart(),
                        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHexStr.getSelectionEnd());

                int startPos = pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHexStr.getSelectionStart();
                int endPos = pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHexStr.getSelectionEnd();

                automatedTesterModel.setCurrentTestTargetSelectionRange(
                        startPos,
                        endPos
                );
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        /*
            Targets table select
         */
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            int rowId = pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets.getSelectedRow();
            resetSelections();
            if (rowId >= 0) {
                String targetId = (String) pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets.getValueAt(rowId, 0);
                loadTargetById(targetId);
                updateTargetTextHighlighting();
            }
        });

        /*
            Target name
         */
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTesterTargetsToolbar.jtxtTargetName.getDocument().addDocumentListener(new DocumentListener() {
            private void updateDocument() {
                Runnable updateDoc = new Runnable() {
                    @Override
                    public void run() {
                        if (automatedTesterModel.getCurrentTestTarget() != null) {
                            automatedTesterModel.setCurrentTestTargetName(pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTesterTargetsToolbar.jtxtTargetName.getText());
                        }
                    }
                };
                SwingUtilities.invokeLater(updateDoc);
            }
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                updateDocument();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                updateDocument();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {

            }
        });

        /*
            Payload encoding checkboxes
         */
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlPayloadEncodings.jchkEncodingJavascript.addActionListener(actionEvent -> {
            onClickEncodingCheckBox(actionEvent, PayloadEncoding.JAVASCRIPT);
        });
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlPayloadEncodings.jchkEncodingXml.addActionListener(actionEvent -> {
            onClickEncodingCheckBox(actionEvent, PayloadEncoding.XML);
        });
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlPayloadEncodings.jchkEncodingURL.addActionListener(actionEvent -> {
            onClickEncodingCheckBox(actionEvent, PayloadEncoding.URL);
        });
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlPayloadEncodings.jchkEncodingHtml.addActionListener(actionEvent -> {
            onClickEncodingCheckBox(actionEvent, PayloadEncoding.HTML);

        });
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlPayloadEncodings.jchkEncodingBase64.addActionListener(actionEvent -> {
            onClickEncodingCheckBox(actionEvent, PayloadEncoding.BASE64);
        });

        /*
            Dry run toggle
         */

        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterToolbar.jchkDryRun.addActionListener( actionEvent -> {
            if ( pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterToolbar.jchkDryRun.isSelected() ) {
                automatedTesterModel.setTotalPayloadCount(1);
            }
            else {
                updateTotalPayloadCount();
            }
        });
        // Button/comp enable/disable
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                // update model if toggled, call sync
                /*
                if ( e.getFirstRow() >= 0 && pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets.getRowCount() > 0) {
                    String tid = (String) pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets.getValueAt(e.getFirstRow(),0);
                    if ( tid != null ) {
                        TestTarget curTestTarget = automatedTesterModel.getTestSequence().getTestTargetById(tid);
                        if ( curTestTarget != null ) {
                            boolean enabled = (boolean) pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets.getValueAt(e.getFirstRow(),2);
                            curTestTarget.setEnabled(enabled);
                        }
                    }
                }
                */
                // enable / disable components
                boolean enabled = pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets.getRowCount() > 0;
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTesterTargetsToolbar.jbtnDelete.setEnabled(enabled);
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterToolbar.jbtnDelete.setEnabled(enabled);
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTesterTargetsToolbar.jtxtTargetName.setEnabled(enabled);
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubject.setEnabled(enabled);
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHex.setEnabled(enabled);
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubjectHexStr.setEnabled(enabled);
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlPayloadEncodings.jchkEncodingBase64.setEnabled(enabled);
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlPayloadEncodings.jchkEncodingHtml.setEnabled(enabled);
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlPayloadEncodings.jchkEncodingURL.setEnabled(enabled);
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlPayloadEncodings.jchkEncodingXml.setEnabled(enabled);
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlPayloadEncodings.jchkEncodingJavascript.setEnabled(enabled);
                updateTestSummary();
            }
        });
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblConversation.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                boolean enabled = pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblConversation.getRowCount() > 0;
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTesterTargetsToolbar.jbtnAutoTarget.setEnabled(enabled);
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTesterTargetsToolbar.jbtnNew.setEnabled(enabled);
            }
        });
        pnlAutomatedTesterView.pnlAutomatedTesterTestOutput.jtblTestRuns.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                boolean enabled = pnlAutomatedTesterView.pnlAutomatedTesterTestOutput.jtblTestRuns.getRowCount() > 0;
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterToolbar.jbtnRun.setEnabled(enabled);
            }
        });
    }

    public void updateTargetTableRowFilter() {
        TableRowSorter sorter = new TableRowSorter<>((DefaultTableModel) pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets.getModel());
        RowFilter<DefaultTableModel, Object> rf = null;
        try {
            ArrayList<RowFilter<Object,Object>> rfs = new ArrayList<>();
            rfs.add(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL,pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblConversation.getSelectedRow(), 4));
            rf = RowFilter.andFilter(rfs);
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        sorter.setRowFilter(rf);
        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblTargets.setRowSorter(sorter);
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

        // Change events from the rules / payloads models
        if ( "RulesModel.enabledRuleCount".equals(propertyChangeEvent.getPropertyName())) {
            debug(String.format("Event - %s", propertyChangeEvent.getPropertyName()));
            updateTotalPayloadCount();
        }
        if ( "PayloadsModel.enabledPayloadCount".equals(propertyChangeEvent.getPropertyName())) {
            debug(String.format("Event - %s", propertyChangeEvent.getPropertyName()));
            updateTotalPayloadCount();
        }

        // Total payloads changed
        if ( "AutomatedTesterModel.totalPayloadCount".equals(propertyChangeEvent.getPropertyName())) {
            debug(String.format("Event - %s", propertyChangeEvent.getPropertyName()));
            updateTestSummary();
        }

        // New test run added
        if ( "ProjectModel.automatedTestRuns".equals(propertyChangeEvent.getPropertyName())) {
            debug(String.format("Event - %s", propertyChangeEvent.getPropertyName()));
            syncTestRunsTable();
        }

        // Test start / stop
        if ( "AutomatedTestExecutionModel.status".equals(propertyChangeEvent.getPropertyName())) {
            debug(String.format("Event - %s", propertyChangeEvent.getPropertyName()));
            String status = (String) propertyChangeEvent.getNewValue();
            if ( status.equals("STOPPED")) {
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterToolbar.jbtnRun.setText("Run");
            }
            else {
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterToolbar.jbtnRun.setText("Stop");
            }
        }

        // The current test sequence
        if ( "AutomatedTesterModel.testSequence".equals(propertyChangeEvent.getPropertyName())) {
            debug(String.format("Event - %s", propertyChangeEvent.getPropertyName()));
            pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterToolbar.jtxtTestName.setText(automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun().getTestName());
            pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTestConfig.jchkContinueReplayAfterTestInsertion.setSelected(automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun().isContinueReplayAfterTestInsertion());
            pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTestConfig.jchkReuseConnection.setSelected(automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun().isReuseConnection());
            automatedTesterModel.getConversationTableModel().setRowCount(0);
            for (TestSequenceItem testSequenceItem : automatedTesterModel.getTestSequence().getTestSequenceItems()) {
                if ( testSequenceItem.getTestSequenceItemType().equals(TestSequenceItemType.FRAME)) {
                    if ( testSequenceItem.getFrame() != null ) {
                        if ( testSequenceItem.getFrame().getPayloadUnmasked() != null ) {
                            automatedTesterModel.getConversationTableModel().addRow(new Object[] {
                                    testSequenceItem.getFrame().getOpcode(),
                                    new String(testSequenceItem.getFrame().getPayloadUnmasked())
                            });
                        }
                    }
                }
            }
            automatedTesterModel.syncTargetsTable();
            loadTestTraffic();
        }

        // Selected test target
        if ( "AutomatedTesterModel.currentTestTarget".equals(propertyChangeEvent.getPropertyName())) {
            debug(String.format("Event - %s", propertyChangeEvent.getPropertyName()));
            resetSelections();
            if ( automatedTesterModel.getCurrentTestTarget() != null ) {
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblConversation.getSelectionModel().setSelectionInterval(
                        automatedTesterModel.getCurrentTestTarget().getTestableStepIdx(),
                        automatedTesterModel.getCurrentTestTarget().getTestableStepIdx()
                );
                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTesterTargetsToolbar.jtxtTargetName.setText(automatedTesterModel.getCurrentTestTarget().getTargetName());

                pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTesterTargetsToolbar.updateTargetSelectionText(pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubject.getCaretPosition(),
                        pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtxtTargetSubject.getText().length(),
                        automatedTesterModel.getCurrentTestTarget().getStartPos(),
                        automatedTesterModel.getCurrentTestTarget().getEndPos());
                updatePayloadEncodings();
                updateTargetTextHighlighting();
                updateTotalPayloadCount();
            }
        }

        // current test target attributes
        if ( "AutomatedTesterModel.currentTestTarget.name".equals(propertyChangeEvent.getPropertyName())) {
            debug(String.format("Event - %s", propertyChangeEvent.getPropertyName()));
            if ( propertyChangeEvent.getNewValue() != null ) {
                //pnlAutomatedTesterView.pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.pnlAutomatedTesterTargetsToolbar.jtxtTargetName.setText((String)propertyChangeEvent.getNewValue());
            }
        }
        if ( "AutomatedTesterModel.currentTestTarget.selectionRange".equals(propertyChangeEvent.getPropertyName()) ) {
            debug(String.format("Event - %s", propertyChangeEvent.getPropertyName()));
            updateTargetTextHighlighting();
        }

        if ( "AutomatedTesterModel.currentTestTarget.encodings".equals(propertyChangeEvent.getPropertyName())) {
            debug(String.format("Event - %s", propertyChangeEvent.getPropertyName()));
            updatePayloadEncodings();
            updateTestSummary();
        }


        // Test in progress update
        if ( "AutomatedTestExecutionModel.pctComplete".equals(propertyChangeEvent.getPropertyName())) {
            debug(String.format("Event - %s", propertyChangeEvent.getPropertyName()));
            AutomatedTestRun testRun = automatedTesterModel.getAutomatedTestExecutionModel().getCurrentTestRun();
            double pctComplete = 0;
            for ( int i = 0; i < automatedTesterModel.getRunsTableModel().getRowCount(); i++ ) {
                String testId = (String) automatedTesterModel.getRunsTableModel().getValueAt(i,0);
                if ( testId.equals(testRun.getTestId())) {
                    if ( testRun.getTestsCompleted() > 0 ) {
                        pctComplete = ((double)testRun.getTestsCompleted()/(double)testRun.getTestCount())*100;
                    }
                    DefaultTableModel model = automatedTesterModel.getRunsTableModel();
                    model.setValueAt(GuiUtils.trafficTimeFmt.format(new Date(testRun.getTestRunStartTime())),i,1);
                    model.setValueAt(String.format("%d/%d", testRun.getTestsCompleted(),testRun.getTestCount()),i,3);
                    model.setValueAt(String.format("%d%%", (int)pctComplete),i,4);
                    model.setValueAt(testRun.getStatus(),i,5);
                    testRun.setPctComplete((int) pctComplete);
                    break;
                }
            }
        }

    }
}
