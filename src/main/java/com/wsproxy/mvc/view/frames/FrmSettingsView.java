package com.wsproxy.mvc.view.frames;
import com.wsproxy.mvc.model.SettingsModel;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;

public class FrmSettingsView extends JFrame {

    private final String DEFAULT_HTTP_TEST_URL = "https://example.com";

    public JButton btnTest = new JButton("Test");
    public JButton btnApply = new JButton("Apply");
    public JButton btnDefaults = new JButton("Defaults");
    public JTable tblSettings;
    public SettingsModel settingsModel;

    public FrmSettingsView(SettingsModel settingsModel) {
        this.settingsModel = settingsModel;
        initLayout();
    }

    public void initLayout() {
        setTitle("Settings");
        setSize(800,600);
        tblSettings = new JTable(settingsModel.getSettingsTableModel()) {
            public boolean isCellEditable(int row, int column)
            {
                return column == 2;
            }


        };
        tblSettings.setDefaultRenderer(String.class, new DefaultTableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column) {
                Component c = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
                c.setForeground(Color.BLACK);
                if ( column == 2 ) {
                    if ( !(boolean)table.getValueAt(row,0) ) {
                        c.setForeground(Color.RED);
                    }
                }
                return c;
            }
        });
        int[] trcolWidths = { -1,250, 300};
        for ( int i = 0; i < trcolWidths.length; i++ ) {
            tblSettings.getColumnModel().getColumn(i).setMinWidth(trcolWidths[i]);
            tblSettings.getColumnModel().getColumn(i).setMaxWidth(trcolWidths[i]);
            tblSettings.getColumnModel().getColumn(i).setPreferredWidth(trcolWidths[i]);
        }
        tblSettings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollSettings = new JScrollPane(tblSettings);
        scrollSettings.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollSettings.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(settingsModel.getSettingsTableModel());
        tblSettings.setRowSorter(sorter);

        ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);

        JPanel pnlToolbar = new JPanel();
        pnlToolbar.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        pnlToolbar.add(new JPanel(),gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        pnlToolbar.add(btnTest,gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        pnlToolbar.add(btnDefaults,gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        pnlToolbar.add(btnApply,gbc);
        pnlToolbar.setBorder(BorderFactory.createTitledBorder("Actions"));

        gbc = new GridBagConstraints();
        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(scrollSettings,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(pnlToolbar,gbc);


    }
}
