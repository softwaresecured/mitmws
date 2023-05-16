package com.mitmws.mvc.view.panels.mainform;
import com.mitmws.version.VERSION;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public class PnlMainStatusBarView extends JPanel {
    public JLabel lblTrafficStatus = new JLabel();
    public JLabel lblProxyStatus = new JLabel();
    public JLabel lblHttpServerStatus = new JLabel();
    public JLabel lblStatusMsg = new JLabel();
    public JLabel lblVersion = new JLabel(String.format("Version %s", VERSION.getVersionStr(),SwingConstants.RIGHT));
    public PnlMainStatusBarView() {
        initLayout();
    }

    public void initLayout() {
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(lblProxyStatus,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(lblHttpServerStatus,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        add(lblTrafficStatus,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        add(lblStatusMsg,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 4;
        gbc.gridy = 0;
        add(new JPanel(),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        add(lblVersion,gbc);
    }
}
