package com.wsproxy.mvc.controller;
import com.wsproxy.httpproxy.websocket.WebsocketFrameType;
import com.wsproxy.mvc.model.WebsocketFrameModel;
import com.wsproxy.mvc.view.panels.trafficpanel.PnlWebsocketFrameView;
import com.wsproxy.util.GuiUtils;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/*
    The viewer/editor for websocket frames
 */
public class WebsocketFrameController implements PropertyChangeListener  {
    private WebsocketFrameModel websocketFrameModel;
    private PnlWebsocketFrameView pnlWebsocketFrameView;

    public WebsocketFrameController(WebsocketFrameModel websocketFrameModel, PnlWebsocketFrameView pnlWebsocketFrameView) {
        this.websocketFrameModel = websocketFrameModel;
        this.pnlWebsocketFrameView = pnlWebsocketFrameView;
        this.websocketFrameModel.addListener(this);
        toggleToolbar();
        initEventListeners();
        updateDisplay();
    }

    /*
        Pick the proper editor/viewer for the frame type based on display format
     */
    public void updateDisplay() {
        pnlWebsocketFrameView.pnlTextViewer.setVisible(false);
        pnlWebsocketFrameView.pnlHexViewer.setVisible(false);
        if ( this.websocketFrameModel.getDisplayFormat().equals("HEX")) {
            pnlWebsocketFrameView.pnlHexViewer.setVisible(true);
        }
        if ( this.websocketFrameModel.getDisplayFormat().equals("UTF-8")) {
            pnlWebsocketFrameView.pnlTextViewer.setVisible(true);
        }
    }


