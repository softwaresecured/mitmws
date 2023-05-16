package com.mitmws.mvc.view.panels.environment;

import javax.swing.*;
import java.awt.*;

public class PnlEnvRegexReplacement extends JPanel {
    public JTextField jtxtRegexReplacementText = new JTextField();
    public JTextField jtxtMatchRegex = new JTextField();
    public JSpinner jspnMatchRegexGroup = new JSpinner(new SpinnerNumberModel(1,1,1000,1));
    public JCheckBox jchkMatchGroup = new JCheckBox("Match group");
    public PnlEnvRegexReplacement() {
        initLayout();
    }

    void initLayout() {
        jtxtRegexReplacementText.setPreferredSize(new Dimension(100,(int)jtxtRegexReplacementText.getPreferredSize().getHeight()));
        jtxtMatchRegex.setPreferredSize(new Dimension(100,(int)jtxtMatchRegex.getPreferredSize().getHeight()));
        jspnMatchRegexGroup.setPreferredSize(new Dimension(50,(int)jspnMatchRegexGroup.getPreferredSize().getHeight()));
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Regex"),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        add(jtxtMatchRegex,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        add(jchkMatchGroup,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        add(jspnMatchRegexGroup,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        add(new JLabel("Replacement text"),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        add(jtxtRegexReplacementText,gbc);

    }
}
