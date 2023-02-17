package com.wsproxy.mvc.view.panels.immediate;

import com.wsproxy.mvc.controller.HttpRequestResponseController;
import com.wsproxy.mvc.controller.WebsocketFrameController;
import com.wsproxy.mvc.model.HttpRequestResponseModel;
import com.wsproxy.mvc.model.ImmediateModel;
import com.wsproxy.mvc.model.WebsocketFrameModel;
import com.wsproxy.mvc.view.panels.PnlHttpRequestResponse;
import com.wsproxy.mvc.view.panels.trafficpanel.PnlWebsocketFrameView;

import javax.swing.*;
import java.awt.*;

public class PnlImmediateEditor extends JPanel {
    private ImmediateModel immediateModel;
    public PnlImmediateToolbar immediateToolbar;
    private PnlHttpRequestResponse pnlHttpRequestResponse;
    private HttpRequestResponseController httpRequestResponseController;
    public PnlWebsocketFrameView pnlWebsocketFrameView;
    private WebsocketFrameController websocketFrameController;
    public PnlImmediateSendToolbar pnlImmediateSendToolbar;
    private WebsocketFrameModel websocketFrameModel;
    private JSplitPane splt;
    public PnlImmediateEditor(ImmediateModel immediateModel) {
        this.immediateModel = immediateModel;
        initLayout();
    }
    public void initLayout() {
        immediateToolbar = new PnlImmediateToolbar();
        pnlImmediateSendToolbar = new PnlImmediateSendToolbar();
        // http editor
        pnlHttpRequestResponse = new PnlHttpRequestResponse(immediateModel.getRequestResponseModel());
        pnlHttpRequestResponse.jbtnRunTest.setVisible(false);
        httpRequestResponseController = new HttpRequestResponseController(immediateModel.getRequestResponseModel(),pnlHttpRequestResponse);
        // ws editor

        websocketFrameModel = new WebsocketFrameModel(null);
        websocketFrameModel.newFrame();
        websocketFrameModel.setEditable(true);
        pnlWebsocketFrameView = new PnlWebsocketFrameView(websocketFrameModel);
        websocketFrameController = new WebsocketFrameController(websocketFrameModel,pnlWebsocketFrameView);
        /*
            Top panel
         */
        JPanel pnlTop = new JPanel();
        pnlTop.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        pnlTop.add(immediateToolbar,gbc);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.weightx = 1;
        pnlTop.add(pnlHttpRequestResponse,gbc);

        /*
            Bottom panel
         */
        JPanel pnlBottom = new JPanel();
        pnlBottom.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        pnlBottom.add(pnlWebsocketFrameView,gbc);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        pnlBottom.add(pnlImmediateSendToolbar,gbc);

        /*
            Main layout
         */
        splt = new JSplitPane(JSplitPane.VERTICAL_SPLIT,pnlTop,pnlBottom);
        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(splt,gbc);
        int parentHeight = (int)getHeight()/2;
        pnlTop.setPreferredSize(new Dimension(getWidth(),parentHeight));
        pnlBottom.setPreferredSize(new Dimension(getWidth(),parentHeight));
        splt.setResizeWeight(0.50);
    }

    public WebsocketFrameController getWebsocketFrameController() {
        return websocketFrameController;
    }

    public HttpRequestResponseController getHttpRequestResponseController() {
        return httpRequestResponseController;
    }
}
