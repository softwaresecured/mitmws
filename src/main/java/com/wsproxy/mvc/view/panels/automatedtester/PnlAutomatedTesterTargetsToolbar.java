package com.wsproxy.mvc.view.panels.automatedtester;
import javax.swing.*;
import java.awt.*;

public class PnlAutomatedTesterTargetsToolbar extends JPanel {
    public JTextField jtxtTargetName = new JTextField("");
    public JButton jbtnDelete = new JButton("Delete");
    public JButton jbtnNew = new JButton("New");
    public JButton jbtnAutoTarget = new JButton("Auto target");
    public JLabel jlblPayloadTargetStr = new JLabel("");
    public PnlAutomatedTesterTargetsToolbar() {
        initLayout();
    }

    public void resetUi() {
        updateTargetSelectionText(0,0,0,0);
        jtxtTargetName.setText("");
        jbtnDelete.setEnabled(false);
        jbtnNew.setEnabled(false);
        jbtnAutoTarget.setEnabled(false);

    }

    public void initLayout() {

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        jtxtTargetName.setPreferredSize(new Dimension(100, (int) jbtnNew.getPreferredSize().getHeight()));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(new JLabel("Target name"),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jtxtTargetName,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add( jlblPayloadTargetStr,gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 0;
        add( new JPanel(),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jbtnAutoTarget,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jbtnNew,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jbtnDelete,gbc);
        updateTargetSelectionText(0,0,0,0);
    }

    public void updateTargetSelectionText( int pos, int totalSize, int selectionStart, int selectionEnd ) {
        String selectionStr = String.format("%d/%d:%d-%d", pos,totalSize,selectionStart,selectionEnd);
        jlblPayloadTargetStr.setText(selectionStr);
    }
}
