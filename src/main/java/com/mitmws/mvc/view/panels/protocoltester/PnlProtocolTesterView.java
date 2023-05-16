package com.mitmws.mvc.view.panels.protocoltester;

import com.mitmws.mvc.model.ProtocolTesterModel;

import javax.swing.*;
import java.awt.*;
public class PnlProtocolTesterView extends JPanel {
    public PnlTestSettings pnlSettings;
    public PnlTestOutput pnlTestOutput;
    public JSplitPane splt = null;
    private ProtocolTesterModel protocolTesterModel;
    public PnlProtocolTesterView(ProtocolTesterModel protocolTesterModel) {
        this.protocolTesterModel = protocolTesterModel;
        initLayout();
    }
    public void initLayout() {
        pnlSettings = new PnlTestSettings(protocolTesterModel);
        pnlTestOutput = new PnlTestOutput(protocolTesterModel);
        splt = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,pnlSettings, pnlTestOutput);
        setLayout( new BoxLayout(this,BoxLayout.Y_AXIS));
        add(splt);

        int parentWidth = (int)this.getSize().getWidth();
        pnlSettings.setPreferredSize(new Dimension(parentWidth/2,(int)this.getPreferredSize().getHeight()));
        pnlTestOutput.setPreferredSize(new Dimension(parentWidth/2,(int)this.getPreferredSize().getHeight()));
        splt.setResizeWeight(0.5);
    }

    public void initEventListeners() {

    }
}
