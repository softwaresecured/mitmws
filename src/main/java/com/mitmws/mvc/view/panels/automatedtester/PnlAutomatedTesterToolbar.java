package com.mitmws.mvc.view.panels.automatedtester;

import javax.swing.*;
import java.awt.*;

public class PnlAutomatedTesterToolbar extends JPanel {
    public JLabel jlblURL = new JLabel("");
    public JLabel jlblTestSummary = new JLabel();
    public JButton jbtnRun = new JButton("Run");
    public JButton jbtnDelete = new JButton("Delete");
    public JCheckBox jchkDryRun = new JCheckBox("Dry run");
    public JTextField jtxtTestName = new JTextField("UNTITLED");
    public PnlAutomatedTesterToolbar() {
        initLayout();
        jtxtTestName.setFont(new Font(jtxtTestName.getText(), Font.BOLD,12));
        jtxtTestName.setPreferredSize(new Dimension(100,27));
        jtxtTestName.setMinimumSize(new Dimension(100,27));
        jtxtTestName.setMaximumSize(new Dimension(100,27));

    }

    public void resetUi() {
        updateTestSummary(0,0,0);
        jbtnRun.setEnabled(true);
        jbtnDelete.setEnabled(true);
        jchkDryRun.setSelected(false);
        jtxtTestName.setText("");

    }

    public void initLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(new JLabel(),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(new JLabel("Test name"),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jtxtTestName,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jlblTestSummary,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jbtnDelete,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jchkDryRun,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jbtnRun,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(jlblURL,gbc);
        updateTestSummary(0,0,0);
    }



    public void updateTestSummary( int tests, int steps, int etasec ) {
        String summary = String.format("Tests: %d, Steps: %d, ETA: %d", tests, steps, etasec );
        jlblTestSummary.setText(summary);
    }
}
