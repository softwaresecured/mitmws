package com.wsproxy.mvc.view.panels.manualtester;
import com.wsproxy.mvc.controller.HttpRequestResponseController;
import com.wsproxy.mvc.model.HttpRequestResponseModel;
import com.wsproxy.mvc.model.ManualTesterModel;
import com.wsproxy.mvc.view.panels.PnlHttpRequestResponse;

import javax.swing.*;
import java.awt.*;

public class PnlWsConversationEditor extends JPanel {
    public PnlHttpRequestResponse pnlHttpRequestResponse;
    public HttpRequestResponseModel httpRequestResponseModel;
    public HttpRequestResponseController httpRequestResponseController;
    public PnlConversationBuilder pnlConversationBuilder;
    public JSplitPane spltEditor = null;

    private ManualTesterModel manualTesterModel;
    public PnlWsConversationEditor(ManualTesterModel manualTesterModel) {
        this.manualTesterModel = manualTesterModel;
        initLayout();
        initEventListeners();
        updateButtonStatus();
    }
    public void initEventListeners() {
        pnlConversationBuilder.tblWebsocketConversation.getSelectionModel().addListSelectionListener(listSelectionEvent -> updateButtonStatus());
    }

    public void setButtonEnabled( boolean enabled ) {
        pnlHttpRequestResponse.jbtnRunTest.setEnabled(enabled);
        pnlConversationBuilder.pnlWsConversationToolbar.jbtnAutomate.setEnabled(enabled);
        pnlConversationBuilder.pnlWsConversationToolbar.jbtnClear.setEnabled(enabled);
        pnlConversationBuilder.pnlWsConversationToolbar.jbtnDelete.setEnabled(enabled);
        pnlConversationBuilder.pnlWsConversationToolbar.jbtnDown.setEnabled(enabled);
        pnlConversationBuilder.pnlWsConversationToolbar.jbtnDuplicate.setEnabled(enabled);
        pnlConversationBuilder.pnlWsConversationToolbar.jbtnUp.setEnabled(enabled);
    }

    public void updateButtonStatus() {
        setButtonEnabled(false);
        if ( pnlConversationBuilder.tblWebsocketConversation.getRowCount() > 0 ) {
            setButtonEnabled(true);
        }
    }

    public void initLayout() {
        httpRequestResponseModel = new HttpRequestResponseModel();
        pnlHttpRequestResponse = new PnlHttpRequestResponse(httpRequestResponseModel);
        httpRequestResponseController = new HttpRequestResponseController(httpRequestResponseModel,pnlHttpRequestResponse);
        pnlConversationBuilder = new PnlConversationBuilder(manualTesterModel);

        spltEditor = new JSplitPane();
        spltEditor = new JSplitPane(JSplitPane.VERTICAL_SPLIT,pnlHttpRequestResponse, pnlConversationBuilder);

        // Main panel layout
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(spltEditor, gbc);

        int height = (int)getHeight()/2;
        pnlHttpRequestResponse.setPreferredSize(new Dimension(getWidth(),height));
        pnlConversationBuilder.setPreferredSize(new Dimension(getWidth(),height));
        spltEditor.setResizeWeight(0.5);
    }
}
