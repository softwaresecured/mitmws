package com.mitmws.mvc.view.panels.automatedtester;

import com.mitmws.mvc.model.AutomatedTesterModel;

import javax.swing.*;
import java.awt.*;

public class PnlAutomatedTesterSettings extends JPanel {
    public PnlAutomatedTesterToolbar pnlAutomatedTesterToolbar;
    public PnlAutomatedTesterTargets pnlAutomatedTesterTargets;
    private AutomatedTesterModel automatedTesterModel;
    public PnlAutomatedTesterSettings(AutomatedTesterModel automatedTesterModel) {
        this.automatedTesterModel = automatedTesterModel;
        initLayout();
    }

    public void resetUi() {
        pnlAutomatedTesterToolbar.resetUi();
        pnlAutomatedTesterTargets.resetUi();
    }

    public void initLayout() {
        pnlAutomatedTesterToolbar = new PnlAutomatedTesterToolbar();
        pnlAutomatedTesterTargets = new PnlAutomatedTesterTargets(automatedTesterModel);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(pnlAutomatedTesterToolbar,gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(pnlAutomatedTesterTargets,gbc);
    }
}

