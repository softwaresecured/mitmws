package com.mitmws.mvc.view.panels.environment;

import javax.swing.*;
import java.awt.*;

public class PnlEnvironmentItemVariableEditor extends JPanel {


    public PnlEnvStringReplacement pnlStringReplacement = new PnlEnvStringReplacement();
    public PnlEnvRegexReplacement pnlRegexReplacement = new PnlEnvRegexReplacement();
    public PnlEnvSessionReplacement pnlSessionReplacement = new PnlEnvSessionReplacement();
    public PnlEnvScriptReplacement pnlScriptReplacement = new PnlEnvScriptReplacement();

    public JTextArea jtxtValidationIssues = new JTextArea();

    public PnlEnvironmentItemVariableEditor() {
        initLayout();
    }

    public void initLayout() {

        // Main panel
        JScrollPane scrollValidationIssues = new JScrollPane(jtxtValidationIssues);
        scrollValidationIssues.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollValidationIssues.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jtxtValidationIssues.setRows(2);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        add(pnlStringReplacement,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        add(pnlRegexReplacement, gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        add(pnlSessionReplacement,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1;
        add(pnlScriptReplacement,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1;
        add(new JLabel("Validation issues"),gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 1;
        add(scrollValidationIssues,gbc);
    }
}
