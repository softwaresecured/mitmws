package com.wsproxy.mvc.view.panels.trafficpanel;
import com.wsproxy.mvc.model.TrafficModel;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.EventObject;

public class PnlHttpTrafficViewer extends JPanel {
    public JTable tblHttpTraffic;
    private JScrollPane scrollHttpTraffic;
    public PnlHttpTrafficToolbar pnlHttpTrafficToolbar;
    private TrafficModel trafficModel;
    public PnlHttpTrafficViewer(TrafficModel trafficModel) {
        this.trafficModel = trafficModel;
        initLayout();
    }
    public void initLayout() {
        //setBorder(BorderFactory.createTitledBorder("HTTP traffic"));
        tblHttpTraffic = new JTable(trafficModel.getHttpTrafficModel()) {
            public boolean editCellAt(int row, int column, EventObject e) {
                return false;
            }
            @Override
            public Class getColumnClass(int column) {
                if (column == 1) {
                    return Color.class;
                }
                return String.class;
            }
        };
        tblHttpTraffic.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblHttpTraffic.setCellSelectionEnabled(false);
        tblHttpTraffic.setRowSelectionAllowed(true);
        tblHttpTraffic.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
        {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                Color highlightColor = (Color) trafficModel.getHttpTrafficModel().getValueAt(row,1);
                Integer responseCode = (Integer) trafficModel.getHttpTrafficModel().getValueAt(row,5);
                c.setForeground(Color.BLACK);
                if ( !isSelected ) {
                    if(  responseCode != null && responseCode == 101) {
                        c.setBackground(Color.LIGHT_GRAY);
                    }
                    else {
                        if ( highlightColor != null ) {
                            c.setBackground(highlightColor);
                        }
                        else {
                            c.setBackground(Color.WHITE);
                        }
                    }
                }
                else {
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        });
        pnlHttpTrafficToolbar = new PnlHttpTrafficToolbar();

        int[] colWidths = { -1,-1,160,75,75,60};
        for ( int i = 0; i < colWidths.length; i++ ) {
            tblHttpTraffic.getColumnModel().getColumn(i).setMinWidth(colWidths[i]);
            tblHttpTraffic.getColumnModel().getColumn(i).setMaxWidth(colWidths[i]);
            tblHttpTraffic.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }


        scrollHttpTraffic = new JScrollPane(tblHttpTraffic);
        scrollHttpTraffic.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollHttpTraffic.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tblHttpTraffic.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        add(pnlHttpTrafficToolbar,gbc);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(scrollHttpTraffic,gbc);
    }
}
