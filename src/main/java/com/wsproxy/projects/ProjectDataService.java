package com.wsproxy.projects;

import com.wsproxy.anomalydetection.DetectedAnomaly;
import com.wsproxy.httpproxy.HttpMessage;
import com.wsproxy.httpproxy.HttpMessageParseException;
import com.wsproxy.httpproxy.websocket.WebsocketFrame;
import com.wsproxy.httpproxy.websocket.WebsocketFrameType;
import com.wsproxy.httpproxy.trafficlogger.*;
import com.wsproxy.tester.*;
import com.wsproxy.util.TestUtil;
import com.wsproxy.util.WebsocketUtil;

import java.awt.*;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
public class ProjectDataService {
    private final String tableNames[] = {
            "project",
            "traffic_record",
            "http_request_response_pair",
            "websocket_frame",
            "test_seq",
            "test_seq_item",
            "test_websocket_frame",
            "test_target",
            "test_run",
            "detected_anomaly",
            "websocket_frame"
    };
    private final String PROJECT_SCHEMA[] = {
            "CREATE TABLE IF NOT EXISTS project (\n" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "created INTEGER NOT NULL,\n" +
                    "name TEXT,\n" +
                    "description TEXT,\n" +
                    "version TEXT NOT NULL\n" +
                    ");",
            "CREATE TABLE IF NOT EXISTS traffic_record (\n" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "created INTEGER,\n" +
                    "traffic_source TEXT NOT NULL,\n" +
                    "test_run_id INTEGER,\n" +
                    "highlight_colour INTEGER\n" +
                    ");",
            "CREATE TABLE IF NOT EXISTS http_request_response_pair (\n" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "message_uuid TEXT NOT NULL,\n" +
                    "traffic_record_id INTEGER NOT NULL,\n" +
                    "created INTEGER NOT NULL,\n" +
                    "is_tls INTEGER NOT NULL,\n" +
                    "method TEXT NOT NULL,\n" +
                    "url TEXT NOT NULL,\n" +
                    "code INTEGER NOT NULL,\n" +
                    "request_header TEXT NOT NULL,\n" +
                    "request_body BLOB,\n" +
                    "response_header TEXT NOT NULL,\n" +
                    "response_body BLOB\n" +
                    ");",
            "CREATE TABLE IF NOT EXISTS websocket_frame (\n" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "message_uuid TEXT NOT NULL,\n" +
                    "traffic_record_id INTEGER NOT NULL,\n" +
                    "conversation_uuid TEXT,\n" +
                    "upgrade_message_id INTEGER NOT NULL,\n" +
                    "upgrade_message_uuid TEXT NOT NULL,\n" +
                    "created INTEGER NOT NULL,\n" +
                    "direction INTEGER NOT NULL,\n" +
                    "flag_fin INTEGER NOT NULL,\n" +
                    "flag_rsv1 INTEGER NOT NULL,\n" +
                    "flag_rsv2 INTEGER NOT NULL,\n" +
                    "flag_rsv3 INTEGER NOT NULL,\n" +
                    "flag_masked INTEGER NOT NULL,\n" +
                    "opcode TEXT NOT NULL,\n" +
                    "payload_len INTEGER NOT NULL,\n" +
                    "mask BLOB,\n" +
                    "payload BLOB\n" +
                    ");",
            "CREATE TABLE IF NOT EXISTS test_seq (\n" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "test_run_id INTEGER NOT NULL,\n" +
                    "created INTEGER NOT NULL,\n" +
                    "upgrade_helper_script TEXT,\n" +
                    "upgrade_request BLOB\n" +
                    ");",
            "CREATE TABLE IF NOT EXISTS test_seq_item (\n" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "test_seq_id INTEGER NOT NULL,\n" +
                    "test_websocket_frame_id INTEGER,\n" +
                    "created INTEGER NOT NULL,\n" +
                    "test_seq_item_type STRING NOT NULL,\n" +
                    "delay_msec INTEGER NOT NULL,\n" +
                    "action_type TEXT,\n" +
                    "step_order INTEGER NOT NULL\n" +
                    ");",
            "CREATE TABLE IF NOT EXISTS test_websocket_frame (\n" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "flag_fin INTEGER NOT NULL,\n" +
                    "flag_rsv1 INTEGER NOT NULL,\n" +
                    "flag_rsv2 INTEGER NOT NULL,\n" +
                    "flag_rsv3 INTEGER NOT NULL,\n" +
                    "flag_masked INTEGER NOT NULL,\n" +
                    "opcode TEXT NOT NULL,\n" +
                    "payload_len INTEGER NOT NULL,\n" +
                    "mask BLOB,\n" +
                    "payload BLOB\n" +
                    ");",
            "CREATE TABLE IF NOT EXISTS test_target (\n" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "test_seq_item_id INTEGER NOT NULL,\n" +
                    "created INTEGER NOT NULL,\n" +
                    "name TEXT NOT NULL,\n" +
                    "enabled INTEGER NOT NULL,\n" +
                    "start_pos INTEGER NOT NULL,\n" +
                    "end_pos INTEGER NOT NULL,\n" +
                    "encodings_csv TEXT NOT NULL,\n" +
                    "highlight_colour TEXT NOT NULL\n" +
                    ");",
            "CREATE TABLE IF NOT EXISTS test_run (\n" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "test_type TEXT NOT NULL,\n" +
                    "created INTEGER NOT NULL,\n" +
                    "start_time INTEGER,\n" +
                    "end_time INTEGER,\n" +
                    "name TEXT NOT NULL,\n" +
                    "note TEXT,\n" +
                    "auto_step_count INTEGER,\n" +
                    "auto_test_count INTEGER,\n" +
                    "auto_pct_complete INTEGER,\n" +
                    "reuse_connection INTEGER,\n" +
                    "continue_replay_after_test INTEGER,\n" +
                    "auto_tests_completed INTEGER,\n" +
                    "auto_test_status TEXT\n" +
                    ");",
            "CREATE TABLE IF NOT EXISTS detected_anomaly (\n" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "created INTEGER NOT NULL,\n" +
                    "rule_id INTEGER NOT NULL,\n" +
                    "websocket_frame_id INTEGER,\n" +
                    "conversation_id INTEGER,\n" +
                    "highlight_pos_start INTEGER,\n" +
                    "highlight_pos_end INTEGER,\n" +
                    "detector TEXT NOT NULL,\n" +
                    "traffic_source TEXT NOT NULL,\n" +
                    "credibility TEXT NOT NULL,\n" +
                    "cwe INTEGER,\n" +
                    "title TEXT NOT NULL,\n" +
                    "description TEXT NOT NULL\n" +
                    ");",
            "CREATE VIEW v_test_websocket_traffic AS select traffic_record.id as traffic_record_id,traffic_record.created as created,traffic_record.traffic_source as traffic_source,traffic_record.test_run_id as test_run_id,traffic_record.highlight_colour as highlight_colour,test_run.id as test_run_id,test_run.test_type as test_type,test_run.start_time as start_time,test_run.end_time as end_time,test_run.name as name,test_run.note as test_note,test_run.auto_step_count as auto_step_count,test_run.auto_test_count as auto_test_count,test_run.auto_pct_complete as auto_pct_complete,test_run.reuse_connection as reuse_connection,test_run.continue_replay_after_test as continue_replay_after_test,test_run.auto_tests_completed as auto_tests_completed,test_run.auto_test_status as auto_test_status,websocket_frame.id as id,websocket_frame.conversation_uuid as conversation_uuid,websocket_frame.upgrade_message_id as upgrade_message_id,websocket_frame.created as created,websocket_frame.direction as direction,websocket_frame.flag_fin as flag_fin,websocket_frame.flag_rsv1 as flag_rsv1,websocket_frame.flag_rsv2 as flag_rsv2,websocket_frame.flag_rsv3 as flag_rsv3,websocket_frame.flag_masked as flag_masked,websocket_frame.opcode as opcode,websocket_frame.payload_len as payload_len,websocket_frame.mask as mask,websocket_frame.payload as payload from traffic_record join test_run on traffic_record.test_run_id = test_run.id join websocket_frame on traffic_record.id = websocket_frame.traffic_record_id"
    };
    private String dbFilePath;
    private Connection connection;

    public ProjectDataService() throws ProjectDataServiceException {

    }

    public ProjectDataService(String dbFilePath) throws ProjectDataServiceException {
        if ( dbFilePath == null ) {
            throw new ProjectDataServiceException("DB file path is null");
        }
        connect(dbFilePath);
    }

    public boolean isConnected() {
        if ( connection != null ) {
            return true;
        }
        return false;
    }

