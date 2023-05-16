package com.mitmws.mvc.view.panels.payloads;

import javax.swing.*;
import java.awt.*;

public class PnlAutomatedTesterPayloadsToolbar extends JPanel {
    public JLabel jlblEnabledPayloadCount = new JLabel();
    public JButton jbtnReload = new JButton("Reload");
    public JButton jbtnSave = new JButton("Save");
    public PnlAutomatedTesterPayloadsToolbar() {
        initLayout();
    }

    public void initLayout() {
        //jbtnReload.setPreferredSize(new Dimension((int)jbtnReload.getPreferredSize().getWidth(),(int)new JTextField().getPreferredSize().getHeight()));
        //jbtnSave.setPreferredSize(new Dimension((int)jbtnSave.getPreferredSize().getWidth(),(int)new JTextField().getPreferredSize().getHeight()));
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add( new JPanel(),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jlblEnabledPayloadCount,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jbtnSave,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jbtnReload,gbc);
    }
}
