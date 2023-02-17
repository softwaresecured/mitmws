package com.wsproxy.mvc.view.panels.protocoltester;
import com.wsproxy.httpproxy.trafficlogger.WebsocketDirection;
import com.wsproxy.util.GuiUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;

public class PnlTestOutputLog extends JPanel {
    public JList lstLog;
    public JTable tblTestTrafficLog;
    public JTextArea txtPreviewFrame = new JTextArea();
    public JLabel jlblTestStatus = new JLabel("Not running");
    public JProgressBar progressBar = new JProgressBar();
    private DefaultListModel logListModel;
    private DefaultTableModel testTrafficTableModel;
    public PnlTestOutputLog(DefaultListModel logListModel,DefaultTableModel testTrafficTableModel) {
        this.logListModel = logListModel;
        this.testTrafficTableModel = testTrafficTableModel;
        initLayout();
    }

    public void initLayout() {
        tblTestTrafficLog = new JTable(testTrafficTableModel);
        int[] trcolWidths = { 160,160,160, 25};
        for ( int i = 0; i < trcolWidths.length; i++ ) {
            tblTestTrafficLog.getColumnModel().getColumn(i).setMinWidth(trcolWidths[i]);
            tblTestTrafficLog.getColumnModel().getColumn(i).setMaxWidth(trcolWidths[i]);
            tblTestTrafficLog.getColumnModel().getColumn(i).setPreferredWidth(trcolWidths[i]);
        }
        tblTestTrafficLog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstLog  = new JList(logListModel);
        JScrollPane scrollLogs = new JScrollPane(lstLog);
        scrollLogs.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollLogs.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JScrollPane scrollFramePreview = new JScrollPane(txtPreviewFrame);
        scrollLogs.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollLogs.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JScrollPane scrollTestTraffic = new JScrollPane(tblTestTrafficLog);
        scrollTestTraffic.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTestTraffic.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0;
        gbc.weightx = 1;
        add(jlblTestStatus, gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 0;
        gbc.weightx = 1;
        add(progressBar, gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(scrollTestTraffic, gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 0;
        gbc.weightx = 1;
        add(new JLabel("Frame preview"), gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(scrollFramePreview, gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(scrollLogs, gbc);
    }
}
