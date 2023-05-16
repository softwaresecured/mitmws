package com.mitmws.mvc.view.panels.trafficpanel;
import com.mitmws.mvc.controller.BreakpointController;
import com.mitmws.mvc.model.BreakpointModel;
import com.mitmws.mvc.model.InteractshModel;
import com.mitmws.mvc.model.MainModel;
import com.mitmws.mvc.model.TrafficModel;
import com.mitmws.mvc.view.panels.breakpoints.PnlBreakPointsView;

import javax.swing.*;
import java.awt.*;

public class PnlWebsocketTraffic extends JPanel {
    public PnlWebsocketTrafficViewer pnlWebsocketTrafficViewer;
    public PnlConnectionsViewer pnlConnectionsViewer;
    private TrafficModel trafficModel;
    private BreakpointModel breakpointModel;
    public JTabbedPane trafficTabs = new JTabbedPane(JTabbedPane.BOTTOM);
    public PnlBreakPointsView pnlBreakPointsView = null;
    private BreakpointController breakpointController;
    private InteractshModel interactshModel;
    public PnlWebsocketTraffic(MainModel mainModel) {
        this.interactshModel = mainModel.getInteractshModel();
        this.trafficModel = mainModel.getTrafficModel();
        this.breakpointModel = mainModel.getBreakpointModel();
        initLayout();
    }

    public void initLayout() {
        pnlBreakPointsView = new PnlBreakPointsView(breakpointModel);
        breakpointController = new BreakpointController(breakpointModel, interactshModel, pnlBreakPointsView);
        pnlWebsocketTrafficViewer = new PnlWebsocketTrafficViewer(trafficModel.getWebsocketTrafficModel());
        pnlConnectionsViewer = new PnlConnectionsViewer(trafficModel);
        JSplitPane spltWebsockets = new JSplitPane(JSplitPane.VERTICAL_SPLIT,pnlWebsocketTrafficViewer,pnlConnectionsViewer);
        trafficTabs.addTab("Traffic",spltWebsockets);
        trafficTabs.addTab("Breakpoints", pnlBreakPointsView);

        setLayout( new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(trafficTabs,gbc);

        int parentHeight = (int)this.getSize().getHeight();
        pnlWebsocketTrafficViewer.setPreferredSize(new Dimension(getWidth(),(int)parentHeight/2));
        pnlConnectionsViewer.setPreferredSize(new Dimension(getWidth(),(int)parentHeight/2));
        spltWebsockets.setResizeWeight(0.5);
    }
}