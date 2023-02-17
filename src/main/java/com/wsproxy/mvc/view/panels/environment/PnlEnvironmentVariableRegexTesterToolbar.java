package com.wsproxy.mvc.view.panels.environment;

import javax.swing.*;
import java.awt.*;

public class PnlEnvironmentVariableRegexTesterToolbar extends JPanel {
    public JButton btnTest = new JButton("Test");
    public JButton btnClear = new JButton("Clear");
    public PnlEnvironmentVariableRegexTesterToolbar() {
        initLayout();
    }
    public void initLayout() {

        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(new JPanel(),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(btnTest,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(btnClear,gbc);
    }
}
