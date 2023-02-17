package com.wsproxy.mvc.view.panels;

import javax.swing.*;
import java.awt.*;

public class PnlWebsocketTrafficToolbar extends JPanel {

    public JButton jbtnApply;
    public JTextField jtxtPayloadRegex;
    public JCheckBox jchkHidePingPong;
    public JComboBox jcmbDirections;

    public PnlWebsocketTrafficToolbar() {
        initLayout();
    }

    public void resetUi() {
        jtxtPayloadRegex.setText("");
        jchkHidePingPong.setSelected(false);
    }
    public void initLayout() {
        jbtnApply = new JButton("Apply");
        jcmbDirections = new JComboBox(new String[] {"Both","Inbound","Outbound"});
        jtxtPayloadRegex = new JTextField();
        jchkHidePingPong = new JCheckBox("Hide PING|PONG");

        //jbtnApply.setPreferredSize(new Dimension((int)jbtnApply.getPreferredSize().getWidth(),(int)jtxtPayloadRegex.getPreferredSize().getHeight()));
        //jcmbDirections.setPreferredSize(new Dimension((int)jcmbDirections.getPreferredSize().getWidth(),(int)jtxtPayloadRegex.getPreferredSize().getHeight()));

        setLayout( new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        add(new JLabel("Payload regex:"),gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridy = 0;
        gbc.gridx = 1;
        add(jtxtPayloadRegex,gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 2;
        add(new JLabel("Directions:"),gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 3;
        add(jcmbDirections,gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 4;
        add(jchkHidePingPong,gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 5;
        add(jbtnApply,gbc);
    }

}
