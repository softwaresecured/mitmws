package com.mitmws.mvc.view.frames;

import javax.swing.*;
import java.awt.*;

public class FrmLogsView extends JFrame {
    public JTextArea jtxtLogs = null;
    public JScrollPane scrollLogs = null;
    public FrmLogsView() {
        initLayout();
    }
    public void initLayout() {
        setTitle("Logs");
        setSize(800,600);
        jtxtLogs = new JTextArea();
        jtxtLogs.setLineWrap(true);
        jtxtLogs.setEditable(false);
        scrollLogs = new JScrollPane(jtxtLogs);
        scrollLogs.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollLogs.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add( scrollLogs, gbc);
    }

    public void initEventListeners() {

    }
}
