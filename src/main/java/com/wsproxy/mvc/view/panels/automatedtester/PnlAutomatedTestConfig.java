package com.wsproxy.mvc.view.panels.automatedtester;

import com.wsproxy.mvc.model.AutomatedTesterModel;

import javax.swing.*;
import java.awt.*;
import java.util.EventObject;

public class PnlAutomatedTestConfig extends JPanel {

    public JCheckBox jchkReuseConnection = new JCheckBox("Reuse connection");
    public JCheckBox jchkContinueReplayAfterTestInsertion = new JCheckBox("Continue replay after test insertion");
    public JCheckBox jchkIncludeWaitAtEnd = new JCheckBox("Include IO wait at end of sequence ( if not present )");
    public JSpinner jspnFuzzSeedStart = new JSpinner(new SpinnerNumberModel(1,1,999999,1));
    public JSpinner jspnFuzzSeedEnd = new JSpinner(new SpinnerNumberModel(1000,1,999999,100));
    public JSpinner jspnRatio = new JSpinner(new SpinnerNumberModel(0.1,0.01,1,0.1));
    private AutomatedTesterModel automatedTesterModel;
    public PnlAutomatedTestConfig(AutomatedTesterModel automatedTesterModel) {
        this.automatedTesterModel = automatedTesterModel;
        initLayout();
    }
    public void resetUI() {
        jchkReuseConnection.setSelected(false);
        jchkContinueReplayAfterTestInsertion.setSelected(false);
        jchkIncludeWaitAtEnd.setSelected(false);

    }
    public void initLayout() {

        JPanel pnlFuzzSettings = new JPanel();
        pnlFuzzSettings.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlFuzzSettings.add(new JLabel("Seed start"),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlFuzzSettings.add(jspnFuzzSeedStart,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlFuzzSettings.add(new JLabel("Seed end"),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlFuzzSettings.add(jspnFuzzSeedEnd,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlFuzzSettings.add(new JLabel("Ratio"),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlFuzzSettings.add(jspnRatio,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        pnlFuzzSettings.add(new JPanel(),gbc);
        setLayout(new GridBagLayout());
        jspnFuzzSeedStart.setPreferredSize(new Dimension(100, 22));
        jspnFuzzSeedEnd.setPreferredSize(new Dimension(100,22));
        jspnRatio.setPreferredSize(new Dimension(100,22));



        setBorder(BorderFactory.createTitledBorder("Test configuration"));

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(jchkReuseConnection,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(jchkContinueReplayAfterTestInsertion,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(jchkIncludeWaitAtEnd,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(pnlFuzzSettings,gbc);
    }
}
