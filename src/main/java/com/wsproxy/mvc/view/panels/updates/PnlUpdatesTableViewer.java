package com.wsproxy.mvc.view.panels.updates;

import com.wsproxy.mvc.model.UpdatesModel;

import javax.swing.*;
import java.awt.*;

public class PnlUpdatesTableViewer extends JPanel {
    public PnlUpdatesToolbar pnlUpdatesToolbar = new PnlUpdatesToolbar();
    public JTable jtblUpdates;
    private UpdatesModel updatesModel;
    public PnlUpdatesTableViewer( UpdatesModel updatesModel ) {
        this.updatesModel = updatesModel;
        initLayout();
    }
    public void initLayout() {
        jtblUpdates = new JTable(updatesModel.getUpdatesTableModel());
        int[] colWidths = { -1,80,100 };
        for ( int i = 0; i < colWidths.length; i++ ) {
            jtblUpdates.getColumnModel().getColumn(i).setMinWidth(colWidths[i]);
            jtblUpdates.getColumnModel().getColumn(i).setMaxWidth(colWidths[i]);
            jtblUpdates.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }
        jtblUpdates.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        jtblUpdates.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jtblUpdates.setCellSelectionEnabled(false);
        jtblUpdates.setRowSelectionAllowed(true);
        JScrollPane scrollUpdates = new JScrollPane(jtblUpdates);
        scrollUpdates.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollUpdates.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weighty = 0;
        gbc.weightx = 1;
        add(pnlUpdatesToolbar,gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(scrollUpdates,gbc);
    }
}
