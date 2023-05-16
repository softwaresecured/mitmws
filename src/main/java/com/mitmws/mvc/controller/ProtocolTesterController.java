package com.mitmws.mvc.controller;

import com.mitmws.httpproxy.websocket.WebsocketFrameType;
import com.mitmws.mvc.model.FuzzRecordModel;
import com.mitmws.mvc.model.MainModel;
import com.mitmws.mvc.thread.ProtocolTesterThread;
import com.mitmws.mvc.thread.RawTesterThread;
import com.mitmws.mvc.view.panels.protocoltester.PnlProtocolTesterView;
import com.mitmws.util.GuiUtils;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ProtocolTesterController implements PropertyChangeListener {

    private MainModel mainModel;
    private PnlProtocolTesterView pnlProtocolTesterView;

    public ProtocolTesterController(MainModel mainModel, PnlProtocolTesterView pnlProtocolTesterView) {
        this.mainModel = mainModel;
        this.pnlProtocolTesterView = pnlProtocolTesterView;
        this.mainModel.addListener(this);
        this.mainModel.getProtocolTesterModel().addListener(this);
        initEventListeners();
        this.mainModel.getProtocolTesterModel().setTestWebsocketTextPayload("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
        this.mainModel.getProtocolTesterModel().setTestWebsocketBinaryPayload(GuiUtils.binToHexStr("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".getBytes(StandardCharsets.UTF_8)));

    }

    public void initEventListeners() {
        pnlProtocolTesterView.pnlSettings.pnlHttpRequestResponse.jbtnRunTest.addActionListener(actionEvent -> {

            int tabIndex = pnlProtocolTesterView.pnlSettings.tabbedPane.getSelectedIndex();
            // Fuzzer
            if ( pnlProtocolTesterView.pnlSettings.tabbedPane.getTitleAt(tabIndex).equals("Fuzz")) {
                if ( mainModel.getProtocolTesterThread() == null && validateTestBinaryPayload()) {
                    mainModel.getProtocolTesterModel().setTestWebsocketTextPayload(pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.getJtxtTextSample().getText());
                    mainModel.getProtocolTesterModel().setTestWebsocketBinaryPayload(pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.getJtxtBinarySample().getText());
                    ProtocolTesterThread protocolTesterThread = new ProtocolTesterThread(mainModel);
                    mainModel.getProtocolTesterModel().setUpgradeHelperScript(mainModel.getProtocolTesterModel().getHttpRequestResponseModel().getUpgradeScriptName());
                    mainModel.getProtocolTesterModel().setUpgradeHttpMessage(mainModel.getProtocolTesterModel().getHttpRequestResponseModel().buildHttpMessage());
                    protocolTesterThread.start();
                    mainModel.setProtocolTesterThread(protocolTesterThread);
                }
                else {
                    mainModel.getProtocolTesterThread().shutdown();
                }
            }
            // Raw request
            else {
                if ( mainModel.getRawTesterThread() == null ) {
                    RawTesterThread rawTesterThread = new RawTesterThread(mainModel);
                    mainModel.getProtocolTesterModel().setUpgradeHelperScript(mainModel.getProtocolTesterModel().getHttpRequestResponseModel().getUpgradeScriptName());
                    mainModel.getProtocolTesterModel().setUpgradeHttpMessage(mainModel.getProtocolTesterModel().getHttpRequestResponseModel().buildHttpMessage());
                    rawTesterThread.start();
                    mainModel.setRawTesterThread(rawTesterThread);
                }
                else {
                    mainModel.getRawTesterThread().shutdown();
                }
            }
        });

        pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.getJtxtBinarySample().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if ( validateTestBinaryPayload()) {
                    pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.getJtxtBinarySample().setBackground(Color.WHITE);
                }
                else {
                    pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.getJtxtBinarySample().setBackground(Color.PINK);
                }
            }
        });



        pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkContinuation.addActionListener( actionEvent -> {
            saveSelectedFrameTypes();
        });

        pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkText.addActionListener( actionEvent -> {
            saveSelectedFrameTypes();
        });

        pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkBin.addActionListener( actionEvent -> {
            saveSelectedFrameTypes();
        });

        pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkRes1.addActionListener( actionEvent -> {
            saveSelectedFrameTypes();
        });

        pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkRes2.addActionListener( actionEvent -> {
            saveSelectedFrameTypes();
        });

        pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkRes3.addActionListener( actionEvent -> {
            saveSelectedFrameTypes();
        });

        pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkRes4.addActionListener( actionEvent -> {
            saveSelectedFrameTypes();
        });

        pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkRes5.addActionListener( actionEvent -> {
            saveSelectedFrameTypes();
        });

        pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkClose.addActionListener( actionEvent -> {
            saveSelectedFrameTypes();
        });

        pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkPing.addActionListener( actionEvent -> {
            saveSelectedFrameTypes();
        });

        pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkPong.addActionListener( actionEvent -> {
            saveSelectedFrameTypes();
        });

        pnlProtocolTesterView.pnlTestOutput.pnlTestOutputLog.tblTestTrafficLog.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if ( pnlProtocolTesterView.pnlTestOutput.pnlTestOutputLog.tblTestTrafficLog.getSelectedRow() >= 0 ) {
                    String id = (String) pnlProtocolTesterView.pnlTestOutput.pnlTestOutputLog.tblTestTrafficLog.getValueAt(pnlProtocolTesterView.pnlTestOutput.pnlTestOutputLog.tblTestTrafficLog.getSelectedRow(),0);
                    try {
                        FuzzRecordModel fuzzRecordModel = mainModel.getProtocolTesterModel().fetchFuzzRecord(id);
                        if ( fuzzRecordModel != null ) {
                            String payloadPreview = null;
                            if ( fuzzRecordModel.getTxFrame() != null ) {
                                payloadPreview = GuiUtils.getBinPreviewStr(fuzzRecordModel.getTxFrame());
                            }
                            if ( fuzzRecordModel.getRxFrame() != null ) {
                                payloadPreview = GuiUtils.getBinPreviewStr(fuzzRecordModel.getRxFrame());
                            }
                            if ( payloadPreview != null ) {
                                pnlProtocolTesterView.pnlTestOutput.pnlTestOutputLog.txtPreviewFrame.setText(payloadPreview);
                            }
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        pnlProtocolTesterView.pnlSettings.pnlRawRequest.jtxtRawFrameHex.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {

            }

            @Override
            public void removeUpdate(DocumentEvent e) {

            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                mainModel.getProtocolTesterModel().setRawFrameHexStr(pnlProtocolTesterView.pnlSettings.pnlRawRequest.jtxtRawFrameHex.getText());
            }
        });
    }

    public boolean validateTestBinaryPayload() {
        String hexText = pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.getJtxtBinarySample().getText().trim();
        if ( hexText.length() % 2 == 0 && hexText.matches("(?i)[a-f0-9]+")) {
            return true;
        }
        else {
            return false;
        }
    }

    private void saveSelectedFrameTypes() {
        mainModel.getProtocolTesterModel().getTestFrameTypes().clear();
        if ( pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkContinuation.isSelected() ) {
            mainModel.getProtocolTesterModel().getTestFrameTypes().add(WebsocketFrameType.CONTINUATION);
        }
        if ( pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkText.isSelected() ) {
            mainModel.getProtocolTesterModel().getTestFrameTypes().add(WebsocketFrameType.TEXT);
        }
        if ( pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkBin.isSelected() ) {
            mainModel.getProtocolTesterModel().getTestFrameTypes().add(WebsocketFrameType.BINARY);
        }
        if ( pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkRes1.isSelected() ) {
            mainModel.getProtocolTesterModel().getTestFrameTypes().add(WebsocketFrameType.RESERVED1);
        }
        if ( pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkRes2.isSelected() ) {
            mainModel.getProtocolTesterModel().getTestFrameTypes().add(WebsocketFrameType.RESERVED2);
        }
        if ( pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkRes3.isSelected() ) {
            mainModel.getProtocolTesterModel().getTestFrameTypes().add(WebsocketFrameType.RESERVED3);
        }
        if ( pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkRes4.isSelected() ) {
            mainModel.getProtocolTesterModel().getTestFrameTypes().add(WebsocketFrameType.RESERVED4);
        }
        if ( pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkRes5.isSelected() ) {
            mainModel.getProtocolTesterModel().getTestFrameTypes().add(WebsocketFrameType.RESERVED5);
        }
        if ( pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkClose.isSelected() ) {
            mainModel.getProtocolTesterModel().getTestFrameTypes().add(WebsocketFrameType.CLOSE);
        }
        if ( pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkPing.isSelected() ) {
            mainModel.getProtocolTesterModel().getTestFrameTypes().add(WebsocketFrameType.PING);
        }
        if ( pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.jchkPong.isSelected() ) {
            mainModel.getProtocolTesterModel().getTestFrameTypes().add(WebsocketFrameType.PONG);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if ( "MainModel.protocolTesterThread".equals(propertyChangeEvent.getPropertyName()) || "MainModel.protocolTesterThread".equals(propertyChangeEvent.getPropertyName())) {
            if (propertyChangeEvent.getNewValue() == null) {
                pnlProtocolTesterView.pnlSettings.pnlHttpRequestResponse.jbtnRunTest.setText("Run");
            } else {
                pnlProtocolTesterView.pnlSettings.pnlHttpRequestResponse.jbtnRunTest.setText("Stop");
            }
        }
        if ("ProtocolTesterModel.lastHttpTxRx".equals(propertyChangeEvent.getPropertyName())) {
            pnlProtocolTesterView.pnlTestOutput.pnlTestOutputSightGlass.jtxtHttp.setText(mainModel.getProtocolTesterModel().getLastHttpTxRx());
        }

        if ("ProtocolTesterModel.currentFuzzRecord".equals(propertyChangeEvent.getPropertyName())) {
            if ( mainModel.getProtocolTesterModel().getCurrentFuzzRecord().getTxFrame() != null ) {
                pnlProtocolTesterView.pnlTestOutput.pnlTestOutputSightGlass.jtxtLastTx.setText(GuiUtils.getBinPreviewStr(mainModel.getProtocolTesterModel().getCurrentFuzzRecord().getTxFrame()));
            }
            else {
                pnlProtocolTesterView.pnlTestOutput.pnlTestOutputSightGlass.jtxtLastTx.setText("");
            }

            if ( mainModel.getProtocolTesterModel().getCurrentFuzzRecord().getRxFrame() != null ) {
                pnlProtocolTesterView.pnlTestOutput.pnlTestOutputSightGlass.jtxtLastRx.setText(GuiUtils.getBinPreviewStr(mainModel.getProtocolTesterModel().getCurrentFuzzRecord().getRxFrame()));
            }
            else {
                pnlProtocolTesterView.pnlTestOutput.pnlTestOutputSightGlass.jtxtLastRx.setText("");
            }
        }

        if ("ProtocolTesterModel.testWebsocketTextPayload".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() != null ) {
                pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.getJtxtTextSample().setText((String)propertyChangeEvent.getNewValue());
            }
        }
        if ("ProtocolTesterModel.testWebsocketBinaryPayload".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() != null ) {
                pnlProtocolTesterView.pnlSettings.pnlTestSettingsProperties.getJtxtBinarySample().setText((String)propertyChangeEvent.getNewValue());
            }
        }
        if ("ProtocolTesterModel.testStatus".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() != null ) {
                String status = (String) propertyChangeEvent.getNewValue();
                if ( !status.equals("RUNNING")) {
                    mainModel.getProtocolTesterModel().setTestsCompleted(0);
                }
            }
        }

        if ("ProtocolTesterModel.testsCompleted".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() != null ) {
                pnlProtocolTesterView.pnlTestOutput.pnlTestOutputLog.jlblTestStatus.setText(
                        String.format("Running - %d/%d %.2f%% complete",
                        mainModel.getProtocolTesterModel().getTestsCompleted(),
                        mainModel.getProtocolTesterModel().getTotalTests(),
                        mainModel.getProtocolTesterModel().getPctComplete())
                );
            }
        }
        if ("ProtocolTesterModel.rawFrameHexStr".equals(propertyChangeEvent.getPropertyName())) {
            pnlProtocolTesterView.pnlSettings.pnlRawRequest.jtxtRawFrameHex.setText("");
            if ( propertyChangeEvent.getNewValue() != null ) {
                pnlProtocolTesterView.pnlSettings.pnlRawRequest.jtxtRawFrameHex.setText((String)propertyChangeEvent.getNewValue());
            }
        }
    }
}
