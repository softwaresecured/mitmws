package com.mitmws.util;

import com.mitmws.httpproxy.HttpMessage;
import com.mitmws.httpproxy.HttpProxy;
import com.mitmws.httpproxy.websocket.WebsocketFrame;
import com.mitmws.httpproxy.websocket.WebsocketFrameType;
import com.mitmws.httpproxy.trafficlogger.*;
import com.mitmws.mvc.view.panels.PnlWebsocketTrafficToolbar;
import com.mitmws.tester.TestSequenceItem;
import com.mitmws.tester.TestSequenceItemType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class GuiUtils {
    public static final DateFormat trafficTimeFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final Color ERROR_COLOR = new Color(240, 128, 128);
    public static final class NumberDocumentFilter extends DocumentFilter {

        public void replace(FilterBypass fb, int offset, int length, String string,
                            AttributeSet attr) throws BadLocationException {
            if (Pattern.matches("\\d+", string)) {
                super.replace(fb, offset, length, string, attr);
            }
        }
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (Pattern.matches("\\d+", string)) {
                super.insertString(fb, offset, string, attr);
            }
        }
    }

    public static void updateTabTitle( JTabbedPane tabbedPane, String tabName, String newTitle ) {
        for ( int i = 0; i < tabbedPane.getTabCount(); i++ ) {
            if ( tabbedPane.getTitleAt(i).startsWith(tabName)) {
                tabbedPane.setTitleAt(i, newTitle);
            }
        }
    }

    public static int[] stringCsvToIntCsv( String intCsv ) {
        int csv[] = new int[0];
        ArrayList<Integer> csvArr = new ArrayList<>();
        String parts[] = intCsv.split(",");
        for ( int i = 0; i < parts.length; i++ ) {
            try {
                int num = Integer.parseInt(parts[i]);
                csvArr.add(num);
            }
            catch ( NumberFormatException e ) {
                ;
            }
        }
        if ( csvArr.size() > 0 ) {
            csv = new int[csvArr.size()];
            for ( int i = 0; i < csvArr.size(); i++ ) {
                csv[i] = csvArr.get(i);
            }
        }
        return csv;
    }

    public static String getHexEditorContent(String editorContent) {
        String content = null;
        if ( editorContent != null ) {
            StringBuilder sb = new StringBuilder();
            editorContent = editorContent.replaceAll("[\\s\\n\\t\\r]","");
            Matcher m = Pattern.compile(">\\s?([A-F0-9]+)\\s?<",Pattern.MULTILINE|Pattern.DOTALL).matcher(editorContent);
            while ( m.find() ) {
                sb.append(m.group(1));
            }
            content = sb.toString();
        }
        return content;
    }

    public static String getHexEditorDocument(String content, int highlightStart, int highlightEnd, Color highlightColor ) {
        String style = String.format("<style>table {font-size: 10px;\nfont-family: monospace, monospace;\nborder-spacing: 0px;}\ntd    {padding: 0px;}\n.hlcell { background-color: rgb(%d, %d, %d); }</style>", highlightColor.getRed(),highlightColor.getGreen(),highlightColor.getBlue());
        String html = String.format("%s<table></table>", style);
        StringBuilder sb = new StringBuilder();
        int colLength = 16;
        String curRow = "";
        for ( int i = 0, j = 0; i < content.length(); i += 2) {
            if ( j >= colLength ) {
                sb.append(String.format("<tr>%s</tr>", curRow));
                curRow = "";
                j = 0;
            }
            if( i >= highlightStart*2 && i < highlightEnd*2 ) {
                curRow += String.format("<td class=\"hlcell\">%s</td>", content.substring(i,i+2));
            }
            else {
                curRow += String.format("<td>%s</td>", content.substring(i,i+2));
            }
            j++;
        }
        sb.append(String.format("<tr>%s</tr>", curRow));
        if ( sb.toString() != null ) {
            html = String.format("%s<table>%s</table>", style,sb.toString());
        }
        return html;
    }

    public static byte[] parseHexString( String hexStr ) {

        byte buff[] = null;
        if ( hexStr != null ) {
            String cleanHexStr = hexStr.trim().toUpperCase();
            if ( cleanHexStr.length() % 2 == 0 ) {
                buff = new byte[cleanHexStr.length()/2];
                for ( int i = 0, j = 0; i < cleanHexStr.length(); i += 2,j++ ) {
                    buff[j] = Integer.decode(String.format("0x%s", cleanHexStr.substring(i,i+2))).byteValue();
                }
            }
        }
        return buff;
    }

    public static String binToHexStr( byte buff[] ) {
        return binToHexStr(buff,"");
    }

    public static String binToHexStr( byte buff[], String prefix ) {
        String str = null;
        if ( buff != null ) {
            StringBuilder sb = new StringBuilder();
            for ( byte b : buff ) {
                sb.append(String.format("%s%s", prefix,String.format("%02x", b).toUpperCase()));
            }
            str = sb.toString();
        }
        return str;
    }

    public static String getBinPreviewStr( byte[] buff ) {
        String str = "";
        if ( buff != null ) {
            for ( byte b : buff ) {
                int c = (int)b;
                if ( c > 31 && c < 127 || c > 160 ) {
                    str += (char)c;
                }
                else {
                    str += ".";
                }
            }
        }
        return str;
    }


    /*
        Used for the traffic tables so binary frames are somewhat readable at a glance
     */
    public static String getTableBinPreviewStr( WebsocketFrame frame ) {
        String previewStr;
        if ( frame.getOpcode().equals(WebsocketFrameType.TEXT)) {
            previewStr = new String(frame.getPayloadUnmasked());
        }
        else {
            previewStr = getBinPreviewStr(frame.getPayloadUnmasked()).replaceAll("\\.+",".");
        }
        if ( previewStr.length() > 500 ) {
            previewStr = previewStr.substring(0,500);
        }
        return previewStr;
    }

    public static boolean validateRegex( String regex ) {
        try {
            Pattern p = Pattern.compile(regex);
            return true;
        }
        catch ( PatternSyntaxException e ) {
        }
        return false;
    }

    public static JPanel frameWrapComponent ( JComponent component, String frameTitle) {
        JPanel pnl = new JPanel();
        if ( frameTitle != null ) {
            pnl.setBorder(BorderFactory.createTitledBorder(frameTitle));
        }
        pnl.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        pnl.add(component,gbc);
        return pnl;
    }

    public static String getPatternString(Pattern pattern) {
        String patternStr = "";
        if ( pattern != null ) {
            patternStr = pattern.toString();
        }
        return patternStr;
    }

    public static int getMatchGroupCount(Pattern pattern) {
        Matcher matcher = pattern.matcher("");
        return matcher.groupCount();
    }


    public static void updateHttpTable(JTable tblHttpTraffic, HttpTrafficRecord rec) {
        boolean isTailing = isTableTailing(tblHttpTraffic);
        HttpMessage request = rec.getRequest();
        HttpMessage response = rec.getResponse();
        DefaultTableModel httpTrafficDataModel = (DefaultTableModel) tblHttpTraffic.getModel();
        httpTrafficDataModel.addRow(new Object[] {
                request.getMessageUUID(),
                trafficTimeFmt.format(new Date(request.getCreateTime())),
                request.getProtocol(),
                request.getHttpMethod(),
                response.getStatusCode(),
                request.getUrl()
        });
        if ( isTailing ) {
            tableSelectLast(tblHttpTraffic);
        }
        tableSelectFirst(tblHttpTraffic);
    }

    public static String[] getSelectedMessageIds( JTable tbl ) {
        String[] messageIds = null;
        int[] selectedRows = tbl.getSelectedRows();
        if ( selectedRows.length > 0 ) {
            messageIds = new String[selectedRows.length];
            for ( int i = 0; i < selectedRows.length; i++) {
                messageIds[i] = (String) tbl.getValueAt(selectedRows[i],0);
                //System.out.println(String.format("SELECTED MESSAGE: %s", messageIds[i]));
            }
        }
        return messageIds;
    }

    public static void setComboBoxItem ( JComboBox<String> cmb, String selectStr ) {
        for( int i = 0; i < cmb.getItemCount(); i++ ) {
            String curItem = cmb.getItemAt(i);
            if ( curItem.equals(selectStr) ) {
                cmb.setSelectedIndex(i);
                break;
            }
        }
    }
    /*
        Adds or updates an entry
     */

    public static void updateWebsocketTable (JTable tblWebsocketTraffic, WebsocketFrame frame, Color highlightColor ) {
        updateWebsocketTable ( tblWebsocketTraffic, frame, highlightColor, isTableTailing(tblWebsocketTraffic), true );
    }

    //TODO: Fix code duplication at updateWebSocketTable
    public static void addToWebsocketTable (JTable tblWebsocketTraffic, WebsocketFrame frame, Color highlightColor ) {
        String payloadStr = "";
        if ( frame.getPayloadUnmasked() != null ) {
            if (!frame.getOpcode().equals(WebsocketFrameType.CLOSE)) {
                payloadStr = new String(frame.getPayloadUnmasked());
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
        DefaultTableModel webocketTrafficDataModel = (DefaultTableModel) tblWebsocketTraffic.getModel();
        webocketTrafficDataModel.addRow(new Object[] {
                frame.getMessageUUID(),
                frame.getUpgradeMessageUUID(),
                highlightColor,
                trafficTimeFmt.format(new Date(frame.getCreateTime())),
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
    }

    public static void updateWebsocketTable (JTable tblWebsocketTraffic, WebsocketFrame frame, Color highlightColor, boolean tailing, boolean allowUpdate ) {
        DefaultTableModel webocketTrafficDataModel = (DefaultTableModel) tblWebsocketTraffic.getModel();

        String payloadStr = "";
        if ( frame.getPayloadUnmasked() != null ) {
            if (!frame.getOpcode().equals(WebsocketFrameType.CLOSE)) {
                payloadStr = new String(frame.getPayloadUnmasked());
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
        boolean updated = false;
        // update
        if ( allowUpdate ) {
            for ( int i = 0; i < tblWebsocketTraffic.getRowCount(); i++ ) {
                String curId = (String) tblWebsocketTraffic.getValueAt(i,0);
                if (curId.equals(frame.getMessageUUID())) {
                    webocketTrafficDataModel.setValueAt(highlightColor,i,3);
                    webocketTrafficDataModel.setValueAt( frame.getDirection() == WebsocketDirection.OUTBOUND ?">>" : "<<",i,4);
                    webocketTrafficDataModel.setValueAt(frame.getFin(),i,5);
                    webocketTrafficDataModel.setValueAt(frame.getRsv1(),i,6);
                    webocketTrafficDataModel.setValueAt(frame.getRsv2(),i,7);
                    webocketTrafficDataModel.setValueAt(frame.getRsv3(),i,8);
                    webocketTrafficDataModel.setValueAt(frame.getMasked(),i,9);
                    webocketTrafficDataModel.setValueAt(frame.getOpcode(),i,10);
                    webocketTrafficDataModel.setValueAt(frame.getPayloadLength(),i,11);
                    webocketTrafficDataModel.setValueAt(maskStr,i,12);
                    webocketTrafficDataModel.setValueAt(payloadStr,i,13);
                    updated = true;
                }
            }
        }
        // add
        if ( !updated ) {
            webocketTrafficDataModel.addRow(new Object[] {
                    frame.getMessageUUID(),
                    frame.getUpgradeMessageUUID(),
                    highlightColor,
                    trafficTimeFmt.format(new Date(frame.getCreateTime())),
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
        }
        /*
        if ( isTailing ) {
            tableSelectLast(tblWebsocketTraffic);
        }
        firstSelect(tblWebsocketTraffic);
         */
    }

    /*
        Used for things that have a -1 at the end
     */
    public static int getRunNumber( String runName ) {
        Pattern p = Pattern.compile("^.*-(\\d+)$");
        Matcher m = p.matcher(runName);
        if ( m.find() ) {
            if ( m.groupCount() > 0 ) {
                return Integer.parseInt(m.group(1));
            }
        }
        return 0;
    }

    /*
        Gets the run name without the unique number
     */
    public static String getRunName( String runName ) {
        Pattern p = Pattern.compile("^(.*)-\\d+$");
        Matcher m = p.matcher(runName);
        if ( m.find() ) {
            if ( m.groupCount() > 0 ) {
                runName = m.group(1);
            }
        }
        return runName;
    }

    public static Pattern getPattern ( String str ) {
        return Pattern.compile(str);
    }

    /*
        Gets a run name based on a list of existing runs
     */
    public static String getRunName( ArrayList<String> currentRuns, String runName ) {
        /*
        int maxRunId = 0;
        if ( currentRuns != null ) {
            if ( currentRuns.size() > 0 ) {
                runName = getRunName(currentRuns.get(currentRuns.size()-1));
                ArrayList<Integer> runIds = new ArrayList<Integer>();
                for( String curRun : currentRuns ) {
                    runIds.add(getRunNumber(curRun));
                }
                Collections.sort(runIds);
                if ( runIds.size() > 0 ) {
                    maxRunId = runIds.get(runIds.size()-1).intValue();
                    maxRunId += 1;
                    runName += "-" + maxRunId;
                }
            }
        }
         */
        return runName;
    }

    public static void updateTestSequenceTable (JTable tblWebsocketTraffic, TestSequenceItem testSequenceItem) {
        WebsocketFrame frame = testSequenceItem.getFrame();
        DefaultTableModel webSocketTrafficDataModel = (DefaultTableModel) tblWebsocketTraffic.getModel();

        String payloadStr = "";
        String maskStr = "--";
        String direction = "--";

        if ( testSequenceItem.getTestSequenceItemType().equals(TestSequenceItemType.FRAME)) {
            direction = frame.getDirection() == WebsocketDirection.INBOUND ? "←" : "→";
            if ( frame.getPayloadUnmasked() != null ) {
                if (!frame.getOpcode().equals(WebsocketFrameType.CLOSE)) {
                    payloadStr = new String(frame.getPayloadUnmasked());
                }
                else {
                    if ( frame.getPayloadUnmasked().length == 2 ) {
                        int closeCode = ByteBuffer.wrap(frame.getPayloadUnmasked()).getShort();
                        payloadStr = String.format("Code: %d", closeCode);
                    }
                }
            }


            if ( frame.getMaskKey() != null ) {
                maskStr = Integer.toHexString(ByteBuffer.wrap(frame.getMaskKey()).getInt());
            }
        }

        boolean updated = false;
        // update
        for ( int i = 0; i < tblWebsocketTraffic.getRowCount(); i++ ) {
            String curId = (String) tblWebsocketTraffic.getValueAt(i,2);
            if (curId.equals(testSequenceItem.getTestId())) {
                webSocketTrafficDataModel.setValueAt( testSequenceItem.getTestSequenceItemType().toString(),i,3);
                webSocketTrafficDataModel.setValueAt( testSequenceItem.getDelayMsec(),i,4);
                if ( testSequenceItem.getTestSequenceItemType().equals(TestSequenceItemType.FRAME)) {
                    webSocketTrafficDataModel.setValueAt( direction,i,5);
                    webSocketTrafficDataModel.setValueAt(frame.getFin(),i,6);
                    webSocketTrafficDataModel.setValueAt(frame.getRsv1(),i,7);
                    webSocketTrafficDataModel.setValueAt(frame.getRsv2(),i,8);
                    webSocketTrafficDataModel.setValueAt(frame.getRsv3(),i,9);
                    webSocketTrafficDataModel.setValueAt(frame.getMasked(),i,10);
                    webSocketTrafficDataModel.setValueAt(frame.getOpcode(),i,11);
                    webSocketTrafficDataModel.setValueAt(frame.getPayloadLength(),i,12);
                    webSocketTrafficDataModel.setValueAt(maskStr,i,13);
                    webSocketTrafficDataModel.setValueAt(payloadStr,i,14);
                }
                else {
                    webSocketTrafficDataModel.setValueAt( direction,i,5);
                    webSocketTrafficDataModel.setValueAt("--",i,6);
                    webSocketTrafficDataModel.setValueAt("--",i,7);
                    webSocketTrafficDataModel.setValueAt("--",i,8);
                    webSocketTrafficDataModel.setValueAt("--",i,9);
                    webSocketTrafficDataModel.setValueAt("--",i,10);
                    webSocketTrafficDataModel.setValueAt("--",i,11);
                    webSocketTrafficDataModel.setValueAt("--",i,12);
                    webSocketTrafficDataModel.setValueAt(maskStr,i,13);
                    webSocketTrafficDataModel.setValueAt(payloadStr,i,14);
                }
                updated = true;
            }
        }
        // add
        if ( !updated ) {
            if ( testSequenceItem.getTestSequenceItemType().equals(TestSequenceItemType.FRAME)) {
                webSocketTrafficDataModel.addRow(new Object[] {
                        frame.getMessageUUID(),
                        frame.getUpgradeMessageUUID(),
                        testSequenceItem.getTestId(),
                        testSequenceItem.getTestSequenceItemType().toString(),
                        testSequenceItem.getDelayMsec(),
                        direction,
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
            }
            else {
                webSocketTrafficDataModel.addRow(new Object[] {
                        null,
                        null,
                        testSequenceItem.getTestId(),
                        testSequenceItem.getTestSequenceItemType().toString(),
                        testSequenceItem.getDelayMsec(),
                        direction,
                        "--",
                        "--",
                        "--",
                        "--",
                        "--",
                        "--",
                        "--",
                        maskStr,
                        payloadStr
                });
            }
        }
    }
    public static Color generateColour() {
        Random r = new Random();
        return new Color(r.nextInt(55) + 200,
                r.nextInt(55) + 200,
                r.nextInt(55) + 200);
    }


    public static void updateWebsocketConversationReplayTable (JTable tblWebsocketTraffic, String testName, WebsocketFrame frame ) {
        updateWebsocketConversationReplayTable(tblWebsocketTraffic, testName, frame, true );
    }

    public static void updateWebsocketConversationReplayTable (JTable tblWebsocketTraffic, String testName, WebsocketFrame frame, boolean allowRowSelectUpdate ) {
        boolean isTailing = false;
        if ( allowRowSelectUpdate ) {
            isTailing = isTableTailing(tblWebsocketTraffic);
        }
        DefaultTableModel webSocketTrafficDataModel = (DefaultTableModel) tblWebsocketTraffic.getModel();
        /*
            Prepare the payload ( unmask / extensions )
         */
        String payloadStr = "";
        if ( frame.getPayloadUnmasked() != null ) {
            if (!frame.getOpcode().equals(WebsocketFrameType.CLOSE)) {
                payloadStr = new String(frame.getPayloadUnmasked());
            }
            else {
                if ( frame.getPayloadUnmasked().length == 2 ) {
                    int closeCode = ByteBuffer.wrap(frame.getPayloadUnmasked()).getShort();
                    payloadStr = String.format("Code: %d", closeCode);
                }
            }
        }
        webSocketTrafficDataModel.addRow(new Object[] {
                frame.getMessageUUID(),
                trafficTimeFmt.format(new Date(frame.getCreateTime())),
                testName,
                frame.getDirection() == WebsocketDirection.INBOUND ? "←" : "→",
                frame.getOpcode(),
                frame.getPayloadLength(),
                payloadStr
        });
        if ( allowRowSelectUpdate ) {
            if ( isTailing ) {
                tableSelectLast(tblWebsocketTraffic);
            }
            tableSelectFirst(tblWebsocketTraffic);
        }
    }

    public static void tableSelectLast ( JTable tbl ) {
        if ( tbl.getRowCount() > 0 ) {
            tbl.getSelectionModel().setSelectionInterval(tbl.getRowCount()-1,tbl.getRowCount()-1);
        }
    }

    public static boolean isTableTailing( JTable tbl ) {
        return tbl.getRowCount() > 0 && tbl.getSelectedRow() == tbl.getRowCount() - 1;
    }

    /*
        Selects a row if it is empty ( for first runs )
     */
    public static void tableSelectFirst(JTable tbl ) {
        if ( tbl.getRowCount() > 0 ) {
            tbl.getSelectionModel().setSelectionInterval(0,0);
        }
    }

    public static void clearTable( JTable tbl ) {
        DefaultTableModel tblModel = (DefaultTableModel) tbl.getModel();
        tblModel.setRowCount(0);
    }

    public static void insertIntoTextArea( JTextArea txtTarget, byte[] insText ) {
        if ( insText != null ) {
            int cursorPos = txtTarget.getCaretPosition();
            int selStart = txtTarget.getSelectionStart();
            int selEnd = txtTarget.getSelectionEnd();
            if ( cursorPos >= 0 ) {
                String curText = txtTarget.getText();
                StringBuilder sb = new StringBuilder();
                if ( selStart >= 0 && selEnd > 0 ) {
                    sb.append(curText, 0, selStart);
                    sb.append(new String(insText));
                    sb.append(curText.substring(selEnd));
                }
                else {
                    sb.append(curText.substring(cursorPos));
                    sb.append(new String(insText));
                    sb.append(curText, cursorPos, curText.length()-cursorPos);
                }
                txtTarget.setText(sb.toString());
            }
        }
    }

    public static void updateWebsocketConnectionsTable(TrafficRecord rec, JTable tblWebsocketConnections ) {
        if ( rec.getTrafficSource().equals(TrafficSource.PROXY)) {
            HttpMessage msg = rec.getHttpTrafficRecord().getRequest();
            String wsUrl = msg.getUrl().replaceFirst("(?i)http","ws");
            DefaultTableModel connectionsModel = (DefaultTableModel) tblWebsocketConnections.getModel();
            connectionsModel.addRow(new Object[] {
                    msg.getMessageUUID(),
                    trafficTimeFmt.format(new Date(msg.getCreateTime())),
                    "OPEN",
                    wsUrl
            });
        }
    }
    public static void updateWebsocketConnectionStatus(HttpProxy httpProxy, JTable tblWebsocketConnections) {

        String[] activeConnections = httpProxy.getActiveWebsocketConnections();
        DefaultTableModel connectionsModel = (DefaultTableModel) tblWebsocketConnections.getModel();
        // TODO: This can be simplified
        if ( connectionsModel.getRowCount() > 1 ) {
            for ( int i = 1; i < connectionsModel.getRowCount(); i++ ) {
                if ( Arrays.stream(activeConnections).anyMatch(connectionsModel.getValueAt(i,0)::equals)) {
                    connectionsModel.setValueAt("OPEN",i,2);
                }
                else {
                    connectionsModel.setValueAt("CLOSED",i,2);
                }
            }
        }
    }

    public static String getSnippet( String input, int maxLen ) {
        String snippet = input;
        if ( snippet.length() > maxLen ) {
            snippet = snippet.substring(0,maxLen);
        }
        return snippet;
    }

    /*
        Filters the shorter version of the websocket traffic table seen in manual tester
     */
    public static void updateShortWebsocketTableTrafficRowFilter(String messageId, PnlWebsocketTrafficToolbar pnlWebsocketTrafficToolbar, JTable tblWebsocketTraffic) {
        // sorter for proxy traffic
        // "messageId", "Time","Test name","--","OPCODE","LEN","Payload"
        TableRowSorter sorter = new TableRowSorter<>((DefaultTableModel) tblWebsocketTraffic.getModel());
        RowFilter<DefaultTableModel, Object> rf = null;
        try {
            ArrayList<RowFilter<Object,Object>> rfs = new ArrayList<>();
            // Filter for payload, direction, ping|pong
            // Direction - 3
            if ( !pnlWebsocketTrafficToolbar.jcmbDirections.getSelectedItem().toString().equals("Both")) {
                if ( pnlWebsocketTrafficToolbar.jcmbDirections.getSelectedItem().toString().equals("Inbound")) {
                    rfs.add(RowFilter.regexFilter("^<--$", 3));
                }
                else {
                    rfs.add(RowFilter.regexFilter("^-->$", 3));
                }
            }
            // Opcode - 9
            if ( pnlWebsocketTrafficToolbar.jchkHidePingPong.isSelected()) {
                rfs.add(RowFilter.regexFilter("^((?!PING|PONG).)*$", 4));
            }
            // Payload - 12

            if ( pnlWebsocketTrafficToolbar.jtxtPayloadRegex.getText().length() > 0 ) {
                rfs.add(RowFilter.regexFilter(pnlWebsocketTrafficToolbar.jtxtPayloadRegex.getText(), 6));
            }
            rf = RowFilter.andFilter(rfs);
        } catch (java.util.regex.PatternSyntaxException e) {
            e.printStackTrace();
            return;
        }
        sorter.setRowFilter(rf);
        tblWebsocketTraffic.setRowSorter(sorter);
    }
    /*
        Updates the row filters for websocket traffic ( seen on traffic tab )
     */
    public static void updateFullWebsocketTableTrafficRowFilter(String messageId, PnlWebsocketTrafficToolbar pnlWebsocketTrafficToolbar, JTable tblWebsocketTraffic) {
        // sorter for proxy traffic
        // "messageId", "upgradeMessageId","highlight","Time","--","FIN","R1","R2","R3","MSK","OPCODE","LEN","MASK","Payload"
        TableRowSorter sorter = new TableRowSorter<>((DefaultTableModel) tblWebsocketTraffic.getModel());
        RowFilter<DefaultTableModel, Object> rf = null;
        try {
            ArrayList<RowFilter<Object,Object>> rfs = new ArrayList<>();
            // Filter for payload, direction, ping|pong
            // Direction - 3
            if ( !pnlWebsocketTrafficToolbar.jcmbDirections.getSelectedItem().toString().equals("Both")) {
                if ( pnlWebsocketTrafficToolbar.jcmbDirections.getSelectedItem().toString().equals("Inbound")) {
                    rfs.add(RowFilter.regexFilter("^<--$", 4));
                }
                else {
                    rfs.add(RowFilter.regexFilter("^-->$", 4));
                }
            }
            // Opcode - 9
            if ( pnlWebsocketTrafficToolbar.jchkHidePingPong.isSelected()) {
                rfs.add(RowFilter.regexFilter("^((?!PING|PONG).)*$", 10));
            }
            // Payload - 12

            if ( pnlWebsocketTrafficToolbar.jtxtPayloadRegex.getText().length() > 0 ) {
                rfs.add(RowFilter.regexFilter(pnlWebsocketTrafficToolbar.jtxtPayloadRegex.getText(), 13));
            }

            // Conversation
            if ( messageId != null ) {
                rfs.add(RowFilter.regexFilter(messageId, 1));
            }
            rf = RowFilter.andFilter(rfs);
        } catch (java.util.regex.PatternSyntaxException e) {
            e.printStackTrace();
            return;
        }
        sorter.setRowFilter(rf);
        tblWebsocketTraffic.setRowSorter(sorter);
    }

    public static JScrollPane scrollPaneWrap( Component c, int horizontalPolicy, int verticalPolicy ) {
        JScrollPane scrollPane = new JScrollPane(c);
        scrollPane.setHorizontalScrollBarPolicy(horizontalPolicy);
        scrollPane.setVerticalScrollBarPolicy(verticalPolicy);
        return scrollPane;
    }

    public static String uppercaseFirst( String word ) {
        return word.substring(0,1).toUpperCase() + word.substring(1);
    }
}
