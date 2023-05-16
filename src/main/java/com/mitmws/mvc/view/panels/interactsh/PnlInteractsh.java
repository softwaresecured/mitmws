package com.mitmws.mvc.view.panels.interactsh;

import com.mitmws.mvc.model.InteractshModel;

import javax.swing.*;
import java.awt.*;

public class PnlInteractsh extends JPanel {
    private InteractshModel interactshModel;
    public JTable jtblInteractions;
    public JTextArea jtxtRequest = new JTextArea();
    public PnlInteractsh ( InteractshModel interactshModel ) {
        this.interactshModel = interactshModel;
        initLayout();
    }

    public void initLayout() {
        jtblInteractions = new JTable(interactshModel.getInteractionsTableModel());
        int[] trcolWidths = { -1,265,250,160,100};
        for ( int i = 0; i < trcolWidths.length; i++ ) {
            jtblInteractions.getColumnModel().getColumn(i).setMinWidth(trcolWidths[i]);
            jtblInteractions.getColumnModel().getColumn(i).setMaxWidth(trcolWidths[i]);
            jtblInteractions.getColumnModel().getColumn(i).setPreferredWidth(trcolWidths[i]);
        }
        jtblInteractions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollTable = new JScrollPane(jtblInteractions);
        scrollTable.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTable.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JScrollPane scrollRequest = new JScrollPane(jtxtRequest);
        scrollRequest.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollRequest.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(scrollTable, gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(scrollRequest, gbc);

    }
}
