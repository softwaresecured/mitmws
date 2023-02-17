package com.wsproxy.mvc.view.panels.trafficpanel;
import com.wsproxy.mvc.model.TrafficModel;
import javax.swing.*;
import java.awt.*;

public class PnlHttpTraffic extends JPanel {
    private JSplitPane spltHttpTraffic;
    public PnlHttpTrafficViewer pnlHttpTrafficViewer;
    public PnlHttpRequestResponsePairViewer pnlHttpRequestResponsePairViewer;
    private TrafficModel trafficModel;
    public PnlHttpTraffic( TrafficModel trafficModel ) {
        this.trafficModel = trafficModel;
        initLayout();
    }
    public void initLayout() {
        pnlHttpRequestResponsePairViewer = new PnlHttpRequestResponsePairViewer();
        pnlHttpTrafficViewer = new PnlHttpTrafficViewer(trafficModel);
        spltHttpTraffic = new JSplitPane();
        spltHttpTraffic = new JSplitPane(JSplitPane.VERTICAL_SPLIT,pnlHttpTrafficViewer, pnlHttpRequestResponsePairViewer);

        setLayout( new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(spltHttpTraffic,gbc);

        int parentHeight = (int)this.getSize().getHeight();
        pnlHttpTrafficViewer.setPreferredSize(new Dimension(getWidth(),(int)parentHeight/2));
        pnlHttpRequestResponsePairViewer.setPreferredSize(new Dimension(getWidth(),(int)parentHeight/2));
        spltHttpTraffic.setResizeWeight(0.5);
    }

}