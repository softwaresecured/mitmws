package com.wsproxy.mvc.view.panels.trafficpanel;

import com.wsproxy.mvc.controller.WebsocketFrameController;
import com.wsproxy.mvc.model.TrafficModel;
import com.wsproxy.mvc.model.WebsocketFrameModel;
import com.wsproxy.mvc.view.panels.PnlWebsocketTrafficToolbar;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.EventObject;

public class PnlWebsocketTrafficViewer extends JPanel {
    private JScrollPane scrollWebsocketTraffic;
    public JTable tblWebsocketTraffic;
    public PnlWebsocketTrafficToolbar pnlWebsocketTrafficToolbar;
    public PnlWebsocketFrameView pnlFrameViewer;
    public WebsocketFrameModel websocketFrameModel;
    public WebsocketFrameController websocketFrameController;
    private DefaultTableModel websocketTrafficTableModel;



    public PnlWebsocketTrafficViewer( DefaultTableModel websocketTrafficTableModel ) {
        this.websocketTrafficTableModel = websocketTrafficTableModel;
        initLayout();
    }
    public void initLayout() {
        //setBorder(BorderFactory.createTitledBorder("Websocket traffic"));
        pnlWebsocketTrafficToolbar = new PnlWebsocketTrafficToolbar();
        tblWebsocketTraffic = new JTable(websocketTrafficTableModel) {
            @Override
            public Class getColumnClass(int column) {
                if (column == 2) {
                    return Color.class;
                }
                return String.class;
            }
        };
        tblWebsocketTraffic.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
        {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(Color.BLACK);
                if ( !isSelected ) {
                    if ( websocketTrafficTableModel.getValueAt(row,2) != null ) {
                        Color highlightColour = (Color) websocketTrafficTableModel.getValueAt(row,2);
                        c.setBackground(highlightColour);
                    }
                }
                else {
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        });

        int[] colWidths = { -1,-1,-1,160,25,35,35,35,35,35,90,70,70};
        for ( int i = 0; i < colWidths.length; i++ ) {
            tblWebsocketTraffic.getColumnModel().getColumn(i).setMinWidth(colWidths[i]);
            tblWebsocketTraffic.getColumnModel().getColumn(i).setMaxWidth(colWidths[i]);
            tblWebsocketTraffic.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }
        tblWebsocketTraffic.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        websocketFrameModel = new WebsocketFrameModel(null);
        pnlFrameViewer = new PnlWebsocketFrameView(websocketFrameModel);
        websocketFrameController = new WebsocketFrameController(websocketFrameModel,pnlFrameViewer);
        scrollWebsocketTraffic = new JScrollPane(tblWebsocketTraffic);
        scrollWebsocketTraffic.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollWebsocketTraffic.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        JSplitPane spltWebsocketTraffic = new JSplitPane(JSplitPane.VERTICAL_SPLIT,scrollWebsocketTraffic,pnlFrameViewer);

        setLayout( new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(pnlWebsocketTrafficToolbar,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(spltWebsocketTraffic,gbc);

        int parentHeight = (int)getHeight()/2;
        scrollWebsocketTraffic.setPreferredSize(new Dimension(getWidth(),parentHeight));
        pnlFrameViewer.setPreferredSize(new Dimension(getWidth(),parentHeight));
        spltWebsocketTraffic.setResizeWeight(0.80);
    }
    public WebsocketFrameController getWebsocketFrameController() {
        return websocketFrameController;
    }
}
