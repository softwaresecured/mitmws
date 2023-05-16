package com.mitmws.mvc.view.panels.manualtester;

import javax.swing.*;
import java.awt.*;

public class PnlWsConversationToolbar extends JPanel {
    public JButton jbtnClear;
    public JButton jbtnUp;
    public JButton jbtnDown;
    public JButton jbtnDelete;
    public JButton jbtnNew;
    public JButton jbtnDuplicate;
    public JButton jbtnAutomate;
    public PnlWsConversationToolbar() {
        initLayout();
    }
    public void initLayout() {
        jbtnNew = new JButton("New");
        jbtnDuplicate = new JButton("Duplicate");
        jbtnUp = new JButton("Up");
        jbtnDown = new JButton("Down");
        jbtnDelete = new JButton("Delete");
        jbtnClear = new JButton("Clear");
        jbtnAutomate = new JButton("Automate");

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jbtnNew,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jbtnDuplicate,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jbtnUp,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jbtnDown,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jbtnDelete,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(new JPanel(),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jbtnAutomate,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jbtnClear,gbc);

    }
}
