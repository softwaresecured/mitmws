package com.wsproxy.mvc.view.panels.trafficpanel;
import com.wsproxy.mvc.model.BreakpointModel;
import com.wsproxy.mvc.model.MainModel;
import com.wsproxy.mvc.model.TrafficModel;

import javax.swing.*;
import java.awt.*;

public class PnlTrafficView extends JPanel {
    public PnlWebsocketTraffic pnlWebsocketTraffic = null;
    public PnlHttpTraffic pnlHttpTraffic = null;
    public JSplitPane spltTraffic;
    private TrafficModel trafficModel;
    private MainModel mainModel;

    public PnlTrafficView(MainModel mainModel) {
        this.mainModel = mainModel;
        this.trafficModel = mainModel.getTrafficModel();
        initLayout();
    }

    public void initLayout() {

        pnlWebsocketTraffic = new PnlWebsocketTraffic(mainModel);
        pnlHttpTraffic = new PnlHttpTraffic(trafficModel);
        spltTraffic = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,pnlHttpTraffic, pnlWebsocketTraffic);



        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(spltTraffic,gbc);

        int parentWidth = (int)this.getSize().getWidth();
        pnlWebsocketTraffic.setPreferredSize(new Dimension(parentWidth/2,(int)this.getPreferredSize().getHeight()));
        pnlHttpTraffic.setPreferredSize(new Dimension(parentWidth/2,(int)this.getPreferredSize().getHeight()));
        spltTraffic.setResizeWeight(0.5);
    }
}
