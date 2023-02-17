package com.wsproxy.mvc.model;

import com.wsproxy.httpproxy.HttpMessage;
import com.wsproxy.httpproxy.trafficlogger.WebsocketDirection;
import com.wsproxy.httpproxy.websocket.WebsocketFrame;
import com.wsproxy.httpproxy.websocket.WebsocketFrameType;
import com.wsproxy.tester.RawWebsocketFrame;
import com.wsproxy.util.FileUtils;
import com.wsproxy.util.GuiUtils;

import javax.swing.*;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.table.DefaultTableModel;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

/*
    - Does not save to db.
    - Only reports interesting stuff
    - Progress update every 1/10
    - Tests upgrade process
    - Tests websocket frames
    - Phase 1: Script based tests, few small ones, recreate the rachet issue etc
    - Phase 2: Zzuf integration, multi-thread
 */
public class ProtocolTesterModel {

    private ArrayList<WebsocketFrameType> testFrameTypes = new ArrayList<WebsocketFrameType>();
    private ArrayList<String> logOutput = new ArrayList<String>();
    private DefaultListModel logListModel = new DefaultListModel();
    private FuzzRecordModel currentFuzzRecord = null;
    public HttpRequestResponseModel httpRequestResponseModel = new HttpRequestResponseModel();
    private String upgradeHelperScript = null;
    private HttpMessage upgradeHttpMessage = null;
    private String lastHttpTxRx = "";
    private String testWebsocketBinaryPayload = null;
    private String testWebsocketTextPayload = null;
    private String testStatus = "STOPPED";
    private int testsCompleted = 0;
    private int totalTests = 0;
    private SwingPropertyChangeSupport eventEmitter;
    private final int MAX_TRAFFIC_HISTORY_SIZE = 100;
    private DefaultTableModel testTrafficTableModel;
    private String rawFrameHexStr = null;
    private Path currentTestFile = null;

    public ProtocolTesterModel() throws IOException {
        testTrafficTableModel = new DefaultTableModel();
        for ( String col: new String[] { "id","conversationId", "Time", "--", "Frame preview"}) {
            testTrafficTableModel.addColumn(col);
        }
        createTestFile();
        eventEmitter = new SwingPropertyChangeSupport(this);
    }

    public void createTestFile() throws IOException {
        currentTestFile = Files.createTempFile("wsproxy",".fuzz");
    }

    public void writeFuzzRecord( FuzzRecordModel fuzzRecordModel ) throws IOException {
        byte fuzzRecord[] = fuzzRecordModel.toFuzzRecord();
        if ( fuzzRecord != null ) {
            FileWriter fileWriter = new FileWriter(currentTestFile.toFile().getAbsoluteFile(),true);
            fileWriter.write(String.format("%s\n", new String(fuzzRecordModel.toFuzzRecord())));
            fileWriter.flush();
            fileWriter.close();
        }
    }



    public FuzzRecordModel fetchFuzzRecord( String id ) throws IOException {
        FuzzRecordModel fuzzRecordModel = null;
        Reader r = new FileReader(currentTestFile.toFile().getAbsoluteFile());
        BufferedReader br = new BufferedReader(r);
        String currentRec = null;
        while ( (currentRec = br.readLine()) != null ) {
            if ( currentRec.startsWith(id)) {
                fuzzRecordModel = new FuzzRecordModel(currentRec);
                break;
            }
        }
        r.close();
        return fuzzRecordModel;
    }
    

    public int getFuzzRecordCount() throws IOException {
        int count = 0;
        Reader r = new FileReader(currentTestFile.toFile().getAbsoluteFile());
        BufferedReader br = new BufferedReader(r);
        String currentRec = null;
        while ( (currentRec = br.readLine()) != null ) {
            count += 1;
        }
        r.close();
        return count;
    }

    public ArrayList<FuzzRecordModel> getFuzzRecords( int start, int length ) throws IOException {
        ArrayList<FuzzRecordModel> fuzzRecords = new ArrayList<FuzzRecordModel>();
        FuzzRecordModel fuzzRecordModel = null;
        Reader r = new FileReader(currentTestFile.toFile().getAbsoluteFile());
        BufferedReader br = new BufferedReader(r);
        String currentRec = null;
        int pos = 0;
        while ( (currentRec = br.readLine()) != null ) {
            if ( pos >= start ) {
                fuzzRecordModel = new FuzzRecordModel(currentRec);
                if ( fuzzRecordModel != null ) {
                    fuzzRecords.add(fuzzRecordModel);
                }
            }
            if ( pos >= start + length ) {
                break;
            }
            pos += 1;
        }
        r.close();
        return fuzzRecords;
    }

