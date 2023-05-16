package com.mitmws.mvc.view.panels.environment;
import com.mitmws.mvc.model.EnvironmentModel;

import javax.swing.*;
import java.awt.*;

public class PnlEnvironmentEditor extends JPanel {
    public PnlEnvironmentItemEditor pnlEnvironmentItemEditor = new PnlEnvironmentItemEditor();
    public PnlEnvironmentItemVariableEditor pnlEnvironmentItemVariableEditor;
    public PnlEnvironmentVariableRegexTester pnlEnvironmentVariableRegexTester;
    private EnvironmentModel environmentModel;
    public PnlEnvironmentEditor(EnvironmentModel environmentModel) {
        this.environmentModel = environmentModel;
        pnlEnvironmentItemVariableEditor = new PnlEnvironmentItemVariableEditor();
        pnlEnvironmentVariableRegexTester = new PnlEnvironmentVariableRegexTester(environmentModel.getEnvVarTestTableModel());
        initLayout();
    }

    public void initLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        add(pnlEnvironmentItemEditor,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 0;
        gbc.weightx = 1;
        add(pnlEnvironmentItemVariableEditor,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(pnlEnvironmentVariableRegexTester,gbc);

    }
}
