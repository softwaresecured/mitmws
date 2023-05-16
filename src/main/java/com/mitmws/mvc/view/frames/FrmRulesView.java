package com.mitmws.mvc.view.frames;
import com.mitmws.mvc.model.RulesModel;

import javax.swing.*;
import java.awt.*;

public class FrmRulesView extends JFrame {
    public JTable jtblDetectionRules;
    private RulesModel rulesModel;
    public FrmRulesView(RulesModel rulesModel) {
        this.rulesModel = rulesModel;
        initLayout();
    }

    public void initLayout() {
        setTitle("Rules");
        setSize(800,600);

        jtblDetectionRules = new JTable(rulesModel.getDetectionRuleTableModel()) {
            @Override
            public Class getColumnClass(int column) {
                if (column == 0) {
                    return Boolean.class;
                }
                else if (column == 1) {
                    return Integer.class;
                }
                else {
                    return String.class;
                }
            }
        };
        int[] colWidths = { 100,60,100,400};
        for ( int i = 0; i < colWidths.length; i++ ) {
            jtblDetectionRules.getColumnModel().getColumn(i).setMinWidth(colWidths[i]);
            jtblDetectionRules.getColumnModel().getColumn(i).setMaxWidth(colWidths[i]);
            jtblDetectionRules.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }
        jtblDetectionRules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jtblDetectionRules.setCellSelectionEnabled(false);
        jtblDetectionRules.setRowSelectionAllowed(true);


        jtblDetectionRules.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        JScrollPane scrollPane = new JScrollPane(jtblDetectionRules);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(scrollPane, gbc);
    }
}