    public DefaultTableModel getTestTrafficTableModel() {
        return testTrafficTableModel;
    }

    public void setTestTrafficTableModel(DefaultTableModel testTrafficTableModel) {
        this.testTrafficTableModel = testTrafficTableModel;
    }

    public HttpRequestResponseModel getHttpRequestResponseModel() {
        return httpRequestResponseModel;
    }

    public String getUpgradeHelperScript() {
        return upgradeHelperScript;
    }

    public String getLastHttpTxRx() {
        return lastHttpTxRx;
    }

    public void setLastHttpTxRx(String lastHttpTxRx) {
        this.lastHttpTxRx = lastHttpTxRx;
        eventEmitter.firePropertyChange("ProtocolTesterModel.lastHttpTxRx", null, this.logOutput);
    }

    public String getTestStatus() {
        return testStatus;
    }

    public void setTestStatus(String testStatus) {
        this.testStatus = testStatus;
        eventEmitter.firePropertyChange("ProtocolTesterModel.testStatus", null, this.testStatus);
    }

    public int getTestsCompleted() {
        return testsCompleted;
    }

    public void setTestsCompleted(int testsCompleted) {
        this.testsCompleted = testsCompleted;
        eventEmitter.firePropertyChange("ProtocolTesterModel.testsCompleted", null, this.testsCompleted);
    }

    public int getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(int totalTests) {
        this.totalTests = totalTests;
        eventEmitter.firePropertyChange("ProtocolTesterModel.totalTests", null, this.totalTests);
    }

    public double getPctComplete() {
        double pctComplete = 0;
        if ( totalTests > 0 ) {
            pctComplete = ((double)testsCompleted/(double)totalTests)*100;
        }
        return pctComplete;
    }



    public void setUpgradeHelperScript(String upgradeHelperScript) {
        this.upgradeHelperScript = upgradeHelperScript;
    }

    public HttpMessage getUpgradeHttpMessage() {
        return upgradeHttpMessage;
    }

    public void setUpgradeHttpMessage(HttpMessage upgradeHttpMessage) {
        this.upgradeHttpMessage = upgradeHttpMessage;
    }

    public ArrayList<WebsocketFrameType> getTestFrameTypes() {
        return testFrameTypes;
    }

    public ArrayList<String> getLogOutput() {
        return logOutput;
    }

    public String getTestWebsocketTextPayload() {
        return testWebsocketTextPayload;
    }

    public void setTestWebsocketTextPayload(String testWebsocketTextPayload) {
        this.testWebsocketTextPayload = testWebsocketTextPayload;
        eventEmitter.firePropertyChange("ProtocolTesterModel.testWebsocketTextPayload", null, this.testWebsocketTextPayload);
    }

    public String getTestWebsocketBinaryPayload() {
        return testWebsocketBinaryPayload;
    }

    public void setTestWebsocketBinaryPayload(String testWebsocketBinaryPayload) {
        this.testWebsocketBinaryPayload = testWebsocketBinaryPayload;
        eventEmitter.firePropertyChange("ProtocolTesterModel.testWebsocketBinaryPayload", null, this.testWebsocketBinaryPayload);
    }

    public void setLogOutput(ArrayList<String> logOutput) {
        this.logOutput = logOutput;
        eventEmitter.firePropertyChange("ProtocolTesterModel.logOutput", null, this.logOutput);
    }

    public String getRawFrameHexStr() {
        return rawFrameHexStr;
    }

    public void setRawFrameHexStr(String rawFrameHexStr) {
        this.rawFrameHexStr = rawFrameHexStr;
        eventEmitter.firePropertyChange("ProtocolTesterModel.rawFrameHexStr", null, this.rawFrameHexStr);
    }

    public void addLogMessage(String message ) {
        logListModel.addElement(message);
        eventEmitter.firePropertyChange("ProtocolTesterModel.lastMessage", null, message);
    }

    public DefaultListModel getLogListModel() {
        return logListModel;
    }

