package com.mitmws.mvc.view.frames;
import com.mitmws.mvc.model.PayloadsModel;
import com.mitmws.mvc.view.panels.payloads.PnlAutomatedTesterPayloadsToolbar;

import javax.swing.*;
import java.awt.*;
import java.util.EventObject;

public class FrmPayloadsView extends JFrame {
    public PnlAutomatedTesterPayloadsToolbar pnlAutomatedTesterPayloadsToolbar = new PnlAutomatedTesterPayloadsToolbar();
    public JSplitPane spltAutomatedTester = null;
    public JTable jtblPayloadLists = null;
    public JTextArea jtxtLayloadList = new JTextArea();
    public PayloadsModel payloadsModel;
    public FrmPayloadsView(PayloadsModel payloadsModel) {
        this.payloadsModel = payloadsModel;
        initLayout();

    }

    public void initLayout() {

        setTitle("Payloads");
        setSize(800,600);

        JScrollPane scrollPayloadList = new JScrollPane(jtxtLayloadList);
        scrollPayloadList.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPayloadList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);


        jtblPayloadLists = new JTable(payloadsModel.getPayloadsTableModel()) {
            public boolean isCellEditable(int row, int column, EventObject e) {
                if ( column == 0) {
                    return true;
                }
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

        int[] colWidths = { 80,140};
        for ( int i = 0; i < colWidths.length; i++ ) {
            jtblPayloadLists.getColumnModel().getColumn(i).setMinWidth(colWidths[i]);
            jtblPayloadLists.getColumnModel().getColumn(i).setMaxWidth(colWidths[i]);
            jtblPayloadLists.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }
        jtblPayloadLists.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPayloadLists = new JScrollPane(jtblPayloadLists);
        scrollPayloadLists.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPayloadLists.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);




        spltAutomatedTester = new JSplitPane(JSplitPane.VERTICAL_SPLIT,scrollPayloadLists, scrollPayloadList);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(pnlAutomatedTesterPayloadsToolbar,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(spltAutomatedTester,gbc);
    }
}
