package com.mitmws.mvc.view.panels.immediate;

import javax.swing.*;
import java.awt.*;

public class PnlImmediateToolbar extends JPanel {
    public JButton btnConnect = new JButton("Connect");
    public JComboBox jcmbSessions = new JComboBox();
    public JCheckBox jchkJoinActiveSession = new JCheckBox("Drop-in");
    public JLabel lblStatus = new JLabel(" [Not connected] ");
    public PnlImmediateToolbar() {
        initLayout();
    }
    public void initLayout() {
        //jcmbSessions.setPreferredSize(new Dimension((int)jcmbSessions.getPreferredSize().getWidth(),(int)new JTextField().getPreferredSize().getHeight()));
        //btnConnect.setPreferredSize(new Dimension((int)btnConnect.getPreferredSize().getWidth(),(int)new JTextField().getPreferredSize().getHeight()));
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jchkJoinActiveSession,gbc);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(jcmbSessions,gbc);
        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(lblStatus,gbc);
        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 3;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(btnConnect,gbc);
    }
}
