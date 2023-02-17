package com.wsproxy.mvc.model;

import com.wsproxy.util.GuiUtils;
import com.wsproxy.httpproxy.HttpMessage;
import com.wsproxy.httpproxy.websocket.WebsocketFrame;
import com.wsproxy.httpproxy.websocket.WebsocketFrameType;
import com.wsproxy.httpproxy.trafficlogger.*;

import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.nio.ByteBuffer;
import java.util.Date;

public class TrafficModel {
    private DefaultTableModel httpTrafficModel; // http traffic from proxy
    private DefaultTableModel websocketTrafficModel; // websocket traffic from proxy
    private DefaultTableModel websocketConnectionsModel; // websocket connections
    private String scopeRegex;
    private String excludeRegex;



    private SwingPropertyChangeSupport eventEmitter;

    public TrafficModel( ProjectModel projectModel ) {
        eventEmitter = new SwingPropertyChangeSupport(this);
        httpTrafficModel = new DefaultTableModel();
        websocketTrafficModel = new DefaultTableModel();
        websocketConnectionsModel = new DefaultTableModel();

        for ( String col: new String[] { "messageId", "highlightColour","Time", "Protocol", "Method","Code", "URL"}) {
            httpTrafficModel.addColumn(col);
        }
        for ( String col: new String[] { "messageId", "upgradeMessageId","highlight","Time","--","FIN","R1","R2","R3","MSK","OpCode","Length","Mask","Payload" }) {
            websocketTrafficModel.addColumn(col);
        }
        for ( String col: new String[] { "upgradeMessageId", "Time","State","URL"}) {
            websocketConnectionsModel.addColumn(col);
        }
    }

    public void setTrafficRowFilters( String scopeRegex, String excludeRegex) {
        this.scopeRegex = scopeRegex;
        this.excludeRegex = excludeRegex;
        eventEmitter.firePropertyChange("TrafficModel.rowFilterChange", null, null);
    }

    public String getScopeRegex() {
        return scopeRegex;
    }

    public void setScopeRegex(String scopeRegex) {
        String oldPattern = this.scopeRegex;
        this.scopeRegex = scopeRegex;
        eventEmitter.firePropertyChange("TrafficModel.scopeRegex", oldPattern, scopeRegex);
    }

    public String getExcludeRegex() {
        return excludeRegex;
    }

    public void setExcludeRegex(String excludeRegex) {
        String oldPattern = this.excludeRegex;
        this.excludeRegex = excludeRegex;
        eventEmitter.firePropertyChange("TrafficModel.excludeRegex", oldPattern, excludeRegex);
    }


    public void updateWebsocketConnections(TrafficRecord rec) {
        if ( rec.getTrafficSource().equals(TrafficSource.PROXY)) {
            HttpMessage msg = rec.getHttpTrafficRecord().getRequest();
            String wsUrl = msg.getUrl().replaceFirst("(?i)http","ws");
            websocketConnectionsModel.addRow(new Object[] {
                    msg.getMessageUUID(),
                    GuiUtils.trafficTimeFmt.format(new Date(msg.getCreateTime())),
                    "OPEN",
                    wsUrl
            });
            eventEmitter.firePropertyChange("TrafficModel.websocketConnectionsModel", null, null);
        }
    }

    public void addHttpTraffic(HttpTrafficRecord rec) {
        HttpMessage request = rec.getRequest();
        HttpMessage response = rec.getResponse();
        httpTrafficModel.addRow(new Object[] {
                request.getMessageUUID(),
                Color.WHITE,
                GuiUtils.trafficTimeFmt.format(new Date(request.getCreateTime())),
                request.getProtocol(),
                request.getHttpMethod(),
                response.getStatusCode(),
                request.getUrl()
        });
    }

    public void clearWebsocketTraffic() {
        websocketTrafficModel.setRowCount(0);
        eventEmitter.firePropertyChange("websocketTrafficModelRowCount", null, websocketTrafficModel.getRowCount());
    }

    public void clearHttpTraffic() {
        httpTrafficModel.setRowCount(0);
        eventEmitter.firePropertyChange("httpTrafficModelRowCount", null, httpTrafficModel.getRowCount());
    }

    public void clearConnections() {
        websocketConnectionsModel.setRowCount(0);
        eventEmitter.firePropertyChange("websocketConnectionsModelRowCount", null, websocketConnectionsModel.getRowCount());
    }

    public void addWebsocketTraffic (WebsocketFrame frame, Color highlightColor) {
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
        websocketTrafficModel.addRow(new Object[] {
                frame.getMessageUUID(),
                frame.getUpgradeMessageUUID(),
                highlightColor,
                GuiUtils.trafficTimeFmt.format(new Date(frame.getCreateTime())),
                frame.getDirection() == WebsocketDirection.INBOUND ? "←" : "→",
                frame.getFin(),
                frame.getRsv1(),
                frame.getRsv2(),
                frame.getRsv3(),
                frame.getMasked(),
                frame.getOpcode(),
                frame.getPayloadLength(),
                maskStr,
                payloadStr
        });
        eventEmitter.firePropertyChange("TrafficModel.websocketTrafficModel", null, null);
    }

    public DefaultTableModel getHttpTrafficModel() {
        return httpTrafficModel;
    }
    public DefaultTableModel getWebsocketTrafficModel() {
        return websocketTrafficModel;
    }
    public DefaultTableModel getWebsocketConnectionsModel() {
        return websocketConnectionsModel;
    }
    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
