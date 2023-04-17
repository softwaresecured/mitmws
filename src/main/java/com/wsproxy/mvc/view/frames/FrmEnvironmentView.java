package com.wsproxy.mvc.view.frames;

import com.wsproxy.environment.Environment;
import com.wsproxy.mvc.model.EnvironmentModel;
import com.wsproxy.mvc.view.panels.environment.PnlEnvironmentEditor;

import javax.swing.*;
import java.awt.*;
import java.util.EventObject;

public class FrmEnvironmentView extends JFrame {
    public PnlEnvironmentEditor pnlEnvironmentEditor;
    public JTable tblEnvironment;
    private EnvironmentModel environmentModel;
    public JSplitPane spltEnv;
    public Environment environment = new Environment();
    public FrmEnvironmentView(EnvironmentModel environmentModel) {
        this.environmentModel = environmentModel;
        initLayout();
    }

    public void initLayout() {
        setTitle("Environment");
        setSize(800,600);
        pnlEnvironmentEditor = new PnlEnvironmentEditor(environmentModel);
        pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnSave.setEnabled(true);
        pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnDelete.setEnabled(true);
        pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnNew.setEnabled(true);
        tblEnvironment = new JTable(environmentModel.getEnvironmentTableModel()) {
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
        int[] trcolWidths = { 80, 250,100};
        for ( int i = 0; i < trcolWidths.length; i++ ) {
            tblEnvironment.getColumnModel().getColumn(i).setMinWidth(trcolWidths[i]);
            tblEnvironment.getColumnModel().getColumn(i).setMaxWidth(trcolWidths[i]);
            tblEnvironment.getColumnModel().getColumn(i).setPreferredWidth(trcolWidths[i]);
        }
        tblEnvironment.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollEnv = new JScrollPane(tblEnvironment);
        scrollEnv.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollEnv.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        spltEnv = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,scrollEnv,pnlEnvironmentEditor);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(spltEnv,gbc);
        pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnSave.setEnabled(false);
        pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnDelete.setEnabled(false);
        spltEnv.setResizeWeight(0.5);

        int parentWidth = (int)this.getSize().getWidth();
        scrollEnv.setPreferredSize(new Dimension(parentWidth/2,(int)this.getPreferredSize().getHeight()));
        pnlEnvironmentEditor.setPreferredSize(new Dimension(parentWidth/2,(int)this.getPreferredSize().getHeight()));
    }
}