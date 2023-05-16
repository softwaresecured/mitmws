package com.mitmws.mvc.controller;

import com.mitmws.environment.Environment;
import com.mitmws.httpproxy.websocket.WebsocketFrame;
import com.mitmws.httpproxy.websocket.WebsocketSession;
import com.mitmws.httpproxy.trafficlogger.*;
import com.mitmws.mvc.model.ImmediateModel;
import com.mitmws.mvc.model.MainModel;
import com.mitmws.mvc.model.ProjectModel;
import com.mitmws.mvc.popupmenus.PopupMenuPayloadEditorInsert;
import com.mitmws.mvc.thread.ImmediateThread;
import com.mitmws.mvc.view.panels.immediate.PnlImmediateView;
import com.mitmws.projects.ProjectDataServiceException;
import com.mitmws.util.GuiUtils;
import com.mitmws.util.HttpMessageUtil;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;

public class ImmediateController implements PropertyChangeListener {

    private ImmediateModel immediateModel;
    private PnlImmediateView pnlImmediateView;
    private MainModel mainModel;
    private ProjectModel projectModel;
    private TrafficLogger logger;
    private PopupMenuPayloadEditorInsert popupMenuPayloadEditorInsert;
    public ImmediateController(MainModel mainModel, ImmediateModel immediateModel, ProjectModel projectModel, TrafficLogger logger, PnlImmediateView pnlImmediateView) {
        this.mainModel = mainModel;
        this.immediateModel = immediateModel;
        this.pnlImmediateView = pnlImmediateView;
        this.immediateModel.addListener(this);
        this.mainModel.addListener(this);
        this.mainModel.getImmediateModel().addListener(this);
        this.logger = logger;
        this.projectModel = projectModel;
        initEventListeners();
        mainModel.getImmediateModel().setAutoPingPong(true);
        pnlImmediateView.pnlImmediateEditor.immediateToolbar.jchkJoinActiveSession.setEnabled(false);
        pnlImmediateView.pnlImmediateEditor.getWebsocketFrameController().getWebsocketFrameModel().newFrame();
        popupMenuPayloadEditorInsert = new PopupMenuPayloadEditorInsert(pnlImmediateView.pnlImmediateEditor.pnlWebsocketFrameView.jtxtFramePayload, mainModel.getInteractshModel());
    }

