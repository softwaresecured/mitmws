package com.wsproxy.mvc.view.panels.immediate;

import javax.swing.*;
import java.awt.*;

public class PnlImmediateSendToolbar extends JPanel {




    public JButton jbtnSend = new JButton("Send");
    public JCheckBox jchkHandlePingPong = new JCheckBox("Auto ping/pong");
    public JRadioButton radioSendToClient = new JRadioButton("Client");
    public JRadioButton radioSendToServer = new JRadioButton("Server");
    public PnlImmediateSendToolbar() {
        initLayout();
    }
    public void initLayout() {
        radioSendToServer.setSelected(true);
        ButtonGroup sendGroup = new ButtonGroup();
        sendGroup.add(radioSendToClient);
        sendGroup.add(radioSendToServer);

        setLayout(new GridBagLayout());


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(new JPanel(),gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jchkHandlePingPong,gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(radioSendToClient,gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 3;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(radioSendToServer,gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 4;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jbtnSend,gbc);
    }
}
