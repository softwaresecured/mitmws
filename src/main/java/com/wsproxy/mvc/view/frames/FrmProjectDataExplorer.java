package com.wsproxy.mvc.view.frames;

import com.wsproxy.mvc.model.ProjectDataExplorerModel;
import com.wsproxy.util.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class FrmProjectDataExplorer extends JFrame {
    public JPanel pnlQueryPanel = new JPanel();
    public JPanel pnlTableList = new JPanel(new FlowLayout(FlowLayout.LEFT));
    public JTable tblResults = new JTable();
    public JTextArea jtxtError = new JTextArea();
    public JTextArea jtxtSQL = new JTextArea();
    public JButton btnQuery = new JButton("Query");
    public JPanel pnlError;
    public FrmProjectDataExplorer(ProjectDataExplorerModel projectDataExplorerModel) {
        initLayout();
    }
    public void initLayout() {
        setTitle("Project Data Query");
        setSize(800,600);


        setLayout(new GridBagLayout());

        jtxtSQL.setLineWrap(true);
        jtxtSQL.setRows(5);

        jtxtError.setLineWrap(true);
        jtxtError.setRows(5);

        pnlError = GuiUtils.frameWrapComponent(GuiUtils.scrollPaneWrap(jtxtError,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS),"Errors");

        pnlQueryPanel.setBorder(BorderFactory.createTitledBorder("Query"));

        pnlQueryPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        pnlQueryPanel.add(pnlTableList,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.gridy = 1;
        gbc.weightx = 1;
        pnlQueryPanel.add(GuiUtils.scrollPaneWrap(jtxtSQL,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS),gbc);


        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1;
        pnlQueryPanel.add(new JPanel(),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        pnlQueryPanel.add(btnQuery,gbc);


        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        add(pnlQueryPanel,gbc);


        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        add(pnlError,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(GuiUtils.frameWrapComponent(GuiUtils.scrollPaneWrap(tblResults,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS),"Results"),gbc);
        pnlError.setVisible(false);
    }

    public void initEventListeners() {

    }
}
