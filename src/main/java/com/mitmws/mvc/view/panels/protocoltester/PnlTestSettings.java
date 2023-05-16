package com.mitmws.mvc.view.panels.protocoltester;

import com.mitmws.mvc.controller.HttpRequestResponseController;
import com.mitmws.mvc.model.ProtocolTesterModel;
import com.mitmws.mvc.view.panels.PnlHttpRequestResponse;

import javax.swing.*;
import java.awt.*;

public class PnlTestSettings extends JPanel {
    public PnlHttpRequestResponse pnlHttpRequestResponse;
    public HttpRequestResponseController httpRequestResponseController;
    public PnlTestSettingsProperties pnlTestSettingsProperties;
    public PnlRawRequest pnlRawRequest;
    private ProtocolTesterModel protocolTesterModel;
    public JSplitPane splt = null;
    public JTabbedPane tabbedPane = new JTabbedPane();
    public PnlTestSettings(ProtocolTesterModel protocolTesterModel) {
        this.protocolTesterModel = protocolTesterModel;
        initLayout();
    }
    public void initLayout() {
        pnlTestSettingsProperties = new PnlTestSettingsProperties();
        pnlRawRequest = new PnlRawRequest();
        pnlHttpRequestResponse = new PnlHttpRequestResponse(protocolTesterModel.getHttpRequestResponseModel());
        httpRequestResponseController = new HttpRequestResponseController(protocolTesterModel.getHttpRequestResponseModel(),pnlHttpRequestResponse);

        tabbedPane.addTab("Fuzz", null, pnlTestSettingsProperties);
        tabbedPane.addTab("Raw", null, pnlRawRequest);

        splt = new JSplitPane();
        splt = new JSplitPane(JSplitPane.VERTICAL_SPLIT,pnlHttpRequestResponse, tabbedPane);

        // Main panel layout
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(splt, gbc);

        int height = (int)getHeight()/2;
        pnlHttpRequestResponse.setPreferredSize(new Dimension(getWidth(),height));
        pnlTestSettingsProperties.setPreferredSize(new Dimension(getWidth(),height));
        splt.setResizeWeight(0.5);
    }
}
