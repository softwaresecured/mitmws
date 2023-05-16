package com.mitmws.mvc.model;

import com.mitmws.httpproxy.websocket.WebsocketFrame;
import com.mitmws.httpproxy.websocket.WebsocketFrameType;
import com.mitmws.httpproxy.trafficlogger.WebsocketDirection;
import com.mitmws.util.GuiUtils;

import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.table.DefaultTableModel;
import java.beans.PropertyChangeListener;

public class WebsocketFrameModel {
    public final int HEX_VIEWER_COLS = 16;
    private WebsocketFrame websocketFrame;
    private SwingPropertyChangeSupport eventEmitter;
    private DefaultTableModel payloadHexModel;
    private String displayFormat = "UTF-8";
    private String hexDocument = "";
    private String displayFormats[] = new String[]{"UTF-8","HEX"};

    private boolean isEditable = false;

    private int fin = 1;
    private int rsv1 = 0;
    private int rsv2 = 0;
    private int rsv3 = 0;
    private int masked = 0;
    private WebsocketFrameType opcode = null;
    int payloadLength = 0;
    private byte[] maskKey = null;
    private byte[] payload = null;

    private String EVENT_SOURCE = "WebsocketFrameModel";

    public WebsocketFrameModel( WebsocketFrame websocketFrame) {
        this.websocketFrame = websocketFrame;
        init();
        eventEmitter = new SwingPropertyChangeSupport(this);
    }

    public WebsocketFrameModel( WebsocketFrame websocketFrame, String EVENT_SOURCE) {
        this.websocketFrame = websocketFrame;
        this.EVENT_SOURCE = EVENT_SOURCE;
        init();
        eventEmitter = new SwingPropertyChangeSupport(this);
    }

    private void init() {
        payloadHexModel = new DefaultTableModel();
        for ( int i = 0; i < HEX_VIEWER_COLS; i++ ) {
            payloadHexModel.addColumn(String.format("%02x", i));
        }
    }
    public void newFrame() {
        WebsocketFrame newFrame = new WebsocketFrame();
        newFrame.setOpcode(WebsocketFrameType.TEXT);
        newFrame.setDirection(WebsocketDirection.OUTBOUND);
        newFrame.setMasked(1);
        newFrame.setMaskKey(newFrame.generateMaskBytes());
        setWebsocketFrame(newFrame);
    }

    public String getEventSourceName() {
        return EVENT_SOURCE;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
        eventEmitter.firePropertyChange(String.format("%s.isEditable", EVENT_SOURCE), null, this.isEditable);
    }

    public String[] getDisplayFormats() {
        return displayFormats;
    }

    public String getDisplayFormat() {
        return displayFormat;
    }

    public void setDisplayFormat(String displayFormat) {
        this.displayFormat = displayFormat;
        if ( displayFormat.equals("HEX")) {
            buildPayloadHexTableModel();
        }
        eventEmitter.firePropertyChange(String.format("%s.displayFormat", EVENT_SOURCE), null, this.displayFormat);
    }

    /*
        builds the table model for hex representation
     */
    public void buildPayloadHexTableModel() {
        payloadHexModel.setRowCount(0);
        String content = GuiUtils.binToHexStr(this.websocketFrame.getPayloadUnmasked());
        if ( content != null && content.length() > 0 ) {
            Object curRow[] = new Object[HEX_VIEWER_COLS];
            int j = 0;
            for ( int i = 0; i < content.length(); i += 2) {
                if ( j >= HEX_VIEWER_COLS ) {
                    payloadHexModel.addRow(curRow);
                    curRow = new Object[HEX_VIEWER_COLS];
                    j = 0;
                }
                curRow[j] = content.substring(i,i+2);
                j++;
            }
            if ( j > 0 ) {
                payloadHexModel.addRow(curRow);
            }
        }
    }

    public WebsocketFrame getWebsocketFrame() {
        return websocketFrame;
    }

    public String getHexDocument() {
        return hexDocument;
    }

