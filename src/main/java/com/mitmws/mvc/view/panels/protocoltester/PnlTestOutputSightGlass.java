package com.mitmws.mvc.view.panels.protocoltester;

import javax.swing.*;
import java.awt.*;

public class PnlTestOutputSightGlass extends JPanel {
    public JTextArea jtxtHttp = new JTextArea();
    public JTextArea jtxtLastTx = new JTextArea();
    public JTextArea jtxtLastRx = new JTextArea();
    private JLabel jlblHttpStatus = new JLabel("Last upgrade");
    private JLabel jlblWsTxStatus = new JLabel("Last websocket TX");
    private JLabel jlblWsRxStatus = new JLabel("Last websocket RX");
    public PnlTestOutputSightGlass() {
        initLayout();
    }

    public void initLayout() {
        jtxtHttp.setLineWrap(true);
        jtxtLastTx.setLineWrap(true);
        jtxtLastRx.setLineWrap(true);
        JScrollPane scrollHttp = new JScrollPane(jtxtHttp);
        scrollHttp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollHttp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JScrollPane scrollWsTx = new JScrollPane(jtxtLastTx);
        scrollWsTx.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollWsTx.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JScrollPane scrollWsRx = new JScrollPane(jtxtLastRx);
        scrollWsRx.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollWsRx.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0;
        gbc.weightx = 1;
        add(jlblHttpStatus, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(scrollHttp, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.weightx = 1;
        add(jlblWsTxStatus, gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(scrollWsTx, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weighty = 0;
        gbc.weightx = 1;
        add(jlblWsRxStatus, gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(scrollWsRx, gbc);

    }
}