    public void connect( String dbFilePath ) throws ProjectDataServiceException {
        try {
            connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s", dbFilePath));
            connection.setAutoCommit(true);
            initDb();
            this.dbFilePath = dbFilePath;
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("connect exception: %s", e.getMessage()));
        }
    }

    public void disconnect() throws ProjectDataServiceException {
        if ( connection != null ) {
            try {
                connection.close();
                this.dbFilePath = null;
            } catch (SQLException e) {
                throw new ProjectDataServiceException(String.format("disconnect exception: %s", e.getMessage()));
            }
        }
    }

    public int getLastInsert() throws SQLException {
        ResultSet rs = connection.prepareStatement("select last_insert_rowid();").executeQuery();
        if ( rs.next() ) {
            return rs.getInt(1);
        }
        return -1;
    }

    public void initDb() throws ProjectDataServiceException {
        try {
            Statement dropView = connection.createStatement();
            dropView.execute("DROP VIEW IF EXISTS v_test_websocket_traffic");
            for ( String table : PROJECT_SCHEMA ) {
                Statement stmt = connection.createStatement();
                stmt.execute(table);
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("initDb exception: %s", e.getMessage()));
        }
    }

    public int getRowCount( String tableName) throws ProjectDataServiceException {
        int rowCount = -1;
        if ( Arrays.asList(tableNames).contains(tableName)) {
            String sql = String.format("select count(*) as rowcount from %s", tableName);
            try {
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    rowCount = rs.getInt("rowcount");
                }
            } catch (SQLException e) {
                throw new ProjectDataServiceException(String.format("getRowCount exception: %s", e.getMessage()));
            }
        }
        return rowCount;
    }

    public String getProjectVersion() throws ProjectDataServiceException {
        String version = null;
        String sql = "select version from project";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                version = rs.getString("version");
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getProjectVersion exception: %s", e.getMessage()));
        }
        return version;
    }

    public void setProjectVersion(String projectVersion) throws ProjectDataServiceException {
        String sql = "UPDATE project set version = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,projectVersion);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("setProjectVersion exception: %s", e.getMessage()));
        }
    }

    public String getProjectName() throws ProjectDataServiceException {
        String name = null;
        String sql = "select name from project";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                name = rs.getString("name");
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getProjectName exception: %s", e.getMessage()));
        }
        return name;
    }

    public void setProjectName(String projectName) throws ProjectDataServiceException {
        String sql = "UPDATE project SET name = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,projectName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("setProjectName exception: %s", e.getMessage()));
        }
    }

    public String getProjectDescription() throws ProjectDataServiceException {
        String description = null;
        String sql = "select description from project";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                description = rs.getString("description");
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getProjectDescription exception: %s", e.getMessage()));
        }
        return description;
    }

    public void setProjectDescription(String projectDescription) throws ProjectDataServiceException {
        String sql = "UPDATE project SET description = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,projectDescription);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("setProjectDescription exception: %s", e.getMessage()));
        }
    }

    public ArrayList<TestTarget> getTestTargets ( int testSequenceItemId ) throws ProjectDataServiceException {
        ArrayList<TestTarget> testTargets = new ArrayList<TestTarget>();
        String sql = "select id," +
                "test_seq_item_id," +
                "created," +
                "name," +
                "enabled," +
                "start_pos," +
                "end_pos," +
                "encodings_csv, " +
                "highlight_colour from test_target where test_seq_item_id = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1,testSequenceItemId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                testTargets.add(new TestTarget(rs.getInt("id"),
                        rs.getInt("test_seq_item_id"),
                        rs.getString("name"),
                        rs.getInt("enabled") == 1 ? true : false,
                        rs.getInt("start_pos"),
                        rs.getInt("end_pos"),
                        Color.WHITE,
                        new ArrayList<PayloadEncoding>()
                ));
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getTestTargets exception: %s", e.getMessage()));
        }
        return testTargets;
    }

    private String payloadEncodingsToCsv ( ArrayList<PayloadEncoding> encodings ) {
        String encodingCsv = "";
        if ( encodings != null ) {
            if ( encodings.size() > 0 ) {
                ArrayList<String> encodingCsvList = new ArrayList<String>();
                for ( PayloadEncoding payloadEncoding : encodings ) {
                    encodingCsvList.add(payloadEncoding.toString());
                }
                encodingCsv = String.join(",", encodingCsvList);
            }
        }
        return encodingCsv;
    }

    public String colourToCsv ( Color color ) {
        return String.format("%d,%d,%d", color.getRed(), color.getGreen(), color.getBlue());
    }

    public void saveTestTarget( TestTarget testTarget, int testSeqItemId  ) throws ProjectDataServiceException {
        if ( testTarget.getId() > 0 ) {
            String sql = "UPDATE test_target " +
                    "SET name = ?," +
                    "enabled = ?," +
                    "start_pos =?," +
                    "end_pos = ?, " +
                    "encodings_csv = ?," +
                    "highlight_colour = ?" +
                    " WHERE id = ?";
            try {
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1,testTarget.getTargetName());
                stmt.setInt(2,testTarget.isEnabled() ? 1 : 0);
                stmt.setInt(3,testTarget.getStartPos());
                stmt.setInt(4,testTarget.getEndPos());
                stmt.setString(5,payloadEncodingsToCsv(testTarget.getEnabledEncodings()));
                stmt.setString(6,colourToCsv(testTarget.getHighlightColour()));
                stmt.setInt(1, testTarget.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new ProjectDataServiceException(String.format("saveTestTarget exception: %s", e.getMessage()));
            }
        }
        else {
            String sql = "INSERT INTO test_target ( test_seq_item_id," +
                    "created," +
                    "name," +
                    "enabled," +
                    "start_pos," +
                    "end_pos, " +
                    "encodings_csv," +
                    "highlight_colour )" +
                    " VALUES ( ?, ?, ?, ?, ?, ?, ?, ? );";
            try {
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, testSeqItemId);
                stmt.setInt(2, (int) System.currentTimeMillis());
                stmt.setString(3,testTarget.getTargetName());
                stmt.setInt(4,testTarget.isEnabled() ? 1 : 0);
                stmt.setInt(5,testTarget.getStartPos());
                stmt.setInt(6,testTarget.getEndPos());
                stmt.setString(7,payloadEncodingsToCsv(testTarget.getEnabledEncodings()));
                stmt.setString(8,colourToCsv(testTarget.getHighlightColour()));
                stmt.executeUpdate();
                testTarget.setId(getLastInsert());
            } catch (SQLException e) {
                throw new ProjectDataServiceException(String.format("saveTestTarget exception: %s", e.getMessage()));
            }
        }
    }



    public WebsocketFrame getTestWebsocketFrame( int id ) throws ProjectDataServiceException {
        WebsocketFrame frame = null;
        String sql = "select id," +
                "flag_fin," +
                "flag_rsv1," +
                "flag_rsv2," +
                "flag_rsv3," +
                "flag_masked," +
                "opcode," +
                "payload_len," +
                "mask," +
                "payload" +
                " from test_websocket_frame where id = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1,id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                WebsocketFrameType websocketFrameType = WebsocketUtil.getWebsocketFrameTypeEnum(rs.getString("opcode"));
                if ( websocketFrameType != null ) {
                    frame = new WebsocketFrame(
                            rs.getInt("id"),
                            null,
                            null,
                            rs.getInt("flag_fin"),
                            rs.getInt("flag_rsv1"),
                            rs.getInt("flag_rsv2"),
                            rs.getInt("flag_rsv3"),
                            rs.getInt("flag_masked"),
                            websocketFrameType,
                            rs.getInt("payload_len"),
                            rs.getBytes("mask"),
                            rs.getBytes("payload"),
                            WebsocketDirection.OUTBOUND
                    );
                }
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getTestWebsocketFrame exception: %s", e.getMessage()));
        }
        return frame;
    }

    public void saveTestWebsocketFrame( WebsocketFrame websocketFrame ) throws ProjectDataServiceException {
        if ( websocketFrame.getId() > 0 ) {
            String sql = "UPDATE test_websocket_frame " +
                    "SET flag_fin = ?," +
                    "flag_rsv1 = ?," +
                    "flag_rsv2 = ?," +
                    "flag_rsv3 = ?," +
                    "flag_masked = ?," +
                    "opcode = ?," +
                    "payload_len = ?," +
                    "mask = ?," +
                    "payload = ? where id = ?";
            try {
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, websocketFrame.getFin());
                stmt.setInt(2, websocketFrame.getRsv1());
                stmt.setInt(3, websocketFrame.getRsv2());
                stmt.setInt(4, websocketFrame.getRsv3());
                stmt.setInt(5, websocketFrame.getMasked());
                stmt.setString(6, websocketFrame.getOpcode().toString());
                stmt.setInt(7, websocketFrame.getPayloadLength());
                stmt.setBytes(8, websocketFrame.getMaskKey());
                stmt.setBytes(9, websocketFrame.getPayload());
                stmt.setInt(10, websocketFrame.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new ProjectDataServiceException(String.format("saveTestWebsocketFrame exception: %s", e.getMessage()));
            }
        }
        else {
            String sql = "INSERT INTO test_websocket_frame ( " +
                    "flag_fin," +
                    "flag_rsv1," +
                    "flag_rsv2," +
                    "flag_rsv3," +
                    "flag_masked," +
                    "opcode," +
                    "payload_len," +
                    "mask," +
                    "payload )" +
                    " VALUES ( ?,?,?,?,?,?,?,?,? )";
            try {
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, websocketFrame.getFin());
                stmt.setInt(2, websocketFrame.getRsv1());
                stmt.setInt(3, websocketFrame.getRsv2());
                stmt.setInt(4, websocketFrame.getRsv3());
                stmt.setInt(5, websocketFrame.getMasked());
                stmt.setString(6, websocketFrame.getOpcode().toString());
                stmt.setInt(7, websocketFrame.getPayloadLength());
                stmt.setBytes(8, websocketFrame.getMaskKey());
                stmt.setBytes(9, websocketFrame.getPayload());
                stmt.executeUpdate();
                websocketFrame.setId(getLastInsert());
            } catch (SQLException e) {
                throw new ProjectDataServiceException(String.format("saveTestWebsocketFrame exception: %s", e.getMessage()));
            }
        }
    }

    public void saveTestSequenceItem( TestSequenceItem testSequenceItem, int testSeqId ) throws ProjectDataServiceException {
        int testFrameId = -1;
        if ( testSequenceItem.getFrame() != null ) {
            saveTestWebsocketFrame(testSequenceItem.getFrame());
            testFrameId = testSequenceItem.getFrame().getId();
        }
        if ( testSequenceItem.getId() > 0 ) {
            String sql = "UPDATE test_seq_item " +
                    "SET test_seq_id = ?," +
                    "test_websocket_frame_id = ?," +
                    "test_seq_item_type = ?," +
                    "delay_msec = ?," +
                    "action_type = ?," +
                    "step_order = ? where id = ?";
            try {
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, testSeqId);
                if ( testFrameId > 0 ) {
                    stmt.setInt(2,testFrameId);
                }
                else {
                    stmt.setNull(2,Types.INTEGER);
                }
                stmt.setString(3,testSequenceItem.getTestSequenceItemType().toString());
                stmt.setInt(4,testSequenceItem.getDelayMsec());
                stmt.setString(5,testSequenceItem.getActionType());
                stmt.setInt(6,testSequenceItem.getStepOrder());
                stmt.setInt(6,testSequenceItem.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new ProjectDataServiceException(String.format("saveTestSequenceItem exception: %s", e.getMessage()));
            }
        }
        else {
            String sql = "INSERT INTO test_seq_item ( test_seq_id," +
                    "test_websocket_frame_id," +
                    "created," +
                    "test_seq_item_type," +
                    "delay_msec," +
                    "action_type," +
                    "step_order )" +
                    " VALUES ( ?,?,?,?,?,?,? )";
            try {
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, testSeqId);
                if ( testFrameId > 0 ) {
                    stmt.setInt(2,testFrameId);
                }
                else {
                    stmt.setNull(2,Types.INTEGER);
                }
                stmt.setInt(3, (int) System.currentTimeMillis());
                stmt.setString(4,testSequenceItem.getTestSequenceItemType().toString());
                stmt.setInt(5,testSequenceItem.getDelayMsec());
                stmt.setString(6,testSequenceItem.getTestSequenceItemType().toString());
                stmt.setInt(7,testSequenceItem.getStepOrder());
                stmt.executeUpdate();
                testSequenceItem.setId(getLastInsert());
            } catch (SQLException e) {
                throw new ProjectDataServiceException(String.format("saveTestSequenceItem exception: %s", e.getMessage()));
            }
        }
    }


    public ArrayList<TestSequenceItem> getTestSequenceItems(int testSeqId ) throws ProjectDataServiceException {
        ArrayList<TestSequenceItem> testSequenceItems = new ArrayList<TestSequenceItem>();
        String sql = "select id," +
                "test_seq_id," +
                "test_websocket_frame_id," +
                "created," +
                "test_seq_item_type," +
                "delay_msec," +
                "action_type," +
                "step_order" +
                " from test_seq_item where test_seq_id = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1,testSeqId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                WebsocketFrame frame = getTestWebsocketFrame(rs.getInt("test_websocket_frame_id"));
                TestSequenceItemType testSequenceItemType = null;
                if ( rs.getString("test_seq_item_type").equals("FRAME")) {
                    testSequenceItemType = TestSequenceItemType.FRAME;
                }
                else if ( rs.getString("test_seq_item_type").equals("IOWAIT")) {
                    testSequenceItemType = TestSequenceItemType.IOWAIT;
                }
                if ( testSequenceItemType != null) {
                    testSequenceItems.add(new TestSequenceItem(rs.getInt("id"),
                            new ArrayList<TestTarget>(),
                            String.format("%d", rs.getInt("id")),
                            testSequenceItemType,
                            rs.getInt("delay_msec"),
                            rs.getString("action_type"),
                            frame,
                            rs.getInt("step_order"))
                    );
                }
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getTestSequenceItems exception: %s", e.getMessage()));
        }
        return testSequenceItems;
    }


    public void saveTestSequence ( TestSequence testSequence, int testRunId ) throws ProjectDataServiceException {

        if ( testSequence.getId() > 0 ) {
            String sql = "UPDATE test_seq " +
                    "SET test_run_id = ?," +
                    "upgrade_helper_script = ?, " +
                    "upgrade_request = ?" +
                    " where id = ?";
            try {
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, testRunId);
                stmt.setString(2,testSequence.getUpgradeHelperScript());
                if ( testSequence.getHttpMessage() != null ) {
                    stmt.setBytes(3,testSequence.getHttpMessage().getBytes());
                }
                else {
                    stmt.setNull(3,Types.BLOB);
                }
                stmt.setInt(4,testSequence.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new ProjectDataServiceException(String.format("setProjectVersion exception: %s", e.getMessage()));
            }
        }
        else {
            String sql = "INSERT INTO test_seq ( test_run_id," +
                    "created," +
                    "upgrade_helper_script," +
                    "upgrade_request" +
                    ") values ( ?,?,?,? )";
            try {
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, testRunId);
                stmt.setInt(2, (int) System.currentTimeMillis());
                stmt.setString(3,testSequence.getUpgradeHelperScript());
                if ( testSequence.getHttpMessage() != null ) {
                    stmt.setBytes(4,testSequence.getHttpMessage().getBytes());
                }
                else {
                    stmt.setNull(4,Types.BLOB);
                }
                stmt.executeUpdate();
                testSequence.setId(getLastInsert());
            } catch (SQLException e) {
                throw new ProjectDataServiceException(String.format("saveTestSequence exception: %s", e.getMessage()));
            }
        }

        if ( testSequence.getId() > 0 ) {
            for ( TestSequenceItem testSequenceItem : testSequence.getTestSequenceItems() ) {
                saveTestSequenceItem(testSequenceItem,testSequence.getId());
            }
        }
    }

    public TestSequence getTestSequence( int testRunId ) throws ProjectDataServiceException {
        TestSequence testSequence = null;
        String sql = "select id," +
                "test_run_id," +
                "created," +
                "upgrade_helper_script," +
                "upgrade_request" +
                " from test_seq where test_run_id = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1,testRunId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                HttpMessage httpMessage = null;
                if ( rs.getBytes("upgrade_request") != null ) {
                    httpMessage = new HttpMessage();
                    httpMessage.fromBytes(rs.getBytes("upgrade_request"));
                }
                ArrayList<TestSequenceItem> testSequenceItems = getTestSequenceItems(rs.getInt("id"));
                ArrayList<TestTarget> testTargets = new ArrayList<TestTarget>();
                for ( TestSequenceItem testSequenceItem : testSequenceItems ) {
                    testTargets.addAll(getTestTargets(testSequenceItem.getId()));
                }
                testSequence = new TestSequence(
                        rs.getInt("id"),
                        rs.getString("upgrade_helper_script"),
                        httpMessage,
                        testSequenceItems,
                        testTargets);
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getTestSequence exception: %s", e.getMessage()));
        } catch (HttpMessageParseException e) {
            throw new ProjectDataServiceException(String.format("getTestSequence exception: %s", e.getMessage()));
        }
        return testSequence;
    }

    public ArrayList<AutomatedTestRun> getAutomatedTestRuns() throws ProjectDataServiceException {
        ArrayList<AutomatedTestRun> automatedTestRuns = new ArrayList<AutomatedTestRun>();
        String sql = "select id," +
                "test_type," +
                "created," +
                "start_time," +
                "end_time," +
                "name," +
                "note," +
                "auto_step_count," +
                "auto_test_count," +
                "auto_pct_complete," +
                "reuse_connection," +
                "continue_replay_after_test," +
                "auto_tests_completed," +
                "auto_test_status" +
                " from test_run where test_type = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,"AUTOMATED");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TestSequence testSequence = getTestSequence(rs.getInt("id"));
                automatedTestRuns.add( new AutomatedTestRun(
                        rs.getInt("id"),
                        rs.getString("name"),
                        Long.parseLong(rs.getString("created")),
                        testSequence,
                        rs.getInt("auto_step_count"),
                        rs.getInt("auto_test_count"),
                        rs.getInt("auto_pct_complete"),
                        rs.getInt("auto_tests_completed"),
                        rs.getString("auto_test_status"),
                        rs.getInt("reuse_connection") == 1 ? true : false,
                        rs.getInt("continue_replay_after_test") == 1 ? true : false));
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getAutomatedTestRuns exception: %s", e.getMessage()));
        }
        return automatedTestRuns;
    }

    public void saveAutomatedTestRuns(ArrayList<AutomatedTestRun> automatedTestRuns) throws ProjectDataServiceException {
        for ( AutomatedTestRun automatedTestRun : automatedTestRuns ) {
            saveAutomatedTestRun(automatedTestRun);
        }
    }
    public void saveAutomatedTestRun( AutomatedTestRun automatedTestRun ) throws ProjectDataServiceException {
        if ( automatedTestRun.getId() > 0 ) {
            String sql = "UPDATE test_run " +
                    "SET start_time = ?," +
                    "end_time = ?," +
                    "name = ?, note = ?," +
                    "auto_step_count = ?," +
                    "auto_test_count = ?," +
                    "auto_tests_completed = ?," +
                    "auto_test_status = ?," +
                    "auto_pct_complete = ?," +
                    "reuse_connection = ?," +
                    "continue_replay_after_test = ? where id = ?";
            try {
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, (int) automatedTestRun.getTestRunStartTime());
                stmt.setInt(2, (int) automatedTestRun.getTestRunStopTime());
                stmt.setString(3, automatedTestRun.getTestName());
                stmt.setString(4, "");
                stmt.setInt(5, automatedTestRun.getStepCount());
                stmt.setInt(6, automatedTestRun.getTestCount());
                stmt.setInt(7, automatedTestRun.getTestsCompleted());
                stmt.setString(8, automatedTestRun.getStatus());
                stmt.setInt(9, automatedTestRun.getPctComplete());
                stmt.setInt(10, automatedTestRun.isReuseConnection() ? 1 : 0);
                stmt.setInt(11, automatedTestRun.isContinueReplayAfterTestInsertion() ? 1 : 0);
                stmt.setInt(12, automatedTestRun.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new ProjectDataServiceException(String.format("saveAutomatedTestRuns exception: %s", e.getMessage()));
            }
        }
        else {
            String sql = "INSERT INTO test_run ( test_type," +
                    "created," +
                    "start_time," +
                    "end_time," +
                    "name," +
                    "note," +
                    "auto_step_count," +
                    "auto_test_count," +
                    "auto_pct_complete," +
                    "reuse_connection," +
                    "continue_replay_after_test," +
                    "auto_tests_completed," +
                    "auto_test_status ) " +
                    " values ( ?, ?, ?, ?, ?, ?, ?, ? , ?, ?, ?, ?, ? )";
            try {
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, "AUTOMATED");
                stmt.setInt(2,(int)System.currentTimeMillis());
                stmt.setInt(3, (int) automatedTestRun.getTestRunStartTime());
                stmt.setInt(4, (int) automatedTestRun.getTestRunStopTime());
                stmt.setString(5, automatedTestRun.getTestName());
                stmt.setString(6, "");
                stmt.setInt(7, automatedTestRun.getStepCount());
                stmt.setInt(8, automatedTestRun.getTestCount());
                stmt.setInt(9, automatedTestRun.getTestsCompleted());
                stmt.setString(10, automatedTestRun.getStatus());
                stmt.setInt(11, automatedTestRun.getPctComplete());
                stmt.setInt(12, automatedTestRun.isReuseConnection() ? 1 : 0);
                stmt.setInt(13, automatedTestRun.isContinueReplayAfterTestInsertion() ? 1 : 0);
                stmt.executeUpdate();
                automatedTestRun.setId(getLastInsert());
            } catch (SQLException e) {
                throw new ProjectDataServiceException(String.format("saveAutomatedTestRuns exception: %s", e.getMessage()));
            }
        }
        if ( automatedTestRun.getId() > 0 ) {
            saveTestSequence(automatedTestRun.getTestSequence(),automatedTestRun.getId());
        }
    }

    public ArrayList<ManualTestRun> getManualTestRuns() throws ProjectDataServiceException {
        ArrayList<ManualTestRun> manualTestRuns = new ArrayList<ManualTestRun>();
        String sql = "select id," +
                "test_type," +
                "created," +
                "start_time," +
                "end_time," +
                "name," +
                "note," +
                "auto_step_count," +
                "auto_test_count," +
                "auto_pct_complete," +
                "reuse_connection," +
                "continue_replay_after_test," +
                "auto_tests_completed," +
                "auto_test_status" +
                " from test_run where test_type = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,"MANUAL");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TestSequence testSequence = getTestSequence(rs.getInt("id"));
                manualTestRuns.add( new ManualTestRun(rs.getInt("id"), rs.getString("name"), testSequence));
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getManualTestRuns exception: %s", e.getMessage()));
        }
        return manualTestRuns;
    }

    public void saveManualTestRuns(ArrayList<ManualTestRun> manualTestRuns) throws ProjectDataServiceException {
        for ( ManualTestRun manualTestRun : manualTestRuns ) {
            if ( manualTestRun.getId() > 0 ) {
                String sql = "UPDATE test_run SET name = ?, note = ? where id = ?";
                try {
                    PreparedStatement stmt = connection.prepareStatement(sql);
                    stmt.setString(1, manualTestRun.getTestName());
                    stmt.setString(1, "");
                    stmt.setInt(1, manualTestRun.getId());
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    throw new ProjectDataServiceException(String.format("saveManualTestRuns exception: %s", e.getMessage()));
                }
            }
            else {
                String sql = "INSERT INTO test_run ( test_type,created,name,note ) " +
                        "values ( ?, ?, ?, ? )";
                try {
                    PreparedStatement stmt = connection.prepareStatement(sql);
                    stmt.setString(1, "MANUAL");
                    stmt.setInt(2,(int)System.currentTimeMillis());
                    stmt.setString(3, manualTestRun.getTestName());
                    stmt.setString(4, "");
                    stmt.executeUpdate();
                    manualTestRun.setId(getLastInsert());
                } catch (SQLException e) {
                    throw new ProjectDataServiceException(String.format("saveManualTestRuns exception: %s", e.getMessage()));
                }
            }
            if ( manualTestRun.getId() > 0 ) {
                saveTestSequence(manualTestRun.getTestSequence(),manualTestRun.getId());
            }
        }
    }

    public ArrayList<DetectedAnomaly> getDetectedAnomalies() throws ProjectDataServiceException {
        ArrayList<DetectedAnomaly> detectedAnomalies = new ArrayList<DetectedAnomaly>();
        String sql = "select id," +
                "created," +
                "rule_id," +
                "websocket_frame_id," +
                "conversation_id," +
                "highlight_pos_start," +
                "highlight_pos_end," +
                "detector," +
                "traffic_source," +
                "credibility," +
                "cwe," +
                "title," +
                "description from detected_anomaly";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TestSequence testSequence = getTestSequence(rs.getInt("id"));
                detectedAnomalies.add( new DetectedAnomaly(
                        rs.getInt("id"),
                        rs.getString("id"),
                        rs.getInt("rule_id"),
                        rs.getString("conversation_id"),
                        rs.getString("conversation_id"),
                        rs.getInt("highlight_pos_start"),
                        rs.getInt("highlight_pos_end"),
                        rs.getString("detector"),
                        rs.getString("traffic_source"),
                        rs.getString("credibility"),
                "testname",
                        rs.getString("cwe"),
                        rs.getString("title"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getDetectedAnomalies exception: %s", e.getMessage()));
        }
        return detectedAnomalies;
    }

    public void saveDetectedAnomaly( DetectedAnomaly detectedAnomaly ) throws ProjectDataServiceException {
        if ( detectedAnomaly.getId() > 0 ) {
            String sql = "UPDATE detected_anomaly " +
                    "SET rule_id = ?," +
                    "websocket_frame_id = ?," +
                    "conversation_id = ?," +
                    "highlight_pos_start = ?," +
                    "highlight_pos_end = ?," +
                    "detector = ?," +
                    "traffic_source = ?," +
                    "credibility = ?," +
                    "cwe = ?," +
                    "title = ?," +
                    "description = ? where id = ?";
            try {
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1,detectedAnomaly.getRuleId());
                stmt.setInt(2,1); //stmt.setInt(1,detectedAnomaly.getWebsocketMsgId());
                stmt.setInt(3,1);//stmt.setInt(1,detectedAnomaly.getTestSequenceId());
                stmt.setInt(4,detectedAnomaly.getHighlightPosStart());
                stmt.setInt(5,detectedAnomaly.getHighlightPosEnd());
                stmt.setString(6,detectedAnomaly.getDetector());
                stmt.setString(7,detectedAnomaly.getSource());
                stmt.setString(8,detectedAnomaly.getCredibility());
                stmt.setInt(9,1); //stmt.setInt(1,detectedAnomaly.getCWE());
                stmt.setString(10,detectedAnomaly.getTitle());
                stmt.setString(11,detectedAnomaly.getDescription());
                stmt.setInt(12, detectedAnomaly.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new ProjectDataServiceException(String.format("saveDetectedAnomaly exception: %s", e.getMessage()));
            }
        }
        else {
            String sql = "INSERT INTO detected_anomaly ( created," +
                    "rule_id," +
                    "websocket_frame_id," +
                    "conversation_id," +
                    "highlight_pos_start," +
                    "highlight_pos_end," +
                    "detector," +
                    "traffic_source," +
                    "credibility," +
                    "cwe," +
                    "title," +
                    "description ) " +
                    "values ( ?,?,?,?,?,?,?,?,?,?,?,?)";
            try {
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1,(int)System.currentTimeMillis());
                stmt.setInt(2,detectedAnomaly.getRuleId());
                stmt.setInt(3,1); //stmt.setInt(1,detectedAnomaly.getWebsocketMsgId());
                stmt.setInt(4,1);//stmt.setInt(1,detectedAnomaly.getTestSequenceId());
                stmt.setInt(5,detectedAnomaly.getHighlightPosStart());
                stmt.setInt(6,detectedAnomaly.getHighlightPosEnd());
                stmt.setString(7,detectedAnomaly.getDetector());
                stmt.setString(8,detectedAnomaly.getSource());
                stmt.setString(9,detectedAnomaly.getCredibility());
                stmt.setInt(10,1); //stmt.setInt(1,detectedAnomaly.getCWE());
                stmt.setString(11,detectedAnomaly.getTitle());
                stmt.setString(12,detectedAnomaly.getDescription());
                detectedAnomaly.setId(getLastInsert());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new ProjectDataServiceException(String.format("saveDetectedAnomaly exception: %s", e.getMessage()));
            }
        }
    }

    public void deleteTestRun( int id ) throws ProjectDataServiceException {
        String sql = "delete from test_run where id = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1,id);
            ResultSet rs = stmt.executeQuery();
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("deleteTestRun exception: %s", e.getMessage()));
        }
    }

    public void saveDetectedAnomalies(ArrayList<DetectedAnomaly> detectedAnomalies) throws ProjectDataServiceException {
        for ( DetectedAnomaly detectedAnomaly : detectedAnomalies ) {
            saveDetectedAnomaly(detectedAnomaly);
        }
    }


    public void saveWebsocketTrafficRecord( WebsocketTrafficRecord websocketTrafficRecord, int trafficRecordId ) throws ProjectDataServiceException {
        if ( websocketTrafficRecord.getId() > 0 ) {
            String sql = "UPDATE websocket_frame " +
                    "SET conversation_uuid = ?," +
                    "direction = ?," +
                    "flag_fin = ?," +
                    "flag_rsv1 = ?," +
                    "flag_rsv2 = ?, " +
                    "flag_rsv3 = ?," +
                    "flag_masked = ?," +
                    "opcode = ?," +
                    "payload_len = ?, " +
                    "mask = ?," +
                    "payload where id = ?";
            try {
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1,websocketTrafficRecord.getFrame().getConversationUUID());
                stmt.setInt(2,1);//stmt.setInt(1,websocketTrafficRecord.getFrame().getUpgradeMessageId());
                stmt.setInt(3,websocketTrafficRecord.getFrame().getDirection().equals(WebsocketDirection.INBOUND) ? 0 : 1);
                stmt.setInt(4,websocketTrafficRecord.getFrame().getFin());
                stmt.setInt(5,websocketTrafficRecord.getFrame().getRsv1());
                stmt.setInt(6,websocketTrafficRecord.getFrame().getRsv2());
                stmt.setInt(7,websocketTrafficRecord.getFrame().getRsv3());
                stmt.setInt(8,websocketTrafficRecord.getFrame().getMasked());
                stmt.setString(9,websocketTrafficRecord.getFrame().getOpcode().toString());
                stmt.setInt(10,websocketTrafficRecord.getFrame().getPayloadLength());

                if ( websocketTrafficRecord.getFrame().getMaskKey() != null ) {
                    stmt.setBytes(11,websocketTrafficRecord.getFrame().getMaskKey());
                }
                else {
                    stmt.setNull(11,Types.BLOB);
                }

                if ( websocketTrafficRecord.getFrame().getPayload() != null ) {
                    stmt.setBytes(12,websocketTrafficRecord.getFrame().getPayload());
                }
                else {
                    stmt.setNull(12,Types.BLOB);
                }
                stmt.setInt(13, websocketTrafficRecord.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new ProjectDataServiceException(String.format("saveWebsocketTrafficRecord exception: %s", e.getMessage()));
            }
        }
        else {
            String sql = "INSERT INTO websocket_frame ( " +
                    "message_uuid," +
                    "upgrade_message_uuid," +
                    "conversation_uuid," +
                    "upgrade_message_id," +
                    "traffic_record_id," +
                    "created," +
                    "direction," +
                    "flag_fin," +
                    "flag_rsv1," +
                    "flag_rsv2," +
                    "flag_rsv3," +
                    "flag_masked," +
                    "opcode, " +
                    "payload_len," +
                    "mask," +
                    "payload" +
                    ") values ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? )";
            try {
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1,websocketTrafficRecord.getFrame().getMessageUUID());
                stmt.setString(2,websocketTrafficRecord.getFrame().getUpgradeMessageUUID());
                stmt.setString(3,websocketTrafficRecord.getFrame().getConversationUUID());
                stmt.setInt(4,1);//stmt.setInt(1,websocketTrafficRecord.getFrame().getUpgradeMessageId());
                stmt.setInt(5,trafficRecordId);
                stmt.setInt(6,(int)System.currentTimeMillis());
                stmt.setInt(7,websocketTrafficRecord.getFrame().getDirection().equals(WebsocketDirection.INBOUND) ? 0 : 1);
                stmt.setInt(8,websocketTrafficRecord.getFrame().getFin());
                stmt.setInt(9,websocketTrafficRecord.getFrame().getRsv1());
                stmt.setInt(10,websocketTrafficRecord.getFrame().getRsv2());
                stmt.setInt(11,websocketTrafficRecord.getFrame().getRsv3());
                stmt.setInt(12,websocketTrafficRecord.getFrame().getMasked());
                stmt.setString(13,websocketTrafficRecord.getFrame().getOpcode().toString());
                stmt.setInt(14,websocketTrafficRecord.getFrame().getPayloadLength());

                if ( websocketTrafficRecord.getFrame().getMaskKey() != null ) {
                    stmt.setBytes(15,websocketTrafficRecord.getFrame().getMaskKey());
                }
                else {
                    stmt.setNull(15,Types.BLOB);
                }

                if ( websocketTrafficRecord.getFrame().getPayload() != null ) {
                    stmt.setBytes(16,websocketTrafficRecord.getFrame().getPayload());
                }
                else {
                    stmt.setNull(16,Types.BLOB);
                }
                stmt.executeUpdate();
                websocketTrafficRecord.setId(getLastInsert());
            } catch (SQLException e) {
                throw new ProjectDataServiceException(String.format("saveWebsocketTrafficRecord exception: %s", e.getMessage()));
            }
        }
    }
    public void saveHttpTrafficRecord ( HttpTrafficRecord httpTrafficRecord, int trafficRecordId ) throws ProjectDataServiceException {
        if ( httpTrafficRecord.getId() > 0 ) {
            String sql = "UPDATE http_request_response_pair" +
                    "SET traffic_record_id, SET created = ?," +
                    "is_tls = ?," +
                    "method = ?," +
                    "url = ?," +
                    "code = ?," +
                    "request_header = ?," +
                    "request_body = ?," +
                    "response_header = ?, " +
                    "response_body = ?" +
                    "where id = ?";
            try {
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, trafficRecordId);
                stmt.setInt(2, httpTrafficRecord.getRequest().isSslEnabled() ? 1 : 0);
                stmt.setString(3, httpTrafficRecord.getRequest().getHttpMethod());
                stmt.setString(4, httpTrafficRecord.getRequest().getUrl());
                stmt.setInt(5, httpTrafficRecord.getResponse().getStatusCode());

                if ( httpTrafficRecord.getRequest().getHeaderBytes() != null ) {
                    stmt.setBytes(6,httpTrafficRecord.getRequest().getHeaderBytes());
                }
                else {
                    stmt.setNull(6,Types.BLOB);
                }

                if ( httpTrafficRecord.getRequest().getMessageBody() != null ) {
                    stmt.setBytes(7,httpTrafficRecord.getRequest().getMessageBody());
                }
                else {
                    stmt.setNull(7,Types.BLOB);
                }

                if ( httpTrafficRecord.getResponse().getHeaderBytes() != null ) {
                    stmt.setBytes(8,httpTrafficRecord.getResponse().getHeaderBytes());
                }
                else {
                    stmt.setNull(8,Types.BLOB);
                }

                if ( httpTrafficRecord.getResponse().getMessageBody() != null ) {
                    stmt.setBytes(9,httpTrafficRecord.getResponse().getMessageBody());
                }
                else {
                    stmt.setNull(9,Types.BLOB);
                }
                stmt.setInt(10, trafficRecordId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new ProjectDataServiceException(String.format("saveHttpTrafficRecord exception: %s", e.getMessage()));
            }
        }
        else {
            String sql = "INSERT INTO http_request_response_pair (" +
                    "traffic_record_id," +
                    "message_uuid," +
                    "created," +
                    "is_tls," +
                    "method," +
                    "url," +
                    "code," +
                    "request_header," +
                    "request_body," +
                    "response_header," +
                    "response_body" +
                    ") values ( ?,?,?,?,?,?,?,?,?,?,? )";
            try {
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, trafficRecordId);
                stmt.setString(2, httpTrafficRecord.getRequest().getMessageUUID());
                stmt.setInt(3,(int)System.currentTimeMillis());
                stmt.setInt(4, httpTrafficRecord.getRequest().isSslEnabled() ? 1 : 0);
                stmt.setString(5, httpTrafficRecord.getRequest().getHttpMethod());
                stmt.setString(6, httpTrafficRecord.getRequest().getUrl());
                stmt.setInt(7, httpTrafficRecord.getResponse().getStatusCode());

                if ( httpTrafficRecord.getRequest().getHeaderBytes() != null ) {
                    stmt.setBytes(8,httpTrafficRecord.getRequest().getHeaderBytes());
                }
                else {
                    stmt.setNull(8,Types.BLOB);
                }

                if ( httpTrafficRecord.getRequest().getMessageBody() != null ) {
                    stmt.setBytes(9,httpTrafficRecord.getRequest().getMessageBody());
                }
                else {
                    stmt.setNull(9,Types.BLOB);
                }

                if ( httpTrafficRecord.getResponse().getHeaderBytes() != null ) {
                    stmt.setBytes(10,httpTrafficRecord.getResponse().getHeaderBytes());
                }
                else {
                    stmt.setNull(10,Types.BLOB);
                }

                if ( httpTrafficRecord.getResponse().getMessageBody() != null ) {
                    stmt.setBytes(11,httpTrafficRecord.getResponse().getMessageBody());
                }
                else {
                    stmt.setNull(11,Types.BLOB);
                }
                stmt.executeUpdate();
                httpTrafficRecord.setId(getLastInsert());
            } catch (SQLException e) {
                throw new ProjectDataServiceException(String.format("saveHttpTrafficRecord exception: %s", e.getMessage()));
            }
        }
    }

    /*
        use for transferring a conversation from proxy to immediate
     */
    public void updateTrafficSource ( String upgradeMessageUUID, TrafficSource trafficSource ) throws ProjectDataServiceException {
        String updateHttp = "update traffic_record " +
                "set traffic_source = ? " +
                "where id in ( select " +
                "traffic_record.id from traffic_record " +
                "join http_request_response_pair on http_request_response_pair.traffic_record_id = traffic_record.id " +
                "where http_request_response_pair.message_uuid = ?)";
        String updateWebsockets = "update traffic_record " +
                "set traffic_source = ? " +
                "where id in ( select " +
                "traffic_record.id from traffic_record " +
                "join websocket_frame on websocket_frame.traffic_record_id = traffic_record.id " +
                "where websocket_frame.upgrade_message_uuid = ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(updateHttp);
            stmt.setString(1, trafficSource.toString());
            stmt.setString(2, upgradeMessageUUID);
            stmt.executeUpdate();

            stmt = connection.prepareStatement(updateWebsockets);
            stmt.setString(1, trafficSource.toString());
            stmt.setString(2, upgradeMessageUUID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("saveTrafficRecord exception: %s", e.getMessage()));
        }
    }

    public void saveTrafficRecord(TrafficRecord trafficRecord ) throws ProjectDataServiceException {
        if ( trafficRecord.getId() > 0 ) {
            String sql = "UPDATE traffic_record " +
                    "SET traffic_source = ?," +
                    "test_run_id = ?," +
                    "highlight_colour = ?" +
                    " where id = ?";
            try {
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, trafficRecord.getTrafficSource().toString());
                stmt.setInt(2, trafficRecord.getTestRunId());
                stmt.setString(3, "");
                stmt.setInt(4, trafficRecord.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new ProjectDataServiceException(String.format("saveTrafficRecord exception: %s", e.getMessage()));
            }
        }
        else {
            String sql = "INSERT INTO traffic_record (" +
                    "created," +
                    "traffic_source," +
                    "test_run_id," +
                    "highlight_colour" +
                    ")" +
                    "values ( ?,?,?,? )";
            try {
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, (int)System.currentTimeMillis());
                stmt.setString(2, trafficRecord.getTrafficSource().toString());
                stmt.setInt(3, trafficRecord.getTestRunId());
                stmt.setInt(4, trafficRecord.getHighlightColour().getRGB());
                stmt.executeUpdate();
                trafficRecord.setId(getLastInsert());
            } catch (SQLException e) {
                throw new ProjectDataServiceException(String.format("saveTrafficRecord exception: %s", e.getMessage()));
            }
        }
    }



    public int getRecordCount( TrafficRecordType trafficRecordType ) throws IOException, ClassNotFoundException, ProjectDataServiceException {
        int count = 0;
        String sql;
        if ( trafficRecordType.equals(TrafficRecordType.HTTP)) {
            sql = "select count(id) from http_request_response_pair";
        }
        else {
            sql = "select count(id) from websocket_frame";
        }
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getRecordCount exception: %s", e.getMessage()));
        }
        return count;
    }

    public String getTestRunName( String testRunName ) {
        return null;
    }

    /*
        Get id by test run name
     */
    public int getTestRunIdByName( String name ) throws IOException, ClassNotFoundException, ProjectDataServiceException {
        int id = 0;
        String sql = "select id from test_run where name = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                id = rs.getInt("id");
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getTestRunIdByName exception: %s", e.getMessage()));
        }
        return id;
    }
    /*
        The test run name is testname-runnumber
     */
    public String getNextTestRunName( String testRunName ) throws ProjectDataServiceException {
        String baseName = TestUtil.getTestBaseName(testRunName);
        ArrayList<String> runBatch = new ArrayList<String>();
        int maxTest = 0;
        for ( String testName : getTestNames() ) {
            if ( testName.startsWith(baseName)) {
                int curTestRunNo = TestUtil.getTestRunNo(testName);
                if ( curTestRunNo > maxTest ) {
                    maxTest = curTestRunNo;
                }
            }
        }
        return String.format("%s-%d", baseName, maxTest+1 );
    }


    public ArrayList<HttpTrafficRecord> getHttpTrafficRecordsBySource ( TrafficSource trafficSource, int offset, int limit ) throws ProjectDataServiceException {
        ArrayList<HttpTrafficRecord> records = new ArrayList<HttpTrafficRecord>();
        String sql = "select " +
                "http_request_response_pair.id," +
                "is_tls," +
                "message_uuid," +
                "request_header," +
                "request_body," +
                "response_header," +
                "response_body" +
                " from http_request_response_pair" +
                " join traffic_record on" +
                " http_request_response_pair.traffic_record_id = traffic_record.id" +
                " where traffic_record.traffic_source = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,trafficSource.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                HttpMessage request = new HttpMessage( rs.getString("message_uuid"), rs.getBytes("request_header"),rs.getBytes("request_body"));
                HttpMessage response = new HttpMessage(rs.getString("message_uuid"), rs.getBytes("response_header"),rs.getBytes("response_body"));
                if ( rs.getInt("is_tls") == 1) {
                    request.setSslEnabled(true);
                }
                records.add(new HttpTrafficRecord(
                        rs.getInt(1),
                        request,
                        response
                ));
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getHttpTrafficRecordsBySource exception ( SQLException ): %s", e.getMessage()));
        } catch (HttpMessageParseException e) {
            throw new ProjectDataServiceException(String.format("getHttpTrafficRecordsBySource exception ( HttpMessageParseException ): %s", e.getMessage()));
        }
        return records;
    }

    public int countHttpTrafficRecordBySource ( TrafficSource trafficSource ) throws IOException, ProjectDataServiceException {
        int rowcount = 0;
        String sql = "select " +
                "count(http_request_response_pair.id) as rowcount" +
                " from http_request_response_pair" +
                " join traffic_record on" +
                " http_request_response_pair.traffic_record_id = traffic_record.id" +
                " where traffic_record.traffic_source = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,trafficSource.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                rowcount = rs.getInt("rowcount");
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getHttpTrafficRecord exception ( SQLException ): %s", e.getMessage()));
        }
        return rowcount;
    }


    public ArrayList<WebsocketTrafficRecord> getWebsocketTrafficRecordBySource ( TrafficSource trafficSource, int offset, int limit ) throws IOException, ProjectDataServiceException {
        ArrayList<WebsocketTrafficRecord> records = new ArrayList<WebsocketTrafficRecord>();
        String sql = "select " +
                "websocket_frame.id," +
                "websocket_frame.message_uuid," +
                "websocket_frame.traffic_record_id," +
                "websocket_frame.conversation_uuid," +
                "websocket_frame.upgrade_message_id," +
                "websocket_frame.upgrade_message_uuid," +
                "websocket_frame.created," +
                "websocket_frame.direction," +
                "websocket_frame.flag_fin," +
                "websocket_frame.flag_rsv1," +
                "websocket_frame.flag_rsv2," +
                "websocket_frame.flag_rsv3," +
                "websocket_frame.flag_masked," +
                "websocket_frame.opcode," +
                "websocket_frame.payload_len," +
                "websocket_frame.mask," +
                "websocket_frame.payload," +
                "traffic_record.highlight_color," +
                "test_run.name as test_name" +
                " from websocket_frame join traffic_record on" +
                " websocket_frame.traffic_record_id = traffic_record.id" +
                " join test_run on traffic_record.test_run_id = test_run.id where traffic_record.traffic_source = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,trafficSource.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                WebsocketFrame websocketFrame = new WebsocketFrame(
                        rs.getInt("id"),
                        rs.getString("message_uuid"),
                        rs.getString("upgrade_message_uuid"),
                        rs.getInt("flag_fin"),
                        rs.getInt("flag_rsv1"),
                        rs.getInt("flag_rsv2"),
                        rs.getInt("flag_rsv3"),
                        rs.getInt("flag_masked"),
                        websocketFrameTypeFromString(rs.getString("opcode")),
                        rs.getInt("payload_len"),
                        rs.getBytes("mask"),
                        rs.getBytes("payload"),
                        rs.getInt("direction") == 0 ? WebsocketDirection.INBOUND : WebsocketDirection.OUTBOUND
                );
                WebsocketTrafficRecord wsTrafficRecord = new WebsocketTrafficRecord(rs.getInt("id"),websocketFrame);
                wsTrafficRecord.setHighlightColour(new Color(rs.getInt("highlight_color")));
                wsTrafficRecord.setTestName(rs.getString("test_name"));
                records.add(wsTrafficRecord);
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getWebsocketTrafficRecordById exception ( SQLException ): %s", e.getMessage()));
        }
        return records;
    }

    public ArrayList<WebsocketTrafficRecord> getWebsocketTrafficRecordsByUpgradeMessageUUID ( String upgradeMessageUUID ) throws IOException, ProjectDataServiceException {
        ArrayList<WebsocketTrafficRecord> records = new ArrayList<WebsocketTrafficRecord>();
        String sql = "select " +
                "id," +
                "message_uuid," +
                "traffic_record_id," +
                "conversation_uuid," +
                "upgrade_message_id," +
                "upgrade_message_uuid," +
                "created," +
                "direction," +
                "flag_fin," +
                "flag_rsv1," +
                "flag_rsv2," +
                "flag_rsv3," +
                "flag_masked," +
                "opcode," +
                "payload_len," +
                "mask," +
                "payload" +
                " from websocket_frame where upgrade_message_uuid = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,upgradeMessageUUID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                WebsocketFrameType websocketFrameType;

                WebsocketFrame websocketFrame = new WebsocketFrame(
                        rs.getInt("id"),
                        rs.getString("message_uuid"),
                        rs.getString("upgrade_message_uuid"),
                        rs.getInt("flag_fin"),
                        rs.getInt("flag_rsv1"),
                        rs.getInt("flag_rsv2"),
                        rs.getInt("flag_rsv3"),
                        rs.getInt("flag_masked"),
                        websocketFrameTypeFromString(rs.getString("opcode")),
                        rs.getInt("payload_len"),
                        rs.getBytes("mask"),
                        rs.getBytes("payload"),
                        rs.getInt("direction") == 0 ? WebsocketDirection.INBOUND : WebsocketDirection.OUTBOUND
                );
                records.add(new WebsocketTrafficRecord(rs.getInt("id"),websocketFrame));
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getWebsocketTrafficRecordById exception ( SQLException ): %s", e.getMessage()));
        }
        return records;
    }

    public int countWebsocketTrafficRecordBySource ( TrafficSource trafficSource ) throws IOException, ProjectDataServiceException {
        int rowCount = 0;
        String sql = "select count(websocket_frame.id) as rowcount from websocket_frame join traffic_record on" +
                " websocket_frame.traffic_record_id = traffic_record.id" +
                " where traffic_record.traffic_source = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,trafficSource.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                rowCount = rs.getInt("rowcount");
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getWebsocketTrafficRecordById exception ( SQLException ): %s", e.getMessage()));
        }
        return rowCount;
    }


    public HttpTrafficRecord getHttpTrafficRecordByUUID ( String uuid ) throws IOException, ProjectDataServiceException {
        HttpTrafficRecord httpTrafficRecord = null;
        String sql = "select " +
                "id," +
                "is_tls," +
                "message_uuid," +
                "request_header," +
                "request_body," +
                "response_header," +
                "response_body" +
                " from http_request_response_pair where message_uuid = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,uuid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                HttpMessage request = new HttpMessage( rs.getString("message_uuid"), rs.getBytes("request_header"),rs.getBytes("request_body"));
                HttpMessage response = new HttpMessage(rs.getString("message_uuid"), rs.getBytes("response_header"),rs.getBytes("response_body"));
                if ( rs.getInt("is_tls") == 1) {
                    request.setSslEnabled(true);
                }
                httpTrafficRecord = new HttpTrafficRecord(
                        rs.getInt(1),
                        request,
                        response
                );
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getHttpTrafficRecord exception ( SQLException ): %s", e.getMessage()));
        } catch (HttpMessageParseException e) {
            throw new ProjectDataServiceException(String.format("getHttpTrafficRecord exception ( HttpMessageParseException ): %s", e.getMessage()));
        }
        return httpTrafficRecord;
    }

    public HttpTrafficRecord getHttpTrafficRecord ( int id ) throws IOException, ProjectDataServiceException {
        HttpTrafficRecord httpTrafficRecord = null;
        String sql = "select" +
                "id," +
                "message_uuid," +
                "request_header," +
                "request_body," +
                "response_header," +
                "response_body" +
                " from http_request_response_pair where id = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1,id);
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                HttpMessage request = new HttpMessage( rs.getString("message_uuid"), rs.getBytes(7),rs.getBytes(8));
                HttpMessage response = new HttpMessage(rs.getString("message_uuid"), rs.getBytes(9),rs.getBytes(10));
                httpTrafficRecord = new HttpTrafficRecord(
                        rs.getInt(1),
                        request,
                        response
                );
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getHttpTrafficRecord exception ( SQLException ): %s", e.getMessage()));
        } catch (HttpMessageParseException e) {
            throw new ProjectDataServiceException(String.format("getHttpTrafficRecord exception ( HttpMessageParseException ): %s", e.getMessage()));
        }
        return httpTrafficRecord;
    }

    public ArrayList<HttpTrafficRecord> getHttpTrafficRecords ( int offset, int count ) throws IOException, ProjectDataServiceException {
        ArrayList<HttpTrafficRecord> httpTrafficRecords = new ArrayList<HttpTrafficRecord>();
        String sql = "select " +
                "id," +
                "message_uuid," +
                "is_tls," +
                "request_header," +
                "request_body," +
                "response_header," +
                "response_body" +
                " from http_request_response_pair limit ?, ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1,offset);
            stmt.setInt(2,count);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                HttpMessage request = new HttpMessage( rs.getString("message_uuid"), rs.getBytes("request_header"),rs.getBytes("request_body"));
                HttpMessage response = new HttpMessage(rs.getString("message_uuid"), rs.getBytes("response_header"),rs.getBytes("response_body"));
                if ( rs.getInt("is_tls") == 1) {
                    request.setSslEnabled(true);
                }
                httpTrafficRecords.add(new HttpTrafficRecord(
                        rs.getInt(1),
                        request,
                        response
                ));
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getHttpTrafficRecord exception ( SQLException ): %s", e.getMessage()));
        } catch (HttpMessageParseException e) {
            throw new ProjectDataServiceException(String.format("getHttpTrafficRecord exception ( HttpMessageParseException ): %s", e.getMessage()));
        }
        return httpTrafficRecords;
    }

    public WebsocketTrafficRecord getWebsocketTrafficRecordById (int id ) throws IOException, ProjectDataServiceException {
        WebsocketTrafficRecord websocketTrafficRecord = null;
        String sql = "select" +
                "id," +
                "message_uuid," +
                "traffic_record_id," +
                "conversation_uuid," +
                "upgrade_message_id," +
                "created," +
                "direction," +
                "flag_fin," +
                "flag_rsv1," +
                "flag_rsv2," +
                "flag_rsv3," +
                "flag_masked," +
                "opcode," +
                "payload_len," +
                "mask," +
                "payload" +
                " from websocket_frame where id = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1,id);
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                WebsocketFrameType websocketFrameType;

                WebsocketFrame websocketFrame = new WebsocketFrame(
                    rs.getInt("id"),
                    rs.getString("message_uuid"),
                    rs.getString("upgrade_message_uuid"),
                    rs.getInt("fin"),
                    rs.getInt("rsv1"),
                    rs.getInt("rsv2"),
                    rs.getInt("rsv3"),
                    rs.getInt("masked"),
                    websocketFrameTypeFromString(rs.getString("opcode")),
                    rs.getInt("payload_len"),
                    rs.getBytes("mask"),
                    rs.getBytes("payload"),
                    rs.getInt("direction") == 0 ? WebsocketDirection.INBOUND : WebsocketDirection.OUTBOUND
                );
                websocketFrame.setMessageUUID(rs.getString("message_uuid"));
                websocketTrafficRecord = new WebsocketTrafficRecord(rs.getInt("id"),websocketFrame);
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getWebsocketTrafficRecordById exception ( SQLException ): %s", e.getMessage()));
        }
        return websocketTrafficRecord;
    }

    public WebsocketTrafficRecord getWebsocketTrafficRecordByUUID (String uuid ) throws IOException, ProjectDataServiceException {
        WebsocketTrafficRecord websocketTrafficRecord = null;
        String sql = "select " +
                "id," +
                "message_uuid," +
                "traffic_record_id," +
                "conversation_uuid," +
                "upgrade_message_id," +
                "upgrade_message_uuid," +
                "created," +
                "direction," +
                "flag_fin," +
                "flag_rsv1," +
                "flag_rsv2," +
                "flag_rsv3," +
                "flag_masked," +
                "opcode," +
                "payload_len," +
                "mask," +
                "payload" +
                " from websocket_frame where message_uuid = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,uuid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                WebsocketFrameType websocketFrameType;

                WebsocketFrame websocketFrame = new WebsocketFrame(
                        rs.getInt("id"),
                        rs.getString("message_uuid"),
                        rs.getString("upgrade_message_uuid"),
                        rs.getInt("flag_fin"),
                        rs.getInt("flag_rsv1"),
                        rs.getInt("flag_rsv2"),
                        rs.getInt("flag_rsv3"),
                        rs.getInt("flag_masked"),
                        websocketFrameTypeFromString(rs.getString("opcode")),
                        rs.getInt("payload_len"),
                        rs.getBytes("mask"),
                        rs.getBytes("payload"),
                        rs.getInt("direction") == 0 ? WebsocketDirection.INBOUND : WebsocketDirection.OUTBOUND
                );
                websocketTrafficRecord = new WebsocketTrafficRecord(rs.getInt("id"),websocketFrame);
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getWebsocketTrafficRecordById exception ( SQLException ): %s", e.getMessage()));
        }
        return websocketTrafficRecord;
    }

    public ArrayList<WebsocketTrafficRecord> getSearchableRecords ( int offset, int count ) throws IOException, ProjectDataServiceException {
        ArrayList<WebsocketTrafficRecord> websocketTrafficRecords = new ArrayList<WebsocketTrafficRecord>();
        String sql = "select " +
                "websocket_frame.id," +
                "message_uuid," +
                "traffic_record_id," +
                "conversation_uuid," +
                "upgrade_message_uuid," +
                "traffic_record.created," +
                "direction," +
                "flag_fin," +
                "flag_rsv1," +
                "flag_rsv2," +
                "flag_rsv3," +
                "flag_masked," +
                "opcode," +
                "payload_len," +
                "mask," +
                "payload," +
                "traffic_record.traffic_source " +
                "from websocket_frame join traffic_record on websocket_frame.traffic_record_id = traffic_record.id limit ? offset ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1,count);
            stmt.setInt(2,offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                WebsocketFrame websocketFrame = new WebsocketFrame(
                        rs.getInt("id"),
                        rs.getString("message_uuid"),
                        rs.getString("upgrade_message_uuid"),
                        rs.getInt("flag_fin"),
                        rs.getInt("flag_rsv1"),
                        rs.getInt("flag_rsv2"),
                        rs.getInt("flag_rsv3"),
                        rs.getInt("flag_masked"),
                        websocketFrameTypeFromString(rs.getString("opcode")),
                        rs.getInt("payload_len"),
                        rs.getBytes("mask"),
                        rs.getBytes("payload"),
                        rs.getInt("direction") == 0 ? WebsocketDirection.INBOUND : WebsocketDirection.OUTBOUND
                );
                websocketFrame.setMessageUUID(rs.getString("message_uuid"));
                WebsocketTrafficRecord rec = new WebsocketTrafficRecord(rs.getInt("id"),websocketFrame);
                rec.setTrafficSource(trafficSourceFromString(rs.getString("traffic_source")));
                websocketTrafficRecords.add(rec);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ProjectDataServiceException(String.format("getWebsocketTrafficRecordById exception ( SQLException ): %s", e.getMessage()));
        }
        return websocketTrafficRecords;
    }

    public ArrayList<WebsocketTrafficRecord> getWebsocketTrafficRecords ( int offset, int count ) throws IOException, ProjectDataServiceException {
        ArrayList<WebsocketTrafficRecord> websocketTrafficRecords = new ArrayList<WebsocketTrafficRecord>();
        String sql = "select " +
                "id," +
                "message_uuid," +
                "traffic_record_id," +
                "conversation_uuid," +
                "upgrade_message_uuid," +
                "created," +
                "direction," +
                "flag_fin," +
                "flag_rsv1," +
                "flag_rsv2," +
                "flag_rsv3," +
                "flag_masked," +
                "opcode," +
                "payload_len," +
                "mask," +
                "payload" +
                " from websocket_frame limit ? offset ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1,count);
            stmt.setInt(2,offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                WebsocketFrame websocketFrame = new WebsocketFrame(
                        rs.getInt("id"),
                        rs.getString("message_uuid"),
                        rs.getString("upgrade_message_uuid"),
                        rs.getInt("flag_fin"),
                        rs.getInt("flag_rsv1"),
                        rs.getInt("flag_rsv2"),
                        rs.getInt("flag_rsv3"),
                        rs.getInt("flag_masked"),
                        websocketFrameTypeFromString(rs.getString("opcode")),
                        rs.getInt("payload_len"),
                        rs.getBytes("mask"),
                        rs.getBytes("payload"),
                        rs.getInt("direction") == 0 ? WebsocketDirection.INBOUND : WebsocketDirection.OUTBOUND
                );
                websocketFrame.setMessageUUID(rs.getString("message_uuid"));
                websocketTrafficRecords.add(new WebsocketTrafficRecord(rs.getInt("id"),websocketFrame));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ProjectDataServiceException(String.format("getWebsocketTrafficRecordById exception ( SQLException ): %s", e.getMessage()));
        }
        return websocketTrafficRecords;
    }


    public ArrayList<WebsocketTrafficRecord> getWebsocketTrafficRecordByTestName ( String testName ) throws IOException, ProjectDataServiceException {
        ArrayList<WebsocketTrafficRecord> websocketTrafficRecords = new ArrayList<WebsocketTrafficRecord>();
        String sql = "select\n" +
                "    traffic_record.id as traffic_record_id,\n" +
                "    traffic_record.created as created,\n" +
                "    traffic_record.traffic_source as traffic_source,\n" +
                "    traffic_record.test_run_id as test_run_id,\n" +
                "    traffic_record.highlight_colour as highlight_colour,\n" +
                "    test_run.id as test_run_id,\n" +
                "    test_run.test_type as test_type,\n" +
                "    test_run.start_time as start_time,\n" +
                "    test_run.end_time as end_time,\n" +
                "    test_run.name as name,\n" +
                "    test_run.note as test_note,\n" +
                "    test_run.auto_step_count as auto_step_count,\n" +
                "    test_run.auto_test_count as auto_test_count,\n" +
                "    test_run.auto_pct_complete as auto_pct_complete,\n" +
                "    test_run.reuse_connection as reuse_connection,\n" +
                "    test_run.continue_replay_after_test as continue_replay_after_test,\n" +
                "    test_run.auto_tests_completed as auto_tests_completed,\n" +
                "    test_run.auto_test_status as auto_test_status,\n" +
                "    websocket_frame.id as id,\n" +
                "    websocket_frame.conversation_uuid as conversation_uuid,\n" +
                "    websocket_frame.upgrade_message_id as upgrade_message_id,\n" +
                "    websocket_frame.upgrade_message_uuid as upgrade_message_uuid,\n" +
                "    websocket_frame.message_uuid as message_uuid,\n" +
                "    websocket_frame.created as created,\n" +
                "    websocket_frame.direction as direction,\n" +
                "    websocket_frame.flag_fin as flag_fin,\n" +
                "    websocket_frame.flag_rsv1 as flag_rsv1,\n" +
                "    websocket_frame.flag_rsv2 as flag_rsv2,\n" +
                "    websocket_frame.flag_rsv3 as flag_rsv3,\n" +
                "    websocket_frame.flag_masked as flag_masked,\n" +
                "    websocket_frame.opcode as opcode,\n" +
                "    websocket_frame.payload_len as payload_len,\n" +
                "    websocket_frame.mask as mask,\n" +
                "    websocket_frame.payload as payload\n" +
                " from traffic_record\n" +
                "join test_run on traffic_record.test_run_id = test_run.id join websocket_frame on traffic_record.id = websocket_frame.traffic_record_id" +
                " where test_run.name = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,testName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                WebsocketFrameType websocketFrameType;
                WebsocketFrame frame = new WebsocketFrame(
                        rs.getInt("id"),
                        rs.getString("message_uuid"),
                        rs.getString("upgrade_message_uuid"),
                        rs.getInt("flag_fin"),
                        rs.getInt("flag_rsv1"),
                        rs.getInt("flag_rsv2"),
                        rs.getInt("flag_rsv3"),
                        rs.getInt("flag_masked"),
                        websocketFrameTypeFromString(rs.getString("opcode")),
                        rs.getInt("payload_len"),
                        rs.getBytes("mask"),
                        rs.getBytes("payload"),
                        rs.getInt("direction") == 0 ? WebsocketDirection.INBOUND : WebsocketDirection.OUTBOUND
                );
                WebsocketTrafficRecord trafficRecord = new WebsocketTrafficRecord(rs.getInt("id"),frame);
                trafficRecord.setHighlightColour(new Color(rs.getInt("highlight_colour")));
                websocketTrafficRecords.add(trafficRecord);
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getWebsocketTrafficRecordByTestName exception ( SQLException ): %s", e.getMessage()));
        }
        return websocketTrafficRecords;
    }

    /*
        Gets a testName by the conversationUUID, they should be 1:1
     */
    public String getTestNameByConversationUUID ( String conversationUUID ) throws ProjectDataServiceException {
        String name = null;
        String sql = "select " +
                "traffic_record.id as traffic_record_id," +
                "test_run.name as name," +
                "websocket_frame.conversation_uuid as conversation_uuid" +
                " from traffic_record" +
                " join test_run on traffic_record.test_run_id = test_run.id join websocket_frame on traffic_record.id = websocket_frame.traffic_record_id" +
                " where websocket_frame.conversation_uuid = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,conversationUUID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                name = rs.getString("name");
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getTestNameByConversationUUID exception: %s", e.getMessage()));
        }
        return name;
    }

    public ArrayList<WebsocketTrafficRecord> getWebsocketTrafficRecordByConversationUUID ( String conversationUUID ) throws IOException, ProjectDataServiceException {
        ArrayList<WebsocketTrafficRecord> websocketTrafficRecords = new ArrayList<WebsocketTrafficRecord>();
        String sql = "select websocket_frame.id as id," +
                "websocket_frame.message_uuid as message_uuid," +
                "websocket_frame.conversation_uuid as conversation_uuid," +
                "websocket_frame.upgrade_message_id as upgrade_message_id," +
                "websocket_frame.upgrade_message_uuid as upgrade_message_uuid," +
                "websocket_frame.created as created," +
                "websocket_frame.direction as direction," +
                "websocket_frame.flag_fin as flag_fin," +
                "websocket_frame.flag_rsv1 as flag_rsv1," +
                "websocket_frame.flag_rsv2 as flag_rsv2," +
                "websocket_frame.flag_rsv3 as flag_rsv3," +
                "websocket_frame.flag_masked as flag_masked," +
                "websocket_frame.opcode as opcode," +
                "websocket_frame.payload_len as payload_len," +
                "websocket_frame.mask as mask," +
                "websocket_frame.payload as payload" +
                " from websocket_frame" +
                " where conversation_uuid = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,conversationUUID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                WebsocketFrame frame = new WebsocketFrame(
                        rs.getInt("id"),
                        rs.getString("message_uuid"),
                        rs.getString("upgrade_message_uuid"),
                        rs.getInt("flag_fin"),
                        rs.getInt("flag_rsv1"),
                        rs.getInt("flag_rsv2"),
                        rs.getInt("flag_rsv3"),
                        rs.getInt("flag_masked"),
                        websocketFrameTypeFromString(rs.getString("opcode")),
                        rs.getInt("payload_len"),
                        rs.getBytes("mask"),
                        rs.getBytes("payload"),
                        rs.getInt("direction") == 0 ? WebsocketDirection.INBOUND : WebsocketDirection.OUTBOUND
                );
                frame.setConversationUUID(rs.getString("conversation_uuid"));
                websocketTrafficRecords.add(new WebsocketTrafficRecord(rs.getInt("id"),frame));
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getWebsocketTrafficRecordByConversationUUID exception ( SQLException ): %s", e.getMessage()));
        }
        return websocketTrafficRecords;
    }

    public WebsocketTrafficRecord getWebsocketTrafficRecordByUpgradeId (int id ) throws IOException {
        return null;
    }
    public ArrayList<String> getTestNames() throws ProjectDataServiceException {
        ArrayList<String> testNames = new ArrayList<String>();
        String sql = "select name from test_run";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                testNames.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            throw new ProjectDataServiceException(String.format("getTestNames exception ( SQLException ): %s", e.getMessage()));
        }
        return testNames;
    }
    public String getDbFilePath() {
        return dbFilePath;
    }

    public WebsocketFrameType websocketFrameTypeFromString(String websocketFrameStr ) {
        WebsocketFrameType websocketFrameType = null;
        switch ( websocketFrameStr ) {
            case "CONTINUATION":
                websocketFrameType = WebsocketFrameType.CONTINUATION;
                break;
            case "TEXT":
                websocketFrameType = WebsocketFrameType.TEXT;
                break;
            case "BINARY":
                websocketFrameType = WebsocketFrameType.BINARY;
                break;
            case "RESERVED1":
                websocketFrameType = WebsocketFrameType.RESERVED1;
                break;
            case "RESERVED2":
                websocketFrameType = WebsocketFrameType.RESERVED2;
                break;
            case "RESERVED3":
                websocketFrameType = WebsocketFrameType.RESERVED3;
                break;
            case "RESERVED4":
                websocketFrameType = WebsocketFrameType.RESERVED4;
                break;
            case "RESERVED5":
                websocketFrameType = WebsocketFrameType.RESERVED5;
                break;
            case "CLOSE":
                websocketFrameType = WebsocketFrameType.CLOSE;
                break;
            case "PING":
                websocketFrameType = WebsocketFrameType.PING;
                break;
            case "PONG":
                websocketFrameType = WebsocketFrameType.PONG;
                break;
        }
        return websocketFrameType;
    }

    public TrafficSource trafficSourceFromString(String str ) {
        TrafficSource trafficSource = null;
        switch ( str ) {
            case "PROXY":
                trafficSource = TrafficSource.PROXY;
                break;
            case "MANUAL_TEST":
                trafficSource = TrafficSource.MANUAL_TEST;
                break;
            case "AUTOMATED_TEST":
                trafficSource = TrafficSource.AUTOMATED_TEST;
                break;
            case "IMMEDIATE":
                trafficSource = TrafficSource.IMMEDIATE;
                break;
        }
        return trafficSource;
    }

}