    public void setHexDocument(String hexDocument) {
        this.hexDocument = hexDocument;
        eventEmitter.firePropertyChange(String.format("%s.displayFormat", EVENT_SOURCE), null, this.displayFormat);
    }

    public void setWebsocketFrame(WebsocketFrame websocketFrame) {
        WebsocketFrame oldVal = this.websocketFrame;
        this.websocketFrame = websocketFrame;
        eventEmitter.firePropertyChange(String.format("%s.websocketFrame", EVENT_SOURCE), null, this.websocketFrame);
    }


    public String getHexFormattedPayload() {
        String displayText = "";
        if ( this.websocketFrame != null && this.websocketFrame.getPayloadUnmasked() != null ) {
            displayText = GuiUtils.binToHexStr(this.websocketFrame.getPayloadUnmasked());
        }
        return displayText;
    }

    public String getHexStrFormattedPayload() {
        String displayText = "";
        if ( this.websocketFrame != null && this.websocketFrame.getPayloadUnmasked() != null ) {
            displayText = GuiUtils.getBinPreviewStr(this.websocketFrame.getPayloadUnmasked());
        }
        return displayText;
    }

    public DefaultTableModel getPayloadHexModel() {
        return payloadHexModel;
    }

    public int getFin() {
        return websocketFrame.getFin();
    }

    public void setFin(int fin) {
        websocketFrame.setFin(fin);
        eventEmitter.firePropertyChange(String.format("%s.fin", EVENT_SOURCE), null, this.fin);
    }

    public int getRsv1() {
        return websocketFrame.getRsv1();
    }

    public void setRsv1(int rsv1) {
        websocketFrame.setRsv1(rsv1);
        eventEmitter.firePropertyChange(String.format("%s.rsv1", EVENT_SOURCE), null, this.rsv1);
    }

    public int getRsv2() {
        return websocketFrame.getRsv2();
    }

    public void setRsv2(int rsv2) {
        websocketFrame.setRsv2(rsv2);
        eventEmitter.firePropertyChange(String.format("%s.rsv2", EVENT_SOURCE), null, this.rsv2);
    }

    public int getRsv3() {
        return websocketFrame.getRsv3();
    }

    public void setRsv3(int rsv3) {
        websocketFrame.setRsv3(rsv3);
        eventEmitter.firePropertyChange(String.format("%s.rsv3", EVENT_SOURCE), null, this.rsv3);
    }

    public int getMasked() {
        return websocketFrame.getMasked();
    }

    public void setMasked(int masked) {
        websocketFrame.setMasked(masked);
        eventEmitter.firePropertyChange(String.format("%s.masked", EVENT_SOURCE), null, this.masked);
    }

    public WebsocketFrameType getOpcode() {
        return websocketFrame.getOpcode();
    }

    public void setOpcode(WebsocketFrameType opcode) {
        websocketFrame.setOpcode(opcode);
        eventEmitter.firePropertyChange(String.format("%s.opcode", EVENT_SOURCE), null, this.opcode);
    }

    public int getPayloadLength() {
        return websocketFrame.getPayloadLength();
    }

    public void setPayloadLength(int payloadLength) {
        websocketFrame.setPayloadLength(payloadLength);
        eventEmitter.firePropertyChange(String.format("%s.payloadLength", EVENT_SOURCE), null, this.payloadLength);
    }

    public byte[] getMaskKey() {
        return websocketFrame.getMaskKey();
    }

    public void setMaskKey(byte[] maskKey) {
        websocketFrame.setMaskKey(maskKey);
        eventEmitter.firePropertyChange(String.format("%s.maskKey", EVENT_SOURCE), null, this.maskKey);
    }

    public byte[] getPayload() {
        byte buff[] = null;
        if ( websocketFrame != null ) {
            buff = websocketFrame.getPayloadUnmasked();
        }
        return buff;
    }

    public void setPayloadUnmasked(byte[] payload) {
        if ( websocketFrame != null ) {
            websocketFrame.setPayloadUnmasked(payload);
        }
        eventEmitter.firePropertyChange(String.format("%s.payload", EVENT_SOURCE), null, this.payload);
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
