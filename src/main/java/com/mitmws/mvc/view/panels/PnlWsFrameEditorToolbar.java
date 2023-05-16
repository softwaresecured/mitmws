package com.mitmws.mvc.view.panels;

import javax.swing.*;
import java.awt.*;

public class PnlWsFrameEditorToolbar extends JPanel {
    public JCheckBox jchkFinFlag = null;
    public JCheckBox jchkRsv1Flag = null;
    public JCheckBox jchkRsv2Flag = null;
    public JCheckBox jchkRsv3Flag = null;
    public JComboBox jcmbOpcode = null;
    public JComboBox jcmbDisplayFormat = null;
    public PnlWsFrameEditorToolbar() {
        initLayout();
    }
    public void initLayout() {
        jchkFinFlag = new JCheckBox("FIN");
        jchkRsv1Flag = new JCheckBox("R1");
        jchkRsv2Flag = new JCheckBox("R2");
        jchkRsv3Flag = new JCheckBox("R3");
        jcmbOpcode = new JComboBox(new String[] { "TEXT","CONTINUATION", "BINARY","PING","PONG","CLOSE" });
        //jcmbOpcode.setPreferredSize(new Dimension((int)jcmbOpcode.getPreferredSize().getWidth(),(int)new JTextField().getPreferredSize().getHeight()));

        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jchkFinFlag,gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jchkRsv1Flag,gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 3;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jchkRsv2Flag,gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 4;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jchkRsv3Flag,gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 5;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(new JLabel("Opcode:"),gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 6;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jcmbOpcode,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.gridx = 7;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(new JPanel(),gbc);
    }
}
