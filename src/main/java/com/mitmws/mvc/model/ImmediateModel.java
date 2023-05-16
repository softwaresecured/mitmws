package com.mitmws.mvc.model;

import com.mitmws.httpproxy.websocket.WebsocketFrame;
import com.mitmws.httpproxy.websocket.WebsocketFrameType;
import com.mitmws.httpproxy.websocket.WebsocketSession;
import com.mitmws.httpproxy.trafficlogger.WebsocketDirection;
import com.mitmws.util.GuiUtils;

import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.table.DefaultTableModel;
import java.beans.PropertyChangeListener;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;

public class ImmediateModel {
    private ArrayList<String> activeSessionNames = new ArrayList<String>();
    private WebsocketFrameModel websocketFrameModel;
    private HttpRequestResponseModel requestResponseModel;
    private DefaultTableModel websocketTrafficTableModel; // websocket traffic from proxy
    private WebsocketDirection insertDirection = WebsocketDirection.OUTBOUND;
    private boolean autoPingPong = true;
    private WebsocketSession websocketSession = null;
    private SwingPropertyChangeSupport eventEmitter;
    private String connectionStatusMsg = "[Not connected]";
    public ImmediateModel() {
        websocketFrameModel = new WebsocketFrameModel(null);
        requestResponseModel = new HttpRequestResponseModel();
        websocketTrafficTableModel = new DefaultTableModel();
        for ( String col: new String[] { "messageId", "Time","Test name","--","OPCODE","LEN","Payload" }) {
            websocketTrafficTableModel.addColumn(col);
        }
        eventEmitter = new SwingPropertyChangeSupport(this);
    }

    public String getConnectionStatusMsg() {
        return connectionStatusMsg;
    }

    public void setConnectionStatusMsg(String connectionStatusMsg) {
        this.connectionStatusMsg = connectionStatusMsg;
        eventEmitter.firePropertyChange("ImmediateModel.connectionStatusMsg", null, this.connectionStatusMsg);
    }

    public ArrayList<String> getActiveSessionNames() {
        return activeSessionNames;
    }

    public void setActiveSessionNames(ArrayList<String> activeSessionNames) {
        this.activeSessionNames = activeSessionNames;
        eventEmitter.firePropertyChange("ImmediateModel.activeSessionNames", null, this.activeSessionNames);
    }

    public WebsocketDirection getInsertDirection() {
        return insertDirection;
    }

    public void setInsertDirection(WebsocketDirection insertDirection) {
        this.insertDirection = insertDirection;
        eventEmitter.firePropertyChange("ImmediateModel.insertDirection", null, this.insertDirection);
    }

    public boolean isAutoPingPong() {
        return autoPingPong;
    }

    public void setAutoPingPong(boolean autoPingPong) {
        this.autoPingPong = autoPingPong;
        eventEmitter.firePropertyChange("ImmediateModel.autoPingPong", null, this.autoPingPong);
    }

    public WebsocketSession getWebsocketSession() {
        return websocketSession;
    }

    public void setWebsocketSession(WebsocketSession websocketSession) {
        this.websocketSession = websocketSession;
        eventEmitter.firePropertyChange("ImmediateModel.websocketSession", null, this.websocketSession);
    }

    public void addWebsocketTraffic (WebsocketFrame frame, String testName) {
        String payloadStr = "";
        if ( frame.getPayloadUnmasked() != null ) {
            if (!frame.getOpcode().equals(WebsocketFrameType.CLOSE)) {
                payloadStr = GuiUtils.getTableBinPreviewStr(frame);
            }
            else {
                if ( frame.getPayloadUnmasked().length == 2 ) {
                    int closeCode = ByteBuffer.wrap(frame.getPayloadUnmasked()).getShort();
                    payloadStr = String.format("Code: %d", closeCode);
                }
            }
        }

        String maskStr = "--";
        if ( frame.getMaskKey() != null ) {
            maskStr = Integer.toHexString(ByteBuffer.wrap(frame.getMaskKey()).getInt());
        }
        websocketTrafficTableModel.addRow(new Object[] {
                frame.getMessageUUID(),
                GuiUtils.trafficTimeFmt.format(new Date(frame.getCreateTime())),
                testName,
                frame.getDirection() == WebsocketDirection.INBOUND ? "←" : "→",
                frame.getOpcode(),
                frame.getPayloadLength(),
                payloadStr
        });
    }

    public void setRequestResponseModel(HttpRequestResponseModel requestResponseModel) {
        this.requestResponseModel = requestResponseModel;
    }

    public WebsocketFrameModel getWebsocketFrameModel() {
        return websocketFrameModel;
    }

    public HttpRequestResponseModel getRequestResponseModel() {
        return requestResponseModel;
    }

    public DefaultTableModel getWebsocketTrafficTableModel() {
        return websocketTrafficTableModel;
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
