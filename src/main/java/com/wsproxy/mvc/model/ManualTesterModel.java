package com.wsproxy.mvc.model;

import com.wsproxy.tester.TestSequenceItemType;
import com.wsproxy.util.GuiUtils;
import com.wsproxy.httpproxy.websocket.WebsocketFrame;
import com.wsproxy.httpproxy.websocket.WebsocketFrameType;
import com.wsproxy.httpproxy.trafficlogger.WebsocketDirection;
import com.wsproxy.tester.ManualTestRun;
import com.wsproxy.tester.TestSequenceItem;

import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.nio.ByteBuffer;
import java.util.Date;

public class ManualTesterModel {
    private DefaultTableModel testLogTableModel;
    private DefaultTableModel websocketConversationHistoryTableModel;
    private DefaultTableModel websocketConversationTableModel;
    private ProjectModel projectModel;
    private SwingPropertyChangeSupport eventEmitter;

    // The currently selected items
    private ManualTestRun currentManualTestRun;
    private TestSequenceItem currentTestSequenceItem;

    private String savedManualRequestMethod = "";
    private String savedManualRequestURL = "";
    private String savedManualRequestHeaders = "";
    private String savedManualRequestBody = "";
    private ManualTestExecutionModel manualTestExecutionModel;
    private String connectionStatusString = "[Not connected]";


    public ManualTesterModel( ProjectModel projectModel) {
        eventEmitter = new SwingPropertyChangeSupport(this);
        this.projectModel = projectModel;
        testLogTableModel = new DefaultTableModel();
        websocketConversationHistoryTableModel = new DefaultTableModel();
        websocketConversationTableModel = new DefaultTableModel();
        manualTestExecutionModel = new ManualTestExecutionModel();

        for ( String col: new String[] { "Time","Level","Test name","Message" }) {
            testLogTableModel.addColumn(col);
        }
        for ( String col: new String[] { "messageId", "Time","Test name","--","OPCODE","LEN","Payload" }) {
            websocketConversationHistoryTableModel.addColumn(col);
        }

        for ( String col: new String[] { "messageId", "upgradeMessageId","testId","Type", "Delay", "--","FIN","R1","R2","R3","MSK","OpCode","Length","Mask","Payload" }) {
            websocketConversationTableModel.addColumn(col);
        }
    }

    public String getConnectionStatusString() {
        return connectionStatusString;
    }

    public void setConnectionStatusString(String connectionStatusString) {
        this.connectionStatusString = connectionStatusString;
        eventEmitter.firePropertyChange("ManualTesterModel.connectionStatusString", null, this.connectionStatusString);
    }

    public ManualTestExecutionModel getManualTestExecutionModel() {
        return manualTestExecutionModel;
    }

    public TestSequenceItem getCurrentTestSequenceItem() {
        return currentTestSequenceItem;
    }

    public void setCurrentTestSequenceItem(TestSequenceItem currentTestSequenceItem) {
        this.currentTestSequenceItem = currentTestSequenceItem;
        eventEmitter.firePropertyChange("ManualTesterModel.currentTestSequenceItem", null, this.currentTestSequenceItem);
    }

    public void setCurrentTestSequenceItemDelay( int delay ) {
        if ( getCurrentTestSequenceItem() != null ) {
            getCurrentTestSequenceItem().setDelayMsec(delay);
            eventEmitter.firePropertyChange("ManualTesterModel.currentTestSequenceItem.delay", null, delay);
        }
    }

    public void setCurrentTestSequenceItemType( TestSequenceItemType type ) {
        if ( getCurrentTestSequenceItem() != null ) {
            getCurrentTestSequenceItem().setTestSequenceItemType(type);
            eventEmitter.firePropertyChange("ManualTesterModel.currentTestSequenceItem.type", null, type);
        }
    }

    public ManualTestRun getCurrentManualTestRun() {
        return currentManualTestRun;
    }

    public void setCurrentManualTestRun(ManualTestRun currentManualTestRun) {
        this.currentManualTestRun = currentManualTestRun;
        eventEmitter.firePropertyChange("ManualTesterModel.currentManualTestRun", null, this.currentManualTestRun);
    }

    public DefaultTableModel getTestLogTableModel() {
        return testLogTableModel;
    }

    public void deleteTestSeqenceItem(TestSequenceItem item ) {

    }

    public void saveTestSequenceItem( TestSequenceItem item ) {

    }

    public String getSavedManualRequestMethod() {
        return savedManualRequestMethod;
    }

    public void setSavedManualRequestMethod(String savedManualRequestMethod) {
        this.savedManualRequestMethod = savedManualRequestMethod;
    }

    public String getSavedManualRequestURL() {
        return savedManualRequestURL;
    }

    public void setSavedManualRequestURL(String savedManualRequestURL) {
        this.savedManualRequestURL = savedManualRequestURL;
    }

    public String getSavedManualRequestHeaders() {
        return savedManualRequestHeaders;
    }

    public void setSavedManualRequestHeaders(String savedManualRequestHeaders) {
        this.savedManualRequestHeaders = savedManualRequestHeaders;
    }

    public String getSavedManualRequestBody() {
        return savedManualRequestBody;
    }

    public void setSavedManualRequestBody(String savedManualRequestBody) {
        this.savedManualRequestBody = savedManualRequestBody;
    }

    public DefaultTableModel getWebsocketConversationHistoryTableModel() {
        return websocketConversationHistoryTableModel;
    }
    public DefaultTableModel getWebsocketConversationTableModel() {
        return websocketConversationTableModel;
    }

    public ProjectModel getProjectModel() {
        return projectModel;
    }

    public void addTestLogMessage( String level, String testName, String message) {
        testLogTableModel.addRow(new Object[] {
                GuiUtils.trafficTimeFmt.format(System.currentTimeMillis()),
                level,
                testName,
                message
        });
    }

    public void addWebsocketTraffic (WebsocketFrame frame, String testName, Color highlightColor) {
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
        websocketConversationHistoryTableModel.addRow(new Object[] {
                frame.getMessageUUID(),
                GuiUtils.trafficTimeFmt.format(new Date(frame.getCreateTime())),
                testName,
                frame.getDirection() == WebsocketDirection.INBOUND ? "←" : "→",
                frame.getOpcode(),
                frame.getPayloadLength(),
                payloadStr
        });
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
