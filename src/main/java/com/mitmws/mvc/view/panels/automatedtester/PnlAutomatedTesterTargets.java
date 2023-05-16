package com.mitmws.mvc.view.panels.automatedtester;
import com.mitmws.util.GuiUtils;
import com.mitmws.mvc.model.AutomatedTesterModel;
import com.mitmws.mvc.view.panels.PnlPayloadEncodings;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.EventObject;

public class PnlAutomatedTesterTargets extends JPanel {
    public JTable jtblConversation = null;
    public PnlAutomatedTesterTargetsToolbar pnlAutomatedTesterTargetsToolbar = new PnlAutomatedTesterTargetsToolbar();
    public JTextArea jtxtTargetSubject = new JTextArea();
    public JEditorPane jtxtTargetSubjectHex = new JEditorPane();
    public JTextArea jtxtTargetSubjectHexStr = new JTextArea();
    public JTable jtblTargets = null;
    public PnlPayloadEncodings pnlPayloadEncodings = new PnlPayloadEncodings();
    public PnlAutomatedTestConfig pnlAutomatedTestConfig;
    public JScrollPane scrollConversation = null;
    public JScrollPane scrollTargets = null;
    public JScrollPane scrollTargetSubject = null;
    public JScrollPane scrollTargetSubjectHex = null;
    public JScrollPane scrollTargetSubjectHexStr = null;
    public JPanel pnlTextViewer = new JPanel();
    public JPanel pnlBinaryViewer = new JPanel();
    public JPanel pnlViewer = new JPanel();

    private AutomatedTesterModel automatedTesterModel;
    public PnlAutomatedTesterTargets(AutomatedTesterModel automatedTesterModel) {
        this.automatedTesterModel = automatedTesterModel;
        pnlAutomatedTestConfig = new PnlAutomatedTestConfig(automatedTesterModel);
        initLayout();
    }

    public void resetUi() {
        GuiUtils.clearTable(jtblTargets);
        GuiUtils.clearTable(jtblConversation);
        jtxtTargetSubject.setText("");
        pnlPayloadEncodings.resetUi();
        pnlAutomatedTestConfig.resetUI();
        this.automatedTesterModel = automatedTesterModel;
    }

    public void initLayout() {

        jtxtTargetSubjectHex.setContentType("text/html");
        jtxtTargetSubject.setEditable(false);
        jtxtTargetSubjectHex.setEditable(false);
        jtxtTargetSubjectHexStr.setEditable(false);
        jtxtTargetSubject.getCaret().setVisible(true);
        jtxtTargetSubject.setRows(5);
        jtxtTargetSubjectHexStr.setRows(5);
        jtxtTargetSubjectHexStr.setLineWrap(true);

        jtblConversation = new JTable(automatedTesterModel.getConversationTableModel());

        jtblConversation.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        jtblConversation.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jtblConversation.setCellSelectionEnabled(false);
        jtblConversation.setRowSelectionAllowed(true);

        jtblTargets = new JTable(automatedTesterModel.getTargetsTableModel()) {
            public boolean isCellEditable(int row, int column, EventObject e) {
                return column == 2;
            }

            @Override
            public Class getColumnClass(int column) {
                if (column == 2) {
                    return Boolean.class;
                }
                return String.class;
            }
        };

        jtblTargets.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        jtblTargets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jtblTargets.setCellSelectionEnabled(false);
        jtblTargets.setRowSelectionAllowed(true);


        jtblConversation.getColumnModel().getColumn(0).setMinWidth(140);
        jtblConversation.getColumnModel().getColumn(0).setMaxWidth(140);
        jtblConversation.getColumnModel().getColumn(0).setPreferredWidth(140);

        jtblTargets.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
        {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(Color.BLACK);
                if ( !isSelected ) {
                    Color color = (Color)table.getValueAt(row,1);
                    c.setBackground(color);
                    c.setForeground(Color.BLACK);
                }
                else {
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        });



        int[] colWidths = { -1,-1,100,140,50,50,50};
        for ( int i = 0; i < colWidths.length; i++ ) {
            jtblTargets.getColumnModel().getColumn(i).setMinWidth(colWidths[i]);
            jtblTargets.getColumnModel().getColumn(i).setMaxWidth(colWidths[i]);
            jtblTargets.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }


        scrollConversation = new JScrollPane(jtblConversation);
        scrollConversation.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollConversation.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        scrollTargets = new JScrollPane(jtblTargets);
        scrollTargets.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTargets.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        scrollTargetSubject = new JScrollPane(jtxtTargetSubject);
        scrollTargetSubject.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTargetSubject.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        scrollTargetSubjectHex = new JScrollPane(jtxtTargetSubjectHex);
        scrollTargetSubjectHex.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTargetSubjectHex.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollTargetSubjectHexStr = new JScrollPane(jtxtTargetSubjectHexStr);
        scrollTargetSubjectHexStr.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTargetSubjectHexStr.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        scrollTargetSubjectHex.setPreferredSize(new Dimension(16*18,scrollTargetSubjectHex.getHeight()));

        pnlTextViewer.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        pnlTextViewer.add(scrollTargetSubject,gbc);

        pnlBinaryViewer.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        pnlBinaryViewer.add(scrollTargetSubjectHexStr,gbc);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0;
        gbc.weighty = 1;
        gbc.gridx = 1;
        gbc.gridy = 0;
        pnlBinaryViewer.add(scrollTargetSubjectHex,gbc);


        pnlViewer.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        pnlViewer.add(pnlTextViewer,gbc);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        pnlViewer.add(pnlBinaryViewer,gbc);

        JSplitPane spltEditor = new JSplitPane(JSplitPane.VERTICAL_SPLIT,pnlViewer, scrollTargets);

        JPanel pnlEditorLayout = new JPanel();
        pnlEditorLayout.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlEditorLayout.add(pnlAutomatedTesterTargetsToolbar,gbc);
        gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 0;
        pnlEditorLayout.add(pnlPayloadEncodings,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        pnlEditorLayout.add(spltEditor,gbc);

        JSplitPane spltEditorMain = new JSplitPane();
        spltEditorMain = new JSplitPane(JSplitPane.VERTICAL_SPLIT,scrollConversation, pnlEditorLayout);
        // Main layout
        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(spltEditorMain,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(pnlAutomatedTestConfig,gbc);

    }
}
