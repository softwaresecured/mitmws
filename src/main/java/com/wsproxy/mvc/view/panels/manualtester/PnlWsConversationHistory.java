package com.wsproxy.mvc.view.panels.manualtester;
import com.wsproxy.mvc.model.ManualTesterModel;
import com.wsproxy.mvc.view.panels.PnlTestLog;
import javax.swing.*;
import java.awt.*;

public class PnlWsConversationHistory extends JPanel {
    public PnlTestLog pnlTestLog;
    public PnlTestHistoryTrafficViewer pnlTestHistoryTrafficViewer;
    private ManualTesterModel manualTesterModel;
    public PnlWsConversationHistory(ManualTesterModel manualTesterModel) {
        this.manualTesterModel = manualTesterModel;
        initLayout();
        initEventListeners();
    }
    public void initEventListeners() {

    }
    public void initLayout() {
        pnlTestHistoryTrafficViewer = new PnlTestHistoryTrafficViewer(manualTesterModel);
        pnlTestLog = new PnlTestLog(manualTesterModel.getTestLogTableModel());
        JSplitPane spltConversationHistory = new JSplitPane(JSplitPane.VERTICAL_SPLIT,pnlTestHistoryTrafficViewer,pnlTestLog);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(spltConversationHistory,gbc);

        int parentHeight = (int)getHeight();
        pnlTestHistoryTrafficViewer.setPreferredSize(new Dimension(getWidth(),(int)parentHeight/2));
        pnlTestLog.setPreferredSize(new Dimension(getWidth(),(int)parentHeight/2));
        spltConversationHistory.setResizeWeight(0.5);
    }
}
