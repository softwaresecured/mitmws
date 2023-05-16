package com.mitmws.mvc.view.panels.manualtester;

import javax.swing.*;
import java.awt.*;

public class PnlTestStepEditorToolbar extends JPanel {
    public JComboBox jcmbStepType = new JComboBox(new String[] { "FRAME", "IOWAIT" });
    public JSpinner jspnDelay = new JSpinner(new SpinnerNumberModel(1000,0,600*1000,500));
    public PnlTestStepEditorToolbar() {
        initLayout();
    }
    public void initLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(new JLabel("Step type"),gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jcmbStepType,gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(new JLabel("Delay"),gbc);

        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 3;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jspnDelay,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.gridx = 4;
        gbc.weightx = 1;
        add(new JPanel(),gbc);
    }
}
