package com.wsproxy.mvc.view.frames;

import com.wsproxy.mvc.controller.HttpRequestController;
import com.wsproxy.mvc.controller.HttpRequestResponseController;
import com.wsproxy.mvc.model.HttpRequestResponseModel;
import com.wsproxy.mvc.model.HttpRequestTesterModel;
import com.wsproxy.mvc.view.panels.PnlHttpRequestResponse;
import com.wsproxy.util.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class FrmHttpRequestTester extends JFrame {
    public FrmHttpRequestTester() {
        initLayout();
    }
    private HttpRequestResponseModel httpRequestResponseModel = new HttpRequestResponseModel();
    private HttpRequestResponseController httpRequestResponseController;
    public PnlHttpRequestResponse pnlHttpRequestResponse;
    public JPanel pnlErrors;
    public JTextArea jtxtErrors = new JTextArea();
    public void initLayout() {
        jtxtErrors.setLineWrap(true);
        jtxtErrors.setRows(5);
        pnlErrors = GuiUtils.frameWrapComponent(GuiUtils.scrollPaneWrap(jtxtErrors,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS),"Errors");
        pnlErrors.setVisible(false);
        pnlHttpRequestResponse = new PnlHttpRequestResponse(httpRequestResponseModel);
        httpRequestResponseController = new HttpRequestResponseController(httpRequestResponseModel,pnlHttpRequestResponse);
        pnlHttpRequestResponse.setTestMode(false);
        setTitle("Http Request tester");
        setSize(800,600);
        pnlHttpRequestResponse.jbtnRunTest.setText("Send");

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(pnlHttpRequestResponse,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(pnlErrors,gbc);

    }

    public void initEventListeners() {

    }

    public HttpRequestResponseModel getHttpRequestResponseModel() {
        return httpRequestResponseModel;
    }

    public HttpRequestResponseController getHttpRequestResponseController() {
        return httpRequestResponseController;
    }
}
