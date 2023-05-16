package com.mitmws.mvc.controller;

import com.mitmws.anomalydetection.DetectedAnomaly;
import com.mitmws.projects.ProjectDataServiceException;
import com.mitmws.util.GuiUtils;
import com.mitmws.httpproxy.websocket.WebsocketFrame;
import com.mitmws.httpproxy.trafficlogger.*;
import com.mitmws.mvc.model.AnomaliesModel;
import com.mitmws.mvc.model.ProjectModel;
import com.mitmws.mvc.view.panels.anomalies.PnlAnomaliesView;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;

public class AnomaliesController implements PropertyChangeListener {

    private AnomaliesModel anomaliesModel;
    private ProjectModel projectModel;
    private PnlAnomaliesView pnlAnomaliesView;

    public AnomaliesController(AnomaliesModel anomaliesModel, ProjectModel projectModel, PnlAnomaliesView pnlAnomaliesView) {
        this.anomaliesModel = anomaliesModel;
        this.projectModel = projectModel;
        this.pnlAnomaliesView = pnlAnomaliesView;
        this.anomaliesModel.addListener(this);
        initEventListeners();
    }

    public void setSelectedAnomaly ( String anomalyId ) {
        for ( DetectedAnomaly anomaly : projectModel.getDetectedAnomalies()) {
            if ( anomaly.getAnomalyId().equals(anomalyId)) {
                anomaliesModel.setCurrentAnomaly(anomaly);
                pnlAnomaliesView.pnlDetectedAnomalies.jtxtAnomalyDescription.setText(anomaly.getDescription());
                pnlAnomaliesView.pnlDetectedAnomalies.updateAnomalyTitle(anomaly.getCredibility(), anomaly.getCWE(),anomaly.getTitle());
                break;
            }
        }
    }

    public void selectAnomalyFrame( String websocketMsgId ) {
        if ( websocketMsgId != null ) {
            if ( pnlAnomaliesView.pnlDetectedAnomalies.jtblWebsocketConversation.getRowCount() > 0 ) {
                for ( int i = 0; i < pnlAnomaliesView.pnlDetectedAnomalies.jtblWebsocketConversation.getRowCount(); i++ ) {
                    String curMsgId = (String) pnlAnomaliesView.pnlDetectedAnomalies.jtblWebsocketConversation.getValueAt(i,0);
                    if ( curMsgId.equals(websocketMsgId)) {
                        pnlAnomaliesView.pnlDetectedAnomalies.jtblWebsocketConversation.setRowSelectionInterval(i,i);
                        break;
                    }
                }
            }
        }
    }