    public void initEventListeners() {

        websocketFrameModel.getPayloadHexModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {

                if ( e.getFirstRow() >= 0 && e.getColumn() >= 0 ) {
                    StringBuilder sb = new StringBuilder();
                    for ( int i = 0; i < websocketFrameModel.getPayloadHexModel().getRowCount(); i++ ) {
                        for ( int j = 0; j < websocketFrameModel.getPayloadHexModel().getColumnCount(); j++ ) {
                            String hexChar = (String) websocketFrameModel.getPayloadHexModel().getValueAt(i,j);
                            if ( hexChar != null ) {
                                sb.append(hexChar);
                            }
                        }
                    }
                    byte buff[] = GuiUtils.parseHexString(sb.toString());
                    if ( buff != null ) {
                        websocketFrameModel.setPayloadUnmasked(buff);
                    }
                }
            }
        });

        pnlWebsocketFrameView.pnlWebsocketFrameViewerToolbar.jcmbDisplayFormat.addActionListener(
                actionEvent -> websocketFrameModel.setDisplayFormat(
                        pnlWebsocketFrameView.pnlWebsocketFrameViewerToolbar.jcmbDisplayFormat.getSelectedItem().toString()
                )
        );

        /*
            Flags/opcode for the frame
         */
        pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jchkFinFlag.addActionListener( actionEvent -> {
            websocketFrameModel.setFin(pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jchkFinFlag.isSelected() ? 1 : 0);
        });
        pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jchkRsv1Flag.addActionListener( actionEvent -> {
            websocketFrameModel.setRsv1(pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jchkRsv1Flag.isSelected() ? 1 : 0);
        });
        pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jchkRsv2Flag.addActionListener( actionEvent -> {
            websocketFrameModel.setRsv2(pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jchkRsv2Flag.isSelected() ? 1 : 0);
        });
        pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jchkRsv3Flag.addActionListener( actionEvent -> {
            websocketFrameModel.setRsv3(pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jchkRsv3Flag.isSelected() ? 1 : 0);
        });
        pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jcmbOpcode.addActionListener( actionEvent -> {
            websocketFrameModel.setOpcode(WebsocketFrameType.valueOf(pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jcmbOpcode.getSelectedItem().toString()));
        });


        /*
            Changes made in the UTF-8 wysiwyg editor
         */
        pnlWebsocketFrameView.jtxtFramePayload.getDocument().addDocumentListener(new DocumentListener() {
            private void updateDocument() {
                if ( websocketFrameModel.getDisplayFormat().equals("UTF-8") && websocketFrameModel.isEditable()) {
                    if ( websocketFrameModel.getWebsocketFrame() != null ) {
                        websocketFrameModel.setPayloadUnmasked(pnlWebsocketFrameView.jtxtFramePayload.getText().getBytes());
                    }
                }
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateDocument();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateDocument();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });

    }

    public WebsocketFrameModel getWebsocketFrameModel() {
        return websocketFrameModel;
    }

    public void toggleToolbar() {

        for (JPanel pnl : new JPanel[]{pnlWebsocketFrameView.pnlWsFrameEditorToolbar, pnlWebsocketFrameView.pnlWebsocketFrameViewerToolbar }) {
            for (Component c : pnl.getComponents() ) {
                c.setEnabled(websocketFrameModel.getWebsocketFrame() == null ? false : true );
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if (String.format("%s.websocketFrame", websocketFrameModel.getEventSourceName()).equals(propertyChangeEvent.getPropertyName())) {
            int payloadLen = 0;
            pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jchkFinFlag.setSelected(false);
            pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jchkRsv1Flag.setSelected(false);
            pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jchkRsv2Flag.setSelected(false);
            pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jchkRsv3Flag.setSelected(false);
            toggleToolbar();

            if ( websocketFrameModel.getWebsocketFrame() != null ) {
                pnlWebsocketFrameView.lblDirection.setVisible(true);
                // Set the display format
                if (websocketFrameModel.getWebsocketFrame().getOpcode().equals(WebsocketFrameType.BINARY) || websocketFrameModel.getWebsocketFrame().getOpcode().equals(WebsocketFrameType.CLOSE) | websocketFrameModel.getWebsocketFrame().getOpcode().equals(WebsocketFrameType.CONTINUATION)) {
                    websocketFrameModel.setDisplayFormat("HEX");
                }
                else {
                    websocketFrameModel.setDisplayFormat("UTF-8");
                }
                GuiUtils.setComboBoxItem(pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jcmbOpcode,getWebsocketFrameModel().getOpcode().toString());
                pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jchkFinFlag.setSelected(websocketFrameModel.getWebsocketFrame().getFin() == 1 ? true : false );
                pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jchkRsv1Flag.setSelected(websocketFrameModel.getWebsocketFrame().getRsv1() == 1 ? true : false );
                pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jchkRsv2Flag.setSelected(websocketFrameModel.getWebsocketFrame().getRsv2() == 1 ? true : false );
                pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jchkRsv3Flag.setSelected(websocketFrameModel.getWebsocketFrame().getRsv3() == 1 ? true : false );

                pnlWebsocketFrameView.lblPayloadLen.setText(String.format(" ( %d bytes )", payloadLen ));
                pnlWebsocketFrameView.lblPayloadLen.setVisible(true);
            }
            updatePayloadEditors();
        }
        if (String.format("%s.displayFormat", websocketFrameModel.getEventSourceName()).equals(propertyChangeEvent.getPropertyName())) {
            updateDisplay();
            updatePayloadEditors();
        }

        if (String.format("%s.fin", websocketFrameModel.getEventSourceName()).equals(propertyChangeEvent.getPropertyName())) {
            pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jchkFinFlag.setSelected(websocketFrameModel.getFin() == 1 ? true : false);
        }
        if (String.format("%s.rsv1", websocketFrameModel.getEventSourceName()).equals(propertyChangeEvent.getPropertyName())) {
            pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jchkRsv1Flag.setSelected(websocketFrameModel.getRsv1() == 1 ? true : false);
        }

        if (String.format("%s.rsv2", websocketFrameModel.getEventSourceName()).equals(propertyChangeEvent.getPropertyName())) {
            pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jchkRsv2Flag.setSelected(websocketFrameModel.getRsv2() == 1 ? true : false);
        }

        if (String.format("%s.rsv3", websocketFrameModel.getEventSourceName()).equals(propertyChangeEvent.getPropertyName())) {
            pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jchkRsv3Flag.setSelected(websocketFrameModel.getRsv3() == 1 ? true : false);
        }

        if (String.format("%s.opcode", websocketFrameModel.getEventSourceName()).equals(propertyChangeEvent.getPropertyName())) {
            GuiUtils.setComboBoxItem(pnlWebsocketFrameView.pnlWsFrameEditorToolbar.jcmbOpcode,websocketFrameModel.getOpcode().toString());
            if ( websocketFrameModel.getOpcode().equals(WebsocketFrameType.TEXT) | websocketFrameModel.getOpcode().equals(WebsocketFrameType.PING) | websocketFrameModel.getOpcode().equals(WebsocketFrameType.PONG) ) {
                GuiUtils.setComboBoxItem(pnlWebsocketFrameView.pnlWebsocketFrameViewerToolbar.jcmbDisplayFormat,"UTF-8");
            }
            else {
                GuiUtils.setComboBoxItem(pnlWebsocketFrameView.pnlWebsocketFrameViewerToolbar.jcmbDisplayFormat,"HEX");
            }
        }
        if (String.format("%s.hexDocument", websocketFrameModel.getEventSourceName()).equals(propertyChangeEvent.getPropertyName())) {

        }
        if (String.format("%s.payload", websocketFrameModel.getEventSourceName()).equals(propertyChangeEvent.getPropertyName())) {
            pnlWebsocketFrameView.lblPayloadLen.setText(String.format(" ( %d bytes )", websocketFrameModel.getPayload().length ));
            pnlWebsocketFrameView.jtxtFramePayloadHexStr.setText(getWebsocketFrameModel().getHexStrFormattedPayload());
            websocketFrameModel.buildPayloadHexTableModel();
        }
        if (String.format("%s.isEditable", websocketFrameModel.getEventSourceName()).equals(propertyChangeEvent.getPropertyName())) {
            pnlWebsocketFrameView.jtxtFramePayload.setEditable((Boolean) propertyChangeEvent.getNewValue());
        }
    }

    public void updatePayloadEditors() {
        String payloadStr = "";
        if ( websocketFrameModel.getPayload() != null ) {
            pnlWebsocketFrameView.lblPayloadLen.setText(String.format(" ( %d bytes )", websocketFrameModel.getPayload().length ));
            if ( websocketFrameModel.getDisplayFormat().equals("HEX")) {
                pnlWebsocketFrameView.jtxtFramePayloadHexStr.setText(getWebsocketFrameModel().getHexStrFormattedPayload());
            }
            else if ( websocketFrameModel.getDisplayFormat().equals("UTF-8")) {
                payloadStr = new String(websocketFrameModel.getPayload());
                pnlWebsocketFrameView.jtxtFramePayload.setText(payloadStr);
            }
        }
        else {
            pnlWebsocketFrameView.lblPayloadLen.setText(String.format(" ( %d bytes )", 0 ));
            pnlWebsocketFrameView.jtxtFramePayloadHexStr.setText("");
            pnlWebsocketFrameView.jtxtFramePayload.setText("");
        }
    }
}
