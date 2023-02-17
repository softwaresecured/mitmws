package com.wsproxy.mvc.view.panels.environment;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.EventObject;

public class PnlEnvironmentVariableRegexTester extends JPanel {
    public JTable tblEnvVarTest = null;
    private DefaultTableModel envVarTestTableModel;
    public PnlEnvironmentVariableRegexTesterToolbar pnlEnvironmentVariableRegexTesterToolbar = new PnlEnvironmentVariableRegexTesterToolbar();
    public PnlEnvironmentVariableRegexTester(DefaultTableModel envVarTestTableModel) {
        this.envVarTestTableModel = envVarTestTableModel;
        initLayout();
    }

    public void initLayout() {
        tblEnvVarTest = new JTable(envVarTestTableModel) {
            public boolean editCellAt(int row, int column, EventObject e) {
                return false;
            }
            @Override
            public Class getColumnClass(int column) {
                if (column == 0) {
                    return Boolean.class;
                }
                return String.class;
            }
        };
        int[] trcolWidths = { 80, 80};
        for ( int i = 0; i < trcolWidths.length; i++ ) {
            tblEnvVarTest.getColumnModel().getColumn(i).setMinWidth(trcolWidths[i]);
            tblEnvVarTest.getColumnModel().getColumn(i).setMaxWidth(trcolWidths[i]);
            tblEnvVarTest.getColumnModel().getColumn(i).setPreferredWidth(trcolWidths[i]);
        }
        tblEnvVarTest.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollTestOutput = new JScrollPane(tblEnvVarTest);
        scrollTestOutput.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTestOutput.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(pnlEnvironmentVariableRegexTesterToolbar,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(scrollTestOutput,gbc);
    }
}
