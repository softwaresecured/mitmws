package com.wsproxy.mvc.view.panels;

import javax.swing.*;
import java.awt.*;

public class PnlTestLogToolbar extends JPanel {
    private JButton jbtnClear = null;
    public PnlTestLogToolbar() {
        initLayout();
    }

    public void initLayout() {
        jbtnClear = new JButton("Clear");
        jbtnClear.setPreferredSize(new Dimension((int) jbtnClear.getPreferredSize().getWidth(), (int) new JTextField().getPreferredSize().getHeight()));
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 1;
        add(new JPanel(), gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 1;
        add(jbtnClear, gbc);
    }
}
