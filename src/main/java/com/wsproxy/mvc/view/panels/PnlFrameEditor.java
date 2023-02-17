package com.wsproxy.mvc.view.panels;

import com.wsproxy.mvc.controller.WebsocketFrameController;
import com.wsproxy.mvc.model.WebsocketFrameModel;
import com.wsproxy.mvc.view.panels.trafficpanel.PnlWebsocketFrameView;

import javax.swing.*;
import java.awt.*;

public class PnlFrameEditor extends JPanel {
    public PnlWebsocketFrameView pnlFrameViewer;
    public WebsocketFrameModel websocketFrameModel;
    public WebsocketFrameController websocketFrameController;
    public PnlFrameEditor() {
        initLayout();
    }

    public void initLayout() {
        websocketFrameModel = new WebsocketFrameModel(null,"WebsocketFrameModelEditor");
        websocketFrameModel.setEditable(true);
        pnlFrameViewer = new PnlWebsocketFrameView(websocketFrameModel);
        websocketFrameController = new WebsocketFrameController(websocketFrameModel,pnlFrameViewer);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH ;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(pnlFrameViewer, gbc);
    }
    public WebsocketFrameController getWebsocketFrameController() {
        return websocketFrameController;
    }
}
