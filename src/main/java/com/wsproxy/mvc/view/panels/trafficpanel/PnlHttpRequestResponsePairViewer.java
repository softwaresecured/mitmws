package com.wsproxy.mvc.view.panels.trafficpanel;

import javax.swing.*;
import java.awt.*;

public class PnlHttpRequestResponsePairViewer extends JPanel {
    private JTabbedPane httpTrafficTabs;
    public PnlHttpMessage pnlCurHttpRequest;
    public PnlHttpMessage pnlCurHttpresponse;
    public PnlHttpRequestResponsePairViewer() {
        initLayout();
    }
    public void initLayout() {
        //setBorder(BorderFactory.createTitledBorder("Request/response headers"));
        pnlCurHttpRequest = new PnlHttpMessage();
        pnlCurHttpresponse = new PnlHttpMessage();
        httpTrafficTabs = new JTabbedPane();
        httpTrafficTabs.addTab("Request", null, pnlCurHttpRequest);
        httpTrafficTabs.addTab("Response", null, pnlCurHttpresponse);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(httpTrafficTabs,gbc);
    }
}
