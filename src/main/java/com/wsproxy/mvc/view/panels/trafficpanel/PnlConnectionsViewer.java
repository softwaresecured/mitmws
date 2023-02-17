package com.wsproxy.mvc.view.panels.trafficpanel;

import com.wsproxy.mvc.model.TrafficModel;

import javax.swing.*;
import java.awt.*;

public class PnlConnectionsViewer extends JPanel {
    private JScrollPane scrollWebsocketConnections;
    public JTable tblWebsocketConnections;
    private TrafficModel trafficModel;
    public PnlConnectionsViewer(TrafficModel trafficModel) {
        this.trafficModel = trafficModel;
        initLayout();
    }
    public void initLayout() {
        //setBorder(BorderFactory.createTitledBorder("Websocket connections"));
        tblWebsocketConnections = new JTable(trafficModel.getWebsocketConnectionsModel());
        int[] colWidths = { -1,140,60};
        for ( int i = 0; i < colWidths.length; i++ ) {
            tblWebsocketConnections.getColumnModel().getColumn(i).setMinWidth(colWidths[i]);
            tblWebsocketConnections.getColumnModel().getColumn(i).setMaxWidth(colWidths[i]);
            tblWebsocketConnections.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }
        tblWebsocketConnections.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        tblWebsocketConnections.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblWebsocketConnections.setCellSelectionEnabled(false);
        tblWebsocketConnections.setRowSelectionAllowed(true);
        scrollWebsocketConnections = new JScrollPane(tblWebsocketConnections);
        scrollWebsocketConnections.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollWebsocketConnections.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        setLayout( new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(scrollWebsocketConnections,gbc);
    }
}
