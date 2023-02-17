package com.wsproxy.mvc.view.panels;

import com.wsproxy.mvc.model.WebsocketFrameModel;

import javax.swing.*;
import java.awt.*;

public class PnlWebsocketFrameViewerToolbar extends JPanel {
    public JComboBox jcmbDisplayFormat = null;
    public JLabel jlblFrameProperties = null;
    private WebsocketFrameModel websocketFrameModel;
    public PnlWebsocketFrameViewerToolbar(WebsocketFrameModel websocketFrameModel) {
        this.websocketFrameModel = websocketFrameModel;
        initLayout();
    }

    public void resetUi() {
        jcmbDisplayFormat.setSelectedIndex(0);
    }
    public void initLayout() {
        jlblFrameProperties = new JLabel();
        jcmbDisplayFormat = new JComboBox(websocketFrameModel.getDisplayFormats());
        setLayout( new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        add(new JPanel(), gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        add(jlblFrameProperties, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        add(new JLabel("Display format:"), gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        add(jcmbDisplayFormat, gbc);
        gbc = new GridBagConstraints();
    }
}
