package com.mitmws.mvc.view.frames;

import javax.swing.*;
import java.awt.*;

public class FrmScriptConsole extends JFrame {
    private JSplitPane jsplt;
    public JTextArea jtxtScriptContent = new JTextArea("");
    public JTextArea jtxtScriptOutput = new JTextArea("");
    public JButton btnExecute = new JButton("Execute");
    public FrmScriptConsole() {
        initLayout();
    }
    public void initLayout() {
        setTitle("Script Console");
        setSize(800,600);
        jtxtScriptOutput.setEditable(false);
        JScrollPane scrollContent = new JScrollPane(jtxtScriptContent);
        JScrollPane scrollOutput = new JScrollPane(jtxtScriptOutput);
        scrollContent.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollContent.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollOutput.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollOutput.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);


        JPanel pnlExec = new JPanel();
        pnlExec.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        pnlExec.add(new JPanel(),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        pnlExec.add(btnExecute,gbc);



        JPanel pnlTop  = new JPanel();
        pnlTop.setBorder(BorderFactory.createTitledBorder("Script content"));
        pnlTop.setLayout(new GridBagLayout());
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        pnlTop.add(scrollContent,gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlTop.add(pnlExec,gbc);


        JPanel pnlBottom = new JPanel();
        pnlBottom.setLayout(new GridBagLayout());
        pnlBottom.setBorder(BorderFactory.createTitledBorder("Script output"));
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        pnlBottom.add(scrollOutput,gbc);
        jsplt = new JSplitPane(JSplitPane.VERTICAL_SPLIT,pnlTop, pnlBottom);

        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add( jsplt, gbc);

        int parentHeight = (int)getHeight()/2;
        pnlTop.setPreferredSize(new Dimension(getWidth(),parentHeight));
        pnlBottom.setPreferredSize(new Dimension(getWidth(),parentHeight));
        jsplt.setResizeWeight(0.50);

    }

    public void initEventListeners() {

    }
}
