package com.wsproxy.mvc.view.panels.trafficpanel;

import javax.swing.*;
import java.awt.*;

public class PnlHttpMessage extends JPanel {
    public JTextArea jtxtHttpMessage;

    public PnlHttpMessage() {
        initLayout();
    }
    public void initLayout() {
        jtxtHttpMessage = new JTextArea("");
        jtxtHttpMessage.setEditable(false);
        jtxtHttpMessage.setLineWrap(true);

        JScrollPane scroll = new JScrollPane(jtxtHttpMessage);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);


        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(scroll,gbc);
    }
}