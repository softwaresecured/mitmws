package com.wsproxy.mvc.view.panels.anomalies;

import javax.swing.*;
import java.awt.*;

public class PnlDetectedAnomaliesToolBar extends JPanel {
    public JTextField jtxtSearch = new JTextField();
    PnlDetectedAnomaliesToolBar() {
        initLayout();
    }
    public void initLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Search"),gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(jtxtSearch,gbc);
    }
}