    public void initEventListeners() {
        // Conversation table
        pnlAnomaliesView.pnlDetectedAnomalies.jtblWebsocketConversation.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            if ( pnlAnomaliesView.pnlDetectedAnomalies.jtblWebsocketConversation.getSelectedRow() >= 0 ) {
                String messageId = (String) pnlAnomaliesView.pnlDetectedAnomalies.jtblWebsocketConversation.getValueAt(pnlAnomaliesView.pnlDetectedAnomalies.jtblWebsocketConversation.getSelectedRow(),0);
                try {
                    pnlAnomaliesView.pnlDetectedAnomalies.getWebsocketFrameController().getWebsocketFrameModel().setWebsocketFrame(null);
                    WebsocketTrafficRecord rec = null;
                    rec = projectModel.getProjectDataService().getWebsocketTrafficRecordByUUID(messageId);
                    if ( rec != null ) {
                        String maskStr = "--";
                        if ( rec.getFrame().getMaskKey() != null ) {
                            maskStr = Integer.toHexString(ByteBuffer.wrap(rec.getFrame().getMaskKey()).getInt());
                        }

                        byte[] payloadViewText = new byte[0];
                        if ( rec.getFrame().getPayloadUnmasked() != null ) {
                            payloadViewText = rec.getFrame().getPayloadUnmasked();
                        }
                        String frameStr = String.format("F:%d/R1:%d/R2:%d/R3:%d/OP:%s/M:%s",
                                rec.getFrame().getFin(),
                                rec.getFrame().getRsv1(),
                                rec.getFrame().getRsv2(),
                                rec.getFrame().getRsv3(),
                                rec.getFrame().getOpcode().toString(),
                                maskStr);
                        pnlAnomaliesView.pnlDetectedAnomalies.pnlFrameViewer.pnlWebsocketFrameViewerToolbar.jlblFrameProperties.setText(frameStr);
                        pnlAnomaliesView.pnlDetectedAnomalies.getWebsocketFrameController().getWebsocketFrameModel().setWebsocketFrame(rec.getFrame());
                    }
                } catch (IOException | ProjectDataServiceException e) {
                    e.printStackTrace();
                }


                if ( anomaliesModel.getCurrentAnomaly().getWebsocketMsgId() != null ) {
                    if (anomaliesModel.getCurrentAnomaly().getWebsocketMsgId().equals(messageId)) {
                        pnlAnomaliesView.pnlDetectedAnomalies.pnlFrameViewer.jtxtFramePayload.setSelectionColor(Color.YELLOW);
                        pnlAnomaliesView.pnlDetectedAnomalies.pnlFrameViewer.jtxtFramePayload.setSelectedTextColor(Color.BLACK);
                        pnlAnomaliesView.pnlDetectedAnomalies.pnlFrameViewer.jtxtFramePayload.requestFocus();
                        pnlAnomaliesView.pnlDetectedAnomalies.pnlFrameViewer.jtxtFramePayload.select(anomaliesModel.getCurrentAnomaly().getHighlightPosStart(),anomaliesModel.getCurrentAnomaly().getHighlightPosEnd());
                    }
                }
            }
        });

        // Anomaly table
        pnlAnomaliesView.pnlDetectedAnomalies.jtblDetectedAnomalies.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            anomaliesModel.getConversationTableModel().setRowCount(0);
            if ( pnlAnomaliesView.pnlDetectedAnomalies.jtblDetectedAnomalies.getSelectedRow() >= 0 ) {
                String selectedAnomlyId = (String) pnlAnomaliesView.pnlDetectedAnomalies.jtblDetectedAnomalies.getValueAt(pnlAnomaliesView.pnlDetectedAnomalies.jtblDetectedAnomalies.getSelectedRow(), 0);
                setSelectedAnomaly(selectedAnomlyId);
                if ( anomaliesModel.getCurrentAnomaly() != null ) {
                    if ( anomaliesModel.getCurrentAnomaly().getRecords() != null ) {
                        loadTraffic(anomaliesModel.getCurrentAnomaly().getRecords());
                    }
                    else {
                        loadTrafficFromDb(anomaliesModel.getCurrentAnomaly().getConversationUuid());
                    }
                    selectAnomalyFrame(anomaliesModel.getCurrentAnomaly().getWebsocketMsgId());
                }
            }
        });
    }

    public void loadTraffic ( ArrayList<WebsocketTrafficRecord> records ) {
        for ( WebsocketTrafficRecord rec : records ) {
            WebsocketFrame frame = rec.getFrame();
            anomaliesModel.getConversationTableModel().addRow(new Object[] {
                    frame.getMessageUUID(),
                    GuiUtils.trafficTimeFmt.format(new Date(frame.getCreateTime())),
                    frame.getDirection() == WebsocketDirection.INBOUND ? "←" : "→",
                    frame.getOpcode(),
                    frame.getPayloadLength(),
                    frame.getPayloadString()
            });
        }
    }

    public void loadTrafficFromDb(String conversationUUID ) {
        try {
            ArrayList<WebsocketTrafficRecord> records = projectModel.getProjectDataService().getWebsocketTrafficRecordByConversationUUID(conversationUUID);
            loadTraffic(records);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ProjectDataServiceException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if ( "AnomaliesModel.currentAnomaly".equals(propertyChangeEvent.getPropertyName())) {
            pnlAnomaliesView.pnlDetectedAnomalies.jtxtRawFrame.setText("");
            if ( propertyChangeEvent.getNewValue() != null ) {
                if ( anomaliesModel.getCurrentAnomaly().getTestPayloadHexStr() != null ) {
                    pnlAnomaliesView.pnlDetectedAnomalies.jtxtRawFrame.setText(anomaliesModel.getCurrentAnomaly().getTestPayloadHexStr());
                    pnlAnomaliesView.pnlDetectedAnomalies.pnlRawFrameViewer.setVisible(true);
                }
            }
            else {
                pnlAnomaliesView.pnlDetectedAnomalies.pnlRawFrameViewer.setVisible(false);
            }
        }
        if ( "AnomaliesModel.anomaliesTableModel".equals(propertyChangeEvent.getPropertyName())) {
            // Select a row if not selected
            if (pnlAnomaliesView.pnlDetectedAnomalies.jtblDetectedAnomalies.getSelectedRows().length == 0 )
            {
                int interval = pnlAnomaliesView.pnlDetectedAnomalies.jtblDetectedAnomalies.getRowCount() - 1;
                pnlAnomaliesView.pnlDetectedAnomalies.jtblDetectedAnomalies.setRowSelectionInterval(interval, interval);
            }
        }
    }
}
