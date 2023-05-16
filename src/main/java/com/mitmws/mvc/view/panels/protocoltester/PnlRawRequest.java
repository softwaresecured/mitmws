package com.mitmws.mvc.view.panels.protocoltester;

import javax.swing.*;
import java.awt.*;

public class PnlRawRequest extends JPanel {
    public JTextArea jtxtRawFrameHex = new JTextArea();
    public PnlRawRequest() {
        initLayout();
    }
    private void initLayout() {
        JScrollPane scroll = new JScrollPane(jtxtRawFrameHex);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(scroll,gbc);
    }
}
