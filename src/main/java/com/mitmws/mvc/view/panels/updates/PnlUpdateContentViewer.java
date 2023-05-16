package com.mitmws.mvc.view.panels.updates;

import javax.swing.*;
import java.awt.*;

public class PnlUpdateContentViewer extends JPanel {
    public JTextArea jtxtContent = new JTextArea();
    public PnlUpdateContentViewer() {
        initLayout();
    }
    public void initLayout() {
        jtxtContent.setEditable(false);
        JScrollPane scroll = new JScrollPane(jtxtContent);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(scroll,gbc);
    }
}
