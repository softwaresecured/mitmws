package com.mitmws.mvc.view.panels;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.EventObject;

public class PnlTestLog extends JPanel {
    public JTable tblTestLog = null;
    private DefaultTableModel logModel;
    public PnlTestLog(DefaultTableModel logModel) {
        this.logModel = logModel;
        initLayout();
    }

    public void initLayout() {
        tblTestLog = new JTable(logModel) {
            public boolean editCellAt(int row, int column, EventObject e) {
                return false;
            }
        };
        tblTestLog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] wsConvoColWidths = { 160,100,140};
        for ( int i = 0; i < wsConvoColWidths.length; i++ ) {
            tblTestLog.getColumnModel().getColumn(i).setMinWidth(wsConvoColWidths[i]);
            tblTestLog.getColumnModel().getColumn(i).setMaxWidth(wsConvoColWidths[i]);
            tblTestLog.getColumnModel().getColumn(i).setPreferredWidth(wsConvoColWidths[i]);
        }
        tblTestLog.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
        {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String level = (String)table.getValueAt(row,1);
                c.setForeground(Color.BLACK);
                if ( !isSelected ) {
                    c.setBackground(Color.WHITE);
                    if ( level.equals("ERROR")) {
                        c.setBackground(Color.PINK);
                    }
                    else if ( level.equals("WARN")) {
                        c.setBackground(Color.YELLOW);
                    }
                }
                else {
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        });

        JScrollPane scrollTestLog = new JScrollPane(tblTestLog);
        scrollTestLog.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollTestLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(scrollTestLog,gbc);
    }
}
