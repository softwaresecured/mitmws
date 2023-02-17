package com.wsproxy.mvc.view.panels.logs;

import javax.swing.*;
import java.awt.*;

public class PnlLogs extends JPanel {
    public JTextArea jtxtLogs = null;
    public JScrollPane scrollLogs = null;
    public PnlLogs() {
        initLayout();
    }
    public void initLayout() {
        jtxtLogs = new JTextArea();
        jtxtLogs.setEditable(false);
        scrollLogs = new JScrollPane(jtxtLogs);
        scrollLogs.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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