    public void addTestTrafficFrame(WebsocketDirection direction, String conversationUuid, RawWebsocketFrame websocketFrame) throws IOException {
        FuzzRecordModel fuzzRecordModel;
        String txSnippet = null;
        String rxSnippet = null;
        if (direction.equals(WebsocketDirection.INBOUND)) {
            fuzzRecordModel = new FuzzRecordModel(null,conversationUuid,System.currentTimeMillis(),null,websocketFrame.toBytes());
            rxSnippet = GuiUtils.binToHexStr(websocketFrame.toBytes());
        }
        else {
            fuzzRecordModel = new FuzzRecordModel(null,conversationUuid,System.currentTimeMillis(),websocketFrame.toBytes(),null);
            txSnippet = GuiUtils.binToHexStr(websocketFrame.toBytes());
        }

        testTrafficTableModel.addRow(new Object[] {
                fuzzRecordModel.getId(),
                fuzzRecordModel.getConversationUuid(),
                GuiUtils.trafficTimeFmt.format(new Date(fuzzRecordModel.getTimestamp())),
                direction == WebsocketDirection.INBOUND ? "←" : "→",
                direction == WebsocketDirection.INBOUND ? rxSnippet : txSnippet
        });
        writeFuzzRecord(fuzzRecordModel);
        setCurrentFuzzRecord(fuzzRecordModel);
    }

    public ArrayList<WebsocketFrame> getBaseFrames() {
        ArrayList<WebsocketFrame> baseFrames = new ArrayList<WebsocketFrame>();
        for ( WebsocketFrameType frameType : getTestFrameTypes() ) {
            switch ( frameType ) {
                case CONTINUATION:
                    baseFrames.add(new WebsocketFrame(1,0,0,0,1,WebsocketFrameType.CONTINUATION,GuiUtils.parseHexString(getTestWebsocketBinaryPayload())));
                    break;
                case TEXT:
                    baseFrames.add(new WebsocketFrame(1,0,0,0,1,WebsocketFrameType.TEXT,getTestWebsocketTextPayload().getBytes(StandardCharsets.UTF_8)));
                    break;
                case BINARY:
                    baseFrames.add(new WebsocketFrame(1,0,0,0,1,WebsocketFrameType.BINARY,GuiUtils.parseHexString(getTestWebsocketBinaryPayload())));
                    break;
                case RESERVED1:
                    baseFrames.add(new WebsocketFrame(1,0,0,0,1,WebsocketFrameType.RESERVED1,GuiUtils.parseHexString(getTestWebsocketBinaryPayload())));
                    break;
                case RESERVED2:
                    baseFrames.add(new WebsocketFrame(1,0,0,0,1,WebsocketFrameType.RESERVED2,GuiUtils.parseHexString(getTestWebsocketBinaryPayload())));
                    break;
                case RESERVED3:
                    baseFrames.add(new WebsocketFrame(1,0,0,0,1,WebsocketFrameType.RESERVED3,GuiUtils.parseHexString(getTestWebsocketBinaryPayload())));
                    break;
                case RESERVED4:
                    baseFrames.add(new WebsocketFrame(1,0,0,0,1,WebsocketFrameType.RESERVED4,GuiUtils.parseHexString(getTestWebsocketBinaryPayload())));
                    break;
                case RESERVED5:
                    baseFrames.add(new WebsocketFrame(1,0,0,0,1,WebsocketFrameType.RESERVED5,GuiUtils.parseHexString(getTestWebsocketBinaryPayload())));
                    break;
                case CLOSE:
                    // https://datatracker.ietf.org/doc/html/rfc6455#page-41
                    baseFrames.add(new WebsocketFrame(1,0,0,0,1,WebsocketFrameType.CLOSE,GuiUtils.parseHexString(getTestWebsocketBinaryPayload())));
                    break;
                case PING:
                    baseFrames.add(new WebsocketFrame(1,0,0,0,1,WebsocketFrameType.PING,GuiUtils.parseHexString(getTestWebsocketBinaryPayload())));
                    break;
                case PONG:
                    baseFrames.add(new WebsocketFrame(1,0,0,0,1,WebsocketFrameType.PONG,GuiUtils.parseHexString(getTestWebsocketBinaryPayload())));
                    break;
            }
        }
        return baseFrames;
    }

    public FuzzRecordModel getCurrentFuzzRecord() {
        return currentFuzzRecord;
    }

    public void setCurrentFuzzRecord(FuzzRecordModel currentFuzzRecord) {
        this.currentFuzzRecord = currentFuzzRecord;
        eventEmitter.firePropertyChange("ProtocolTesterModel.currentFuzzRecord", null, currentFuzzRecord);
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
