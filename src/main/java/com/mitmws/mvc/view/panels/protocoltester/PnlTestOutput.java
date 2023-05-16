package com.mitmws.mvc.view.panels.protocoltester;

import com.mitmws.mvc.model.ProtocolTesterModel;

import javax.swing.*;
import java.awt.*;

public class PnlTestOutput extends JPanel {

    public PnlTestOutputSightGlass pnlTestOutputSightGlass;
    public PnlTestOutputLog pnlTestOutputLog;
    public JSplitPane splt = null;
    private DefaultListModel logListModel;
    private ProtocolTesterModel protocolTesterModel;
    public PnlTestOutput(ProtocolTesterModel protocolTesterModel) {
        this.protocolTesterModel = protocolTesterModel;
        initLayout();
    }
    private void initLayout() {
        pnlTestOutputSightGlass = new PnlTestOutputSightGlass();
        pnlTestOutputLog = new PnlTestOutputLog(protocolTesterModel.getLogListModel(),protocolTesterModel.getTestTrafficTableModel());
        splt = new JSplitPane();
        splt = new JSplitPane(JSplitPane.VERTICAL_SPLIT,pnlTestOutputSightGlass, pnlTestOutputLog);

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
        pnlTestOutputSightGlass.setPreferredSize(new Dimension(getWidth(),height));
        pnlTestOutputLog.setPreferredSize(new Dimension(getWidth(),height));
        splt.setResizeWeight(0.5);
    }
}
