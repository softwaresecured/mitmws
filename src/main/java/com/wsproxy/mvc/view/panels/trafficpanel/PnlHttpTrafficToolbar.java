package com.wsproxy.mvc.view.panels.trafficpanel;
import javax.swing.*;
import java.awt.*;

public class PnlHttpTrafficToolbar extends JPanel {

    public JButton jbtnApply = null;
    public JTextField jtxtProjectScope = null;
    public JTextField jtxtExlcudeRegex = null;
    public JCheckBox jchkExclude = null;

    public PnlHttpTrafficToolbar() {
        initLayout();
    }

    public void initLayout() {
        jbtnApply = new JButton("Apply");
        jtxtProjectScope = new JTextField(".*");
        jtxtExlcudeRegex = new JTextField();
        jchkExclude = new JCheckBox("Exclude:");
        //jbtnApply.setPreferredSize(new Dimension((int)jbtnApply.getPreferredSize().getWidth(),(int)jtxtExlcudeRegex.getPreferredSize().getHeight()));
        setLayout( new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        add(new JLabel("Scope:"),gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridy = 0;
        gbc.gridx = 1;
        add(jtxtProjectScope,gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 2;
        add(jchkExclude,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridy = 0;
        gbc.gridx = 3;
        add(jtxtExlcudeRegex,gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 4;
        add(jbtnApply,gbc);
    }
}