    public void initEventListeners() {
        pnlImmediateView.pnlImmediateViewer.pnlWebsocketTrafficToolbar.jbtnApply.addActionListener( actionEvent -> {
            if ( GuiUtils.validateRegex(pnlImmediateView.pnlImmediateViewer.pnlWebsocketTrafficToolbar.jtxtPayloadRegex.getText())) {
                pnlImmediateView.pnlImmediateViewer.pnlWebsocketTrafficToolbar.jtxtPayloadRegex.setBackground(Color.WHITE);
                GuiUtils.updateShortWebsocketTableTrafficRowFilter(null, pnlImmediateView.pnlImmediateViewer.pnlWebsocketTrafficToolbar,pnlImmediateView.pnlImmediateViewer.tblWebsocketConversation);
            }
            else {
                pnlImmediateView.pnlImmediateViewer.pnlWebsocketTrafficToolbar.jtxtPayloadRegex.setBackground(GuiUtils.ERROR_COLOR);
            }
        });


        pnlImmediateView.pnlImmediateEditor.immediateToolbar.btnConnect.addActionListener( actionEvent -> {
            if ( mainModel.getImmediateThread() == null ) {
                immediateModel.getRequestResponseModel().setEditable(false);
                try {
                    String curTestRunName = projectModel.getProjectDataService().getNextTestRunName(pnlImmediateView.pnlImmediateEditor.getHttpRequestResponseController().getHttpRequestResponseModel().getTestRunBaseName());
                    /*
                        Setup for "drop-in"
                        To do this we must pause the session, re-load the traffic, add an environment and un-pause the session
                     */

                    if ( pnlImmediateView.pnlImmediateEditor.immediateToolbar.jchkJoinActiveSession.isSelected()) {
                        WebsocketSession session = mainModel.getProxy().getWebsocketSessionByName((String) pnlImmediateView.pnlImmediateEditor.immediateToolbar.jcmbSessions.getSelectedItem());
                        if ( session != null ) {
                            try {
                                mainModel.getImmediateModel().setWebsocketSession(session);
                                session.pause();
                                Environment env = new Environment();
                                env.setInteractshModel(mainModel.getInteractshModel());
                                session.setEnvironment(new Environment()); // Apply the environment to the websocket session ( since it is now immediate )
                                session.setTestName(curTestRunName); // give it a test name
                                projectModel.getProjectDataService().updateTrafficSource(session.getUpgradeMsgUUID(), TrafficSource.IMMEDIATE);

                                // load the traffic
                                mainModel.getImmediateModel().getWebsocketTrafficTableModel().setRowCount(0);
                                HttpTrafficRecord upgradeRequest = projectModel.getProjectDataService().getHttpTrafficRecordByUUID(session.getUpgradeMsgUUID());
                                immediateModel.getRequestResponseModel().setRequestResponse(
                                        HttpMessageUtil.getRequestResponseString(upgradeRequest.getRequest(), upgradeRequest.getResponse())
                                );
                                ArrayList<WebsocketTrafficRecord> trafficRecords = projectModel.getProjectDataService().getWebsocketTrafficRecordsByUpgradeMessageUUID(session.getUpgradeMsgUUID());
                                pnlImmediateView.pnlImmediateEditor.getHttpRequestResponseController().loadRequest(upgradeRequest.getRequest());
                                pnlImmediateView.pnlImmediateEditor.getHttpRequestResponseController().getHttpRequestResponseModel().setResponse(new String(upgradeRequest.getResponse().getBytes()));
                                pnlImmediateView.pnlImmediateEditor.getWebsocketFrameController().getWebsocketFrameModel().newFrame();
                                for ( WebsocketTrafficRecord websocketTrafficRecord : trafficRecords ) {
                                    mainModel.getImmediateModel().addWebsocketTraffic(websocketTrafficRecord.getFrame(),curTestRunName);
                                }

                                // remove the conversation from the proxy traffic viewer
                                for ( int i = 0; i < mainModel.getTrafficModel().getWebsocketConnectionsModel().getRowCount(); i++ ) {
                                    String upgradeUuid = (String) mainModel.getTrafficModel().getWebsocketConnectionsModel().getValueAt(i,0);
                                    if ( upgradeUuid != null && upgradeUuid.equals(session.getUpgradeMsgUUID())) {
                                        mainModel.getTrafficModel().getWebsocketConnectionsModel().removeRow(i);
                                        break;
                                    }
                                }
                                session.enableImmediate();
                                session.resume();
                            } catch (ProjectDataServiceException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    mainModel.setImmediateThread(new ImmediateThread(immediateModel, projectModel, logger, curTestRunName));
                    mainModel.getImmediateThread().start();
                } catch (ProjectDataServiceException e) {
                    e.printStackTrace();
                    immediateModel.getRequestResponseModel().setEditable(true);
                }
            }
            else {
                immediateModel.getRequestResponseModel().setEditable(true);
                stopImmediate();
            }
        });
        pnlImmediateView.pnlImmediateEditor.pnlImmediateSendToolbar.jbtnSend.addActionListener( actionEvent -> {
            WebsocketFrame injectedFrame = pnlImmediateView.pnlImmediateEditor.getWebsocketFrameController().getWebsocketFrameModel().getWebsocketFrame();
            injectedFrame.setDirection(WebsocketDirection.OUTBOUND);
            // Add the message to an existing proxy conversation
            if ( mainModel.getImmediateModel().getWebsocketSession() != null ) {
                ArrayList<WebsocketFrame> frames = new ArrayList<WebsocketFrame>();
                injectedFrame.setUpgradeMessageUUID(mainModel.getImmediateModel().getWebsocketSession().getUpgradeMsgUUID());
                frames.add(injectedFrame);
                mainModel.getImmediateModel().getWebsocketSession().injectImmediateFrame(frames);
            }
            // Add to an interactive immediate thread
            else {
                if ( mainModel.getImmediateThread() != null ) {
                    mainModel.getImmediateThread().enqueueFrame(injectedFrame);
                }
            }
        });
        pnlImmediateView.pnlImmediateEditor.pnlImmediateSendToolbar.jchkHandlePingPong.addActionListener( actionEvent -> {
            immediateModel.setAutoPingPong(pnlImmediateView.pnlImmediateEditor.pnlImmediateSendToolbar.jchkHandlePingPong.isSelected());
        });
        pnlImmediateView.pnlImmediateViewer.tblWebsocketConversation.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if ( pnlImmediateView.pnlImmediateViewer.tblWebsocketConversation.getSelectedRow() >= 0 ) {
                    String msgUuid = pnlImmediateView.pnlImmediateViewer.tblWebsocketConversation.getValueAt(pnlImmediateView.pnlImmediateViewer.tblWebsocketConversation.getSelectedRow(), 0).toString();
                    if ( msgUuid != null ) {
                        try {
                            WebsocketTrafficRecord rec = projectModel.getProjectDataService().getWebsocketTrafficRecordByUUID(msgUuid);
                            pnlImmediateView.pnlImmediateViewer.websocketFrameController.getWebsocketFrameModel().setWebsocketFrame(rec.getFrame());
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
                            pnlImmediateView.pnlImmediateViewer.pnlFrameViewer.pnlWebsocketFrameViewerToolbar.jlblFrameProperties.setText(frameStr);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (ProjectDataServiceException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public void addHttpMessageFromHistory(String messageId) {
        if ( mainModel.getImmediateThread() == null ) {
            stopImmediate();
        }
        HttpTrafficRecord rec = null;
        try {
            rec = projectModel.getProjectDataService().getHttpTrafficRecordByUUID(messageId);
        } catch (ProjectDataServiceException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if ( rec != null ) {
            immediateModel.getRequestResponseModel().setUrl(rec.getRequest().getUrl());
            immediateModel.getRequestResponseModel().setMethod(rec.getRequest().getHttpMethod());
            immediateModel.getRequestResponseModel().setHeaders(String.join("\r\n",rec.getRequest().getHeaders()));
            immediateModel.getRequestResponseModel().setBody(null);
            if ( rec.getRequest().getBodyBytes() != null ) {
                immediateModel.getRequestResponseModel().setBody(new String(rec.getRequest().getBodyBytes()));
            }

        }
    }

    public void addWebsocketFrameFromHistory( String messageId ) {
        WebsocketTrafficRecord rec = null;
        try {
            rec = projectModel.getProjectDataService().getWebsocketTrafficRecordByUUID(messageId);
            if ( rec != null ) {
                pnlImmediateView.pnlImmediateEditor.getWebsocketFrameController().getWebsocketFrameModel().setWebsocketFrame(rec.getFrame());
            }
        } catch (ProjectDataServiceException | IOException e) {
            e.printStackTrace();
        }

    }

    public void
    stopImmediate() {
        immediateModel.setConnectionStatusMsg("[Stopping]");
        if ( mainModel.getImmediateThread() != null ) {
            try {
                mainModel.getImmediateThread().shutdown();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if ( "ImmediateModel.insertDirection".equals(propertyChangeEvent.getPropertyName())) {
            if( immediateModel.getInsertDirection().equals(WebsocketDirection.INBOUND)) {
                pnlImmediateView.pnlImmediateEditor.pnlImmediateSendToolbar.radioSendToClient.setSelected(true);
            }
            else {
                pnlImmediateView.pnlImmediateEditor.pnlImmediateSendToolbar.radioSendToServer.setSelected(true);
            }
        }
        if ( "ImmediateModel.autoPingPong".equals(propertyChangeEvent.getPropertyName())) {
            pnlImmediateView.pnlImmediateEditor.pnlImmediateSendToolbar.jchkHandlePingPong.setSelected(immediateModel.isAutoPingPong());
        }
        if ( "MainModel.immediateThread".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() == null ) {
                pnlImmediateView.pnlImmediateEditor.immediateToolbar.btnConnect.setText("Connect");
                immediateModel.getRequestResponseModel().setEditable(true);
                immediateModel.setConnectionStatusMsg("[Not connected]");
            }
            else {
                pnlImmediateView.pnlImmediateEditor.immediateToolbar.btnConnect.setText("Disconnect");
                immediateModel.getRequestResponseModel().setEditable(false);
                immediateModel.setConnectionStatusMsg("[Connected]");
            }
        }
        if ( "HttpRequestResponseModel.upgradeScriptName".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() == null ) {
                pnlImmediateView.pnlImmediateEditor.getHttpRequestResponseController().getHttpRequestResponseModel().setResponse("");
            }
            else {
                pnlImmediateView.pnlImmediateEditor.getHttpRequestResponseController().getHttpRequestResponseModel().setResponse((String) propertyChangeEvent.getNewValue());
            }
        }
        if ( "ImmediateModel.connectionStatusMsg".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() != null ) {
                pnlImmediateView.pnlImmediateEditor.immediateToolbar.lblStatus.setText((String)propertyChangeEvent.getNewValue());
            }
            else {
                pnlImmediateView.pnlImmediateEditor.immediateToolbar.lblStatus.setText("");
            }
        }
        if ( "ImmediateModel.activeSessionNames".equals(propertyChangeEvent.getPropertyName())) {
            ArrayList<String> sessions = immediateModel.getActiveSessionNames();
            if ( sessions.size() > 0 ) {
                // Remove items
                ArrayList<String> currentItems = new ArrayList<String>();
                ArrayList<Integer> removeItems = new ArrayList<Integer>();
                for ( int i = 0; i < pnlImmediateView.pnlImmediateEditor.immediateToolbar.jcmbSessions.getItemCount(); i++ ) {
                    if ( !sessions.contains(pnlImmediateView.pnlImmediateEditor.immediateToolbar.jcmbSessions.getItemAt(i))) {
                        removeItems.add(i);
                    }
                    else {
                        currentItems.add((String) pnlImmediateView.pnlImmediateEditor.immediateToolbar.jcmbSessions.getItemAt(i));
                    }
                }
                Collections.sort(removeItems, Collections.reverseOrder());
                for ( Integer j : removeItems ) {
                    pnlImmediateView.pnlImmediateEditor.immediateToolbar.jcmbSessions.removeItemAt(j);
                }



                // Add items
                for ( String session : sessions ) {
                    if ( !currentItems.contains(session)) {
                        pnlImmediateView.pnlImmediateEditor.immediateToolbar.jcmbSessions.addItem(session);
                    }
                }
                if ( pnlImmediateView.pnlImmediateEditor.immediateToolbar.jcmbSessions.getItemCount() > 0 ) {
                    pnlImmediateView.pnlImmediateEditor.immediateToolbar.jchkJoinActiveSession.setEnabled(true);
                }
                else {
                    pnlImmediateView.pnlImmediateEditor.immediateToolbar.jchkJoinActiveSession.setEnabled(false);
                    pnlImmediateView.pnlImmediateEditor.immediateToolbar.jchkJoinActiveSession.setSelected(false);
                }

            }
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
