package com.mitmws.mvc.view.panels.environment;

import javax.swing.*;
import java.awt.*;

public class PnlEnvStringReplacement extends JPanel {
    public JTextArea jtxtStringReplacementText = new JTextArea();
    public JTextArea jtxtStringReplacementMatch = new JTextArea();
    public PnlEnvStringReplacement() {
        initLayout();
    }

    void initLayout() {
        jtxtStringReplacementMatch.setLineWrap(true);
        jtxtStringReplacementMatch.setRows(5);
        jtxtStringReplacementText.setLineWrap(true);
        jtxtStringReplacementText.setRows(5);

        JPanel pnlReplacements = new JPanel();
        pnlReplacements.setLayout(new GridBagLayout());
        JScrollPane scrollMatchText = new JScrollPane(jtxtStringReplacementMatch);
        scrollMatchText.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollMatchText.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JScrollPane scrollReplaceText = new JScrollPane(jtxtStringReplacementText);
        scrollReplaceText.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollReplaceText.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Match text"),gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(scrollMatchText,gbc);

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(new JLabel("Replacement text"),gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(scrollReplaceText,gbc);


    }
}
