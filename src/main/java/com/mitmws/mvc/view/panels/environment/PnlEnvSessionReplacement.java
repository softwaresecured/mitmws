package com.mitmws.mvc.view.panels.environment;

import javax.swing.*;
import java.awt.*;

public class PnlEnvSessionReplacement extends JPanel {
    public JSpinner jspnInputMatchGroup = new JSpinner(new SpinnerNumberModel(1,1,1000,1));
    public JSpinner jspnOutputMatchGroup = new JSpinner(new SpinnerNumberModel(1,1,1000,1));
    public JTextField jtxtRegexInput = new JTextField();
    public JTextField jtxtRegexOutput = new JTextField();

    public PnlEnvSessionReplacement() {
        initLayout();
    }

    void initLayout() {
        jtxtRegexInput.setPreferredSize(new Dimension(100,(int)26));
        jtxtRegexOutput.setPreferredSize(new Dimension(100,(int)26));
        jspnInputMatchGroup.setPreferredSize(new Dimension(50,(int)jtxtRegexOutput.getPreferredSize().getHeight()));
        jspnOutputMatchGroup.setPreferredSize(new Dimension(50,(int)jtxtRegexOutput.getPreferredSize().getHeight()));

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Input regex"),gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        add(jtxtRegexInput,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        add(new JLabel("Match group"),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        add(jspnInputMatchGroup,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        add(new JLabel("Output regex"),gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.weightx = 1;
        add(jtxtRegexOutput,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 0;
        add(new JLabel("Match group"),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 0;
        add(jspnOutputMatchGroup,gbc);
    }
}
