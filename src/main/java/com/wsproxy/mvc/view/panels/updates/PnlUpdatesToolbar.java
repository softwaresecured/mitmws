package com.wsproxy.mvc.view.panels.updates;

import javax.swing.*;
import java.awt.*;

public class PnlUpdatesToolbar extends JPanel {
    public JButton btnCheckUpdates = new JButton("Check");
    public JButton btnInstallUpdates = new JButton("Install");
    public JLabel lblUpdateServerUrl = new JLabel();
    public PnlUpdatesToolbar() {
        initLayout();
    }
    public void initLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(lblUpdateServerUrl,gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        add(new JPanel(),gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        add(btnCheckUpdates,gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        add(btnInstallUpdates,gbc);


    }
}
