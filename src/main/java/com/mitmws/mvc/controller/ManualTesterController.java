package com.mitmws.mvc.controller;

import com.mitmws.httpproxy.websocket.WebsocketFrame;
import com.mitmws.httpproxy.websocket.WebsocketFrameType;
import com.mitmws.integrations.python.ScriptManager;
import com.mitmws.mvc.model.MainModel;
import com.mitmws.mvc.popupmenus.PopupMenuPayloadEditorInsert;
import com.mitmws.projects.ProjectDataServiceException;
import com.mitmws.util.GuiUtils;
import com.mitmws.httpproxy.*;
import com.mitmws.httpproxy.trafficlogger.*;
import com.mitmws.mvc.model.ManualTesterModel;
import com.mitmws.mvc.model.ProjectModel;
import com.mitmws.mvc.thread.ManualTestManagerActivityThread;
import com.mitmws.mvc.thread.ManualTestManagerCleanupThread;
import com.mitmws.mvc.view.panels.manualtester.PnlManualTesterView;
import com.mitmws.tester.*;
import com.mitmws.util.HttpMessageUtil;
import com.mitmws.util.TestUtil;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class ManualTesterController implements PropertyChangeListener {
    private PnlManualTesterView pnlManualTesterView;
    private TrafficLogger trafficLogger;
    private ScriptManager scriptManager;
    private ProjectModel projectModel;
    private ManualTesterModel manualTesterModel;
    private PopupMenuPayloadEditorInsert popupMenuPayloadEditorInsert;
    // Threads
    private ArrayList<ManualTestManagerActivityThread> manualTestManagerActivityThreads;
    private ManualTestManagerCleanupThread manualTestManagerCleanupThread;
    private MainModel mainModel;

    public ManualTesterController(MainModel mainModel, TrafficLogger trafficLogger, PnlManualTesterView pnlManualTesterView) {
        scriptManager = new ScriptManager();
        this.mainModel = mainModel;
        this.manualTesterModel = this.mainModel.getManualTesterModel();
        this.projectModel = this.mainModel.getProjectModel();
        this.trafficLogger = trafficLogger;
        this.pnlManualTesterView = pnlManualTesterView;
        this.mainModel.getImmediateModel().addListener(this);
        this.manualTesterModel.addListener(this);
        this.manualTesterModel.getManualTestExecutionModel().addListener(this);
        popupMenuPayloadEditorInsert = new PopupMenuPayloadEditorInsert(pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlFrameEditor.pnlFrameViewer.jtxtFramePayload,mainModel.getInteractshModel());
        this.pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlFrameEditor.getWebsocketFrameController().getWebsocketFrameModel().addListener(this);

        manualTestManagerActivityThreads = new ArrayList<ManualTestManagerActivityThread>();
        cleanupStart();
        initEventListeners();
        pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.setVisible(false);

        // Create a new test
        manualTesterModel.setCurrentManualTestRun(new ManualTestRun(TestUtil.DEFAULT_TEST_NAME, new TestSequence(scriptManager)));
    }

    /*
        Set the test sequence steps to the order the user set in the jtable
        Sort the underlying testsequence arraylist
     */
    public void saveTestStepOrder() {
        for ( int i = 0; i < pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getRowCount(); i++ ) {
            String selectedId = (String) pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getValueAt(i, 2);
            for (TestSequenceItem sequenceItem : manualTesterModel.getCurrentManualTestRun().getTestSequence().getTestSequenceItems()) {
                if (sequenceItem.getTestId().equals(selectedId)) {
                    sequenceItem.setStepOrder(i);
                    break;
                }
            }
        }
        manualTesterModel.getCurrentManualTestRun().getTestSequence().getTestSequenceItems().sort(Comparator.comparing(TestSequenceItem::getStepOrder));
    }

    /*
        Makes the model reflect what is in the UI
     */
    public void saveTestSequence() {
        manualTesterModel.getCurrentManualTestRun().getTestSequence().setUpgradeHelperScript(null);
        manualTesterModel.getCurrentManualTestRun().getTestSequence().setHttpMessage(pnlManualTesterView.pnlWsConversationEditor.httpRequestResponseModel.buildHttpMessage());
        if ( pnlManualTesterView.pnlWsConversationEditor.httpRequestResponseModel.isUseUpgradeScript() ) {
            manualTesterModel.getCurrentManualTestRun().getTestSequence().setUpgradeHelperScript(
                    pnlManualTesterView.pnlWsConversationEditor.httpRequestResponseModel.getUpgradeScriptName()
            );
        }
        saveTestStepOrder();
    }

    /*
        Make the model test step reflect what is in the UI
     */
    public void saveCurrentStep() {
        if ( !manualTesterModel.getManualTestExecutionModel().getStatus().equals("STARTED")) {
            if ( pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getRowCount() > 0 ) {
                //TestSequenceItem testSequenceItem = manualTesterModel.getCurrentManualTestRun().getTestSequence().getTestItemById(getSelectedConversationTestId());
                TestSequenceItem testSequenceItem = manualTesterModel.getCurrentTestSequenceItem();
                if ( testSequenceItem != null ) {
                    testSequenceItem.setDelayMsec((Integer) pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlTestStepEditorToolbar.jspnDelay.getValue());
                    String operation = pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlTestStepEditorToolbar.jcmbStepType.getSelectedItem().toString();
                    if (operation.equals("FRAME")) {
                        testSequenceItem.setTestSequenceItemType(TestSequenceItemType.FRAME);
                    }
                    if (operation.equals("IOWAIT")) {
                        testSequenceItem.setTestSequenceItemType(TestSequenceItemType.IOWAIT);
                    }

                    if ( testSequenceItem.getTestSequenceItemType().equals(TestSequenceItemType.FRAME)) {
                        WebsocketFrame selectedFrame = testSequenceItem.getFrame();
                        if ( selectedFrame != null ) {
                            WebsocketFrame editorFrame = new WebsocketFrame();
                            editorFrame.setFin(selectedFrame.getFin());
                            editorFrame.setRsv1(selectedFrame.getRsv1());
                            editorFrame.setRsv2(selectedFrame.getRsv2());
                            editorFrame.setRsv3(selectedFrame.getRsv3());
                            editorFrame.setOpcode(selectedFrame.getOpcode());
                            editorFrame.setPayloadUnmasked(selectedFrame.getPayloadUnmasked());
                            pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlFrameEditor.getWebsocketFrameController().getWebsocketFrameModel().setWebsocketFrame(editorFrame);
                        }
                    }
                    loadTestSequence();
                }
            }
        }
    }

    /*
        Gets the selected test step ID from the conversation table
     */
    public String getSelectedConversationTestId() {
        String selectedTestId = null;
        if ( pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getSelectedRow() >= 0 ) {
            selectedTestId = (String) pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getValueAt(pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getSelectedRow(),2);
        }
        return selectedTestId;
    }

    /*
        Gets a list of testStepIds that appear in the conversation jtable
     */
    public ArrayList<String> getDisplayedTestStepIds() {
        String[] displayIds = new String[0];
        ArrayList<String> tmpArr = new ArrayList<>();
        for ( int i =0; i < pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getRowCount(); i++ ) {
            String curId = (String) pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getValueAt(i,2);
            if ( !tmpArr.contains(curId)) {
                tmpArr.add(curId);
            }
        }
        return tmpArr;
    }

    /*
        Removes items from the jtables that are not present in the UI
     */

    public void pruneTestConversation() {
        int start = manualTesterModel.getWebsocketConversationTableModel().getRowCount();
        if ( start > 0 ) {
            for ( int i = start-1; i >= 0; i-- ) {
                boolean exists = false;
                String curId = (String) manualTesterModel.getWebsocketConversationTableModel().getValueAt(i,2);
                for ( TestSequenceItem curItem : manualTesterModel.getCurrentManualTestRun().getTestSequence().getTestSequenceItems() ) {
                    if ( curId.equals(curItem.getTestId())) {
                        exists = true;
                        break;
                    }
                }
                if ( !exists ) {
                    manualTesterModel.getWebsocketConversationTableModel().removeRow(i);
                }
            }
        }


    }

    /*
        Loads the test sequence into the UI
     */
    public void loadTestSequence() {
        if ( !manualTesterModel.getManualTestExecutionModel().getStatus().equals("STARTED")) {
            // Make sure the UI reflects the http upgrade message
            HttpMessage msg = manualTesterModel.getCurrentManualTestRun().getTestSequence().getHttpMessage();
            if ( msg != null ) {
                if ( msg.getUrl() != null ) {
                    pnlManualTesterView.pnlWsConversationEditor.httpRequestResponseController.loadRequest(msg);
                    pnlManualTesterView.pnlWsConversationEditor.pnlHttpRequestResponse.jtxtWsUrl.setText(msg.getUrl());
                    pnlManualTesterView.pnlWsConversationEditor.pnlHttpRequestResponse.jtxtHttpUpgradeMessageHeaders.setText(String.join("\n",msg.getHeaders()));
                    if ( msg.getBodyBytes() != null ) {
                        pnlManualTesterView.pnlWsConversationEditor.pnlHttpRequestResponse.jchkMessageBody.setSelected(true);
                        pnlManualTesterView.pnlWsConversationEditor.pnlHttpRequestResponse.jtxtHttpUpgradeMessageBody.setText(new String(msg.getBodyBytes()));
                    }
                }
            }

            // sync
            ArrayList<String> displayIds = getDisplayedTestStepIds();
            for ( int i = 0; i < manualTesterModel.getCurrentManualTestRun().getTestSequence().getTestSequenceItems().size(); i++ ) {
                TestSequenceItem testSequenceItem = manualTesterModel.getCurrentManualTestRun().getTestSequence().getTestSequenceItems().get(i);
                if ( !displayIds.contains(testSequenceItem.getTestId())) {
                    GuiUtils.updateTestSequenceTable(pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation,testSequenceItem);
                }
            }

            // add
            for ( int i = 0; i < manualTesterModel.getCurrentManualTestRun().getTestSequence().getTestSequenceItems().size(); i++ ) {
                TestSequenceItem testSequenceItem = manualTesterModel.getCurrentManualTestRun().getTestSequence().getTestSequenceItems().get(i);
                GuiUtils.updateTestSequenceTable(pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation,testSequenceItem);
            }

            // remove
            pruneTestConversation();
            if ( pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getSelectedRow() == -1 ) {
                GuiUtils.tableSelectFirst(pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation);
            }
        }
    }

    public void cleanupStart() {
        manualTestManagerCleanupThread = new ManualTestManagerCleanupThread(manualTesterModel.getManualTestExecutionModel(),manualTestManagerActivityThreads);
        manualTestManagerCleanupThread.start();
    }

    public void cleanupStop() {
        manualTestManagerCleanupThread.shutdown();
        try {
            manualTestManagerCleanupThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void runTest(String curTestRunName, TestSequence sequence) {
        ManualTestManagerActivityThread testManagerWorker = new ManualTestManagerActivityThread(mainModel, trafficLogger,
                sequence,
                curTestRunName,sequence.getHttpMessage().isSslEnabled());
        testManagerWorker.start();
        manualTestManagerActivityThreads.add(testManagerWorker);
    }

    public void stopTest() {
        for ( ManualTestManagerActivityThread worker : manualTestManagerActivityThreads ) {
            worker.shutdown();
        }
    }


    // Used by traffic loader thread / traffic tailer thread
    public void addWebsocketFrame( WebsocketFrame frame ) {
        if ( !frame.getOpcode().equals(WebsocketFrameType.PONG)) {
            TestSequenceItem item = new TestSequenceItem();
            if ( frame.getDirection().equals(WebsocketDirection.OUTBOUND)){
                item.setDelayMsec(0);
                item.setTestSequenceItemType(TestSequenceItemType.FRAME);
                item.setFrame(frame);
                manualTesterModel.getCurrentManualTestRun().getTestSequence().getTestSequenceItems().add(item);
            }
            else {
                item.setDelayMsec(500);
                item.setTestSequenceItemType(TestSequenceItemType.IOWAIT);
                manualTesterModel.getCurrentManualTestRun().getTestSequence().getTestSequenceItems().add(item);
            }
        }
        loadTestSequence();
    }

    // Used by "add to manual test" feature
    public void addWebsocketFrameFromHistory(String[] ids) throws IOException {
        WebsocketTrafficRecord rec = null;
        TestSequenceItemType lastType = TestSequenceItemType.IOWAIT;
        for ( String id: ids  ) {
            try {
                rec = projectModel.getProjectDataService().getWebsocketTrafficRecordByUUID(id);
            } catch (ProjectDataServiceException e) {
                e.printStackTrace(); // TODO
            }
            if (rec != null) {
                // remove pongs
                if (rec.getFrame().getOpcode().equals(WebsocketFrameType.PONG)) {
                    continue;
                }
                TestSequenceItem item = new TestSequenceItem();
                if (rec.getFrame().getDirection().equals(WebsocketDirection.OUTBOUND)) {
                    item.setDelayMsec(0);
                    item.setTestSequenceItemType(TestSequenceItemType.FRAME);
                    item.setFrame(rec.getFrame());
                    lastType = TestSequenceItemType.FRAME;
                    manualTesterModel.getCurrentManualTestRun().getTestSequence().getTestSequenceItems().add(item);
                } else {
                    // Perhaps make an option to not include all the io waits?
                    if ( lastType != TestSequenceItemType.IOWAIT ) {
                        item.setTestSequenceItemType(TestSequenceItemType.IOWAIT);
                        item.setDelayMsec(500);
                        manualTesterModel.getCurrentManualTestRun().getTestSequence().getTestSequenceItems().add(item);
                        lastType = TestSequenceItemType.IOWAIT;
                    }
                }
            }
        }
        loadTestSequence();
        saveTestSequence();
    }

    public void addHttpMessageFromHistory(String id ) throws IOException {
        HttpTrafficRecord rec = null;
        try {
            rec = projectModel.getProjectDataService().getHttpTrafficRecordByUUID(id);
        } catch (ProjectDataServiceException e) {
            e.printStackTrace();
        }
        if ( rec != null ) {
            manualTesterModel.getCurrentManualTestRun().getTestSequence().setHttpMessage(rec.getRequest());
        }
        loadTestSequence();
        saveTestSequence();
    }


    /*
        UI element event listeners
     */
    public void initEventListeners() {
        /*
            Run test
         */
        pnlManualTesterView.pnlWsConversationEditor.pnlHttpRequestResponse.jbtnRunTest.addActionListener(actionEvent -> {
            if ( manualTesterModel.getManualTestExecutionModel().getStatus().equals("STARTED") ) {
                stopTest();
            }
            else {
                if ( manualTesterModel.getCurrentManualTestRun().getTestSequence().getTestSequenceItems().size() > 0 ) {
                    try {
                        saveTestSequence();
                        saveCurrentStep();
                        projectModel.save();
                        GuiUtils.clearTable(pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation);
                        loadTestSequence();

                        manualTesterModel.getCurrentManualTestRun().getTestSequence().setEventScript(null);
                        if ( pnlManualTesterView.pnlWsConversationEditor.pnlHttpRequestResponse.pnlUpgradeRequestScriptConfig.jchkEventScript.isSelected() ) {
                            String selectedScript = (String) pnlManualTesterView.pnlWsConversationEditor.pnlHttpRequestResponse.pnlUpgradeRequestScriptConfig.jcmbEventScripts.getSelectedItem();
                            if ( selectedScript != null ) {
                                manualTesterModel.getCurrentManualTestRun().getTestSequence().setEventScript(selectedScript);
                            }
                        }

                        String curTestRunName = projectModel.getProjectDataService().getNextTestRunName(pnlManualTesterView.pnlWsConversationEditor.pnlHttpRequestResponse.jtxtTestRunName.getText());
                        manualTesterModel.getProjectModel().getManualTestRuns().add(new ManualTestRun(curTestRunName, manualTesterModel.getCurrentManualTestRun().getTestSequence().getCopy()));
                        manualTesterModel.setCurrentManualTestRun(new ManualTestRun(curTestRunName, manualTesterModel.getCurrentManualTestRun().getTestSequence().getCopy()));
                        projectModel.getProjectDataService().saveManualTestRuns(manualTesterModel.getProjectModel().getManualTestRuns());
                        runTest(curTestRunName,manualTesterModel.getCurrentManualTestRun().getTestSequence());
                    } catch (ProjectDataServiceException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        /*
            Conversation toolbar buttons ( new, up, down, duplicate etc )
         */
        pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlWsConversationToolbar.jbtnNew.addActionListener(actionEvent -> {
            // Create a new frame
            WebsocketFrame frame = new WebsocketFrame();
            frame.setFin(1);
            frame.setDirection(WebsocketDirection.OUTBOUND);
            frame.setOpcode(WebsocketFrameType.TEXT);
            frame.setMasked(1);
            frame.setMaskKey(frame.generateMaskBytes());
            frame.setPayloadUnmasked(TestUtil.DEFAULT_TEST_WS_MESSAGE.getBytes());

            // Create the test sequence item
            TestSequenceItem item = new TestSequenceItem();
            item.setDelayMsec(0);
            item.setTestSequenceItemType(TestSequenceItemType.FRAME);
            item.setFrame(frame);
            manualTesterModel.getCurrentManualTestRun().getTestSequence().getTestSequenceItems().add(item);
            loadTestSequence();
            saveTestStepOrder();
            GuiUtils.tableSelectLast(pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation);
        });

        /*
            Up
         */
        pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlWsConversationToolbar.jbtnUp.addActionListener(actionEvent -> {
            int selectedRow = pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getSelectedRow();
            if ( selectedRow > 0 ) {
                DefaultTableModel model = (DefaultTableModel) pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getModel();
                model.moveRow(selectedRow,selectedRow,selectedRow-1);
                pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getSelectionModel().setSelectionInterval(selectedRow-1,selectedRow-1);
                saveTestStepOrder();
            }
        });

        /*
            Down
         */
        pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlWsConversationToolbar.jbtnDown.addActionListener(actionEvent -> {
            int selectedRow = pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getSelectedRow();
            if ( selectedRow < pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getRowCount()-1 ) {
                DefaultTableModel model = (DefaultTableModel) pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getModel();
                model.moveRow(selectedRow,selectedRow,selectedRow+1);
                pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getSelectionModel().setSelectionInterval(selectedRow+1,selectedRow+1);
                saveTestStepOrder();
            }
        });
        /*
            Duplicate
         */
        pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlWsConversationToolbar.jbtnDuplicate.addActionListener(actionEvent -> {
            //TestSequenceItem selectedItem = manualTesterModel.getCurrentManualTestRun().getTestSequence().getTestItemById(getSelectedConversationTestId());
            TestSequenceItem selectedItem = manualTesterModel.getCurrentTestSequenceItem();
            if ( selectedItem != null ) {
                TestSequenceItem newItem = new TestSequenceItem();
                newItem.setTestSequenceItemType(selectedItem.getTestSequenceItemType());
                newItem.setDelayMsec(selectedItem.getDelayMsec());
                newItem.setFrame(selectedItem.getFrame().getCopy());
                manualTesterModel.getCurrentManualTestRun().getTestSequence().addTestStep(newItem);
                loadTestSequence();
                int selectedIdx = pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getRowCount()-1;
                pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getSelectionModel().setSelectionInterval(selectedIdx,selectedIdx);
            }
        });

        /*
            Delete
         */
        pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlWsConversationToolbar.jbtnDelete.addActionListener(actionEvent -> {
            int rowId = pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getSelectedRow();
            if ( rowId >= 0 ) {
                manualTesterModel.getCurrentManualTestRun().getTestSequence().getTestSequenceItems().remove(rowId);
                String stepId = pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getValueAt(rowId, 2).toString();
                if (stepId != null) {
                    manualTesterModel.getCurrentManualTestRun().getTestSequence().deleteTestStep(stepId);
                    loadTestSequence();
                }
                int selectedIdx = pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getRowCount()-1;
                if ( rowId < pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getRowCount() ) {
                    selectedIdx = rowId;
                }

                if ( selectedIdx >= 0 ) {
                    pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getSelectionModel().setSelectionInterval(selectedIdx, selectedIdx);
                }
                else {
                    pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.setVisible(false);
                }
            }
        });

        /*
            Clear
         */
        pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlWsConversationToolbar.jbtnClear.addActionListener(actionEvent -> {
            manualTesterModel.setCurrentManualTestRun(new ManualTestRun(TestUtil.DEFAULT_TEST_NAME, new TestSequence(scriptManager)));
            loadTestSequence();
            pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.setRowSorter(null);
        });

        /*
            Conversation builder table row select
         */
        pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            saveTestSequence();
            pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlFrameEditor.getWebsocketFrameController().getWebsocketFrameModel().setWebsocketFrame(null);
            int rowId = pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getSelectedRow();
            if ( rowId >= 0 ) {
                String testId = pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getValueAt(rowId, 2).toString();
                TestSequenceItem testSequenceItem = manualTesterModel.getCurrentManualTestRun().getTestSequence().getTestItemById(testId);
                manualTesterModel.setCurrentTestSequenceItem(testSequenceItem);
                if ( testSequenceItem != null ) {
                    // Setup the frame editor
                    GuiUtils.setComboBoxItem(pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlTestStepEditorToolbar.jcmbStepType,testSequenceItem.getTestSequenceItemType().toString());
                    pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlTestStepEditorToolbar.jspnDelay.setValue(testSequenceItem.getDelayMsec());
                    // Setup the frame, if there. IOWAIT has no config yet
                    if ( testSequenceItem.getTestSequenceItemType().equals(TestSequenceItemType.FRAME)) {
                        WebsocketFrame selectedFrame = testSequenceItem.getFrame();
                        if ( selectedFrame != null ) {
                            pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlFrameEditor.getWebsocketFrameController().getWebsocketFrameModel().setWebsocketFrame(selectedFrame);
                        }
                    }
                    // Show the correct editor panel for this type of test step
                }
            }
        });

        /*
            Changes to the conversation builder table model
         */
        manualTesterModel.getWebsocketConversationTableModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                boolean enabled = manualTesterModel.getWebsocketConversationTableModel().getRowCount() > 0;
                if ( enabled ) {
                    pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.spltConvBuilder.setDividerLocation(0.5);
                }
                pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.setVisible(enabled);
                pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlTestStepEditorToolbar.setVisible(enabled);
                pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlWsConversationToolbar.jbtnAutomate.setEnabled(enabled);


            }
        });

        /*
            Conversation replay history table row select
         */
        pnlManualTesterView.pnlWsConversationHistory.pnlTestHistoryTrafficViewer.tblWebsocketConversation.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            // load the conversation steps ( if not already loaded )
            int rowId = pnlManualTesterView.pnlWsConversationHistory.pnlTestHistoryTrafficViewer.tblWebsocketConversation.getSelectedRow();
            if (!manualTesterModel.getManualTestExecutionModel().getStatus().equals("STARTED")) {
                if (rowId >= 0) {
                    String selectedTestName = pnlManualTesterView.pnlWsConversationHistory.pnlTestHistoryTrafficViewer.tblWebsocketConversation.getValueAt(rowId, 2).toString();
                    //System.out.println(String.format("Total tests = %d, Manual test name = %s row selected test name = %s, step count = %d", project.getProjectData().getManualTestRuns().size(), manualTestRun.getTestName(), selectedTestName, manualTestRun.getTestSequence().getTestSequenceItems().size()));
                    if (!manualTesterModel.getCurrentManualTestRun().getTestName().equals(selectedTestName)) {
                        //manualTestRun = project.getProjectData().getManualTestRunByName(selectedTestName);
                        //System.out.println(String.format("Currently selected test = %s", manualTestRun.getTestName()));
                        manualTesterModel.setCurrentManualTestRun(new ManualTestRun( selectedTestName,manualTesterModel.getProjectModel().getManualTestRunByName(selectedTestName).getTestSequence().getCopy()));
                        GuiUtils.clearTable(pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation);
                        loadTestSequence();
                        GuiUtils.tableSelectFirst(pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation);
                    }
                }
            }
            // Load the editor
            pnlManualTesterView.pnlWsConversationHistory.pnlTestHistoryTrafficViewer.getWebsocketFrameController().getWebsocketFrameModel().setWebsocketFrame(null);
            if ( rowId >= 0 ) {
                String messageId = (String) pnlManualTesterView.pnlWsConversationHistory.pnlTestHistoryTrafficViewer.tblWebsocketConversation.getValueAt(rowId, 0);
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
                            pnlManualTesterView.pnlWsConversationHistory.pnlTestHistoryTrafficViewer.pnlFrameViewer.pnlWebsocketFrameViewerToolbar.jlblFrameProperties.setText(frameStr);
                            if ( rec.getFrame().getPayload() != null ) {
                                pnlManualTesterView.pnlWsConversationHistory.pnlTestHistoryTrafficViewer.getWebsocketFrameController().getWebsocketFrameModel().setWebsocketFrame(rec.getFrame());
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
            Changes to the test step type combobox
         */
        pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlTestStepEditorToolbar.jcmbStepType.addActionListener(actionEvent -> {
            String selectedValue = pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlTestStepEditorToolbar.jcmbStepType.getSelectedItem().toString();
            if ( selectedValue.equals("FRAME")) {
                manualTesterModel.setCurrentTestSequenceItemType(TestSequenceItemType.FRAME);
            }
            else {
                manualTesterModel.setCurrentTestSequenceItemType(TestSequenceItemType.IOWAIT);
                if ( manualTesterModel.getCurrentTestSequenceItem().getDelayMsec() == 0 ) {
                    manualTesterModel.setCurrentTestSequenceItemDelay(100);
                }
            }
        });
        /*
            Changes to the delay for the test step
         */
        pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlTestStepEditorToolbar.jspnDelay.getModel().addChangeListener( changeEvent -> {
            manualTesterModel.setCurrentTestSequenceItemDelay((Integer) pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlTestStepEditorToolbar.jspnDelay.getValue());
        });

    }
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

        /*
            The current test run has changed
         */
        if ( "ManualTesterModel.currentManualTestRun".equals(propertyChangeEvent.getPropertyName())) {
            loadTestSequence();
        }

        if ( "ManualTesterModel.currentTestSequenceItem".equals(propertyChangeEvent.getPropertyName())) {
            if( manualTesterModel.getCurrentTestSequenceItem() != null ) {
                TestSequenceItem testSequenceItem = manualTesterModel.getCurrentTestSequenceItem();
                // refire the events to set the type / delay
                manualTesterModel.setCurrentTestSequenceItemDelay(testSequenceItem.getDelayMsec());
                manualTesterModel.setCurrentTestSequenceItemType(testSequenceItem.getTestSequenceItemType());
            }
        }

        if ( "ManualTesterModel.currentTestSequenceItem.delay".equals(propertyChangeEvent.getPropertyName())) {
            Integer val = (Integer) propertyChangeEvent.getNewValue();
            pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlTestStepEditorToolbar.jspnDelay.setValue(val);
            loadTestSequence();
        }

        if ( "ManualTesterModel.currentTestSequenceItem.type".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() != null ) {
                TestSequenceItemType stepType = (TestSequenceItemType) propertyChangeEvent.getNewValue();
                GuiUtils.setComboBoxItem(pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlTestStepEditorToolbar.jcmbStepType,stepType.toString());
                if ( stepType.equals(TestSequenceItemType.FRAME)) {
                    pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlFrameEditor.setVisible(true);
                    pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlTestStepOperationEditor.setVisible(false);
                }
                else {
                    pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlFrameEditor.setVisible(false);
                    pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlTestStepOperationEditor.setVisible(true);
                }
            }
            else {
                pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlFrameEditor.setVisible(false);
                pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.pnlTestStepOperationEditor.setVisible(false);
            }
            loadTestSequence();
        }

        if ( "ManualTestExecutionModel.upgradeResponse".equals(propertyChangeEvent.getPropertyName())) {
            pnlManualTesterView.pnlWsConversationEditor.httpRequestResponseController.getHttpRequestResponseModel().setRequestResponse("");
            if ( propertyChangeEvent.getNewValue()!= null ) {
                pnlManualTesterView.pnlWsConversationEditor.httpRequestResponseController.getHttpRequestResponseModel().setRequestResponse(
                        HttpMessageUtil.getRequestResponseString(
                                manualTesterModel.getManualTestExecutionModel().getUpgradeRequest(),manualTesterModel.getManualTestExecutionModel().getUpgradeResponse()
                        )
                );
            }
        }
        if ( "ManualTestExecutionModel.currentTestStep".equals(propertyChangeEvent.getPropertyName())) {
            int stepIdx = (int) propertyChangeEvent.getNewValue();
            if ( stepIdx >= 0 && stepIdx < pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.getRowCount()) {
                pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.tblWebsocketConversation.setRowSelectionInterval(stepIdx,stepIdx);
            }
        }
        if ( "ManualTestExecutionModel.status".equals(propertyChangeEvent.getPropertyName())) {
            String status = (String) propertyChangeEvent.getNewValue();
            if ( status.equals("STARTED")) {
                manualTesterModel.setConnectionStatusString("[Connected]");
                pnlManualTesterView.pnlWsConversationEditor.pnlHttpRequestResponse.jbtnRunTest.setText("Stop");

            }
            else {
                manualTesterModel.setConnectionStatusString("[Not connected]");
                pnlManualTesterView.pnlWsConversationEditor.pnlHttpRequestResponse.jbtnRunTest.setText("Run");
                pnlManualTesterView.pnlWsConversationEditor.pnlHttpRequestResponse.jbtnRunTest.setEnabled(true);
            }
        }

        if ( "ManualTesterModel.connectionStatusString".equals(propertyChangeEvent.getPropertyName())) {
            String status = (String) propertyChangeEvent.getNewValue();
            if ( status != null ) {
                pnlManualTesterView.pnlWsConversationEditor.pnlHttpRequestResponse.lblConnectionStatus.setText(status);
            }
        }

        if ( "WebsocketFrameModelEditor.websocketFrame".equals(propertyChangeEvent.getPropertyName())) {
            WebsocketFrame frame = (WebsocketFrame) propertyChangeEvent.getNewValue();
        }

        if ( "WebsocketFrameModelEditor.fin".equals(propertyChangeEvent.getPropertyName())) {
            loadTestSequence();
        }
        if ( "WebsocketFrameModelEditor.rsv1".equals(propertyChangeEvent.getPropertyName())) {
            loadTestSequence();
        }

        if ( "WebsocketFrameModelEditor.rsv2".equals(propertyChangeEvent.getPropertyName())) {
            loadTestSequence();
        }

        if ( "WebsocketFrameModelEditor.rsv3".equals(propertyChangeEvent.getPropertyName())) {
            loadTestSequence();
        }

        if ( "WebsocketFrameModelEditor.opcode".equals(propertyChangeEvent.getPropertyName())) {
            loadTestSequence();
        }
        if ( "WebsocketFrameModelEditor.payload".equals(propertyChangeEvent.getPropertyName())) {
            loadTestSequence();
        }
        if ( "InteractshModel.correlationId".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() != null ) {
                popupMenuPayloadEditorInsert.mnuInsertIntshPayload.setEnabled(true);
            }
            else {
                popupMenuPayloadEditorInsert.mnuInsertIntshPayload.setEnabled(false);
            }
        }
    }
}
