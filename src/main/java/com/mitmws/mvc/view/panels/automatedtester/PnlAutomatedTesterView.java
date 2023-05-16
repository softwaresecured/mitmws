package com.mitmws.mvc.view.panels.automatedtester;
import com.mitmws.util.GuiUtils;
import com.mitmws.mvc.model.AutomatedTesterModel;

import javax.swing.*;
import java.awt.*;

public class PnlAutomatedTesterView extends JPanel {
    private AutomatedTesterModel automatedTesterModel;
    public PnlAutomatedTesterSettings pnlAutomatedTesterSettings;
    public PnlAutomatedTesterTestOutput pnlAutomatedTesterTestOutput;
    public JSplitPane spltAutomatedTester = null;

    public PnlAutomatedTesterView(AutomatedTesterModel automatedTesterModel) {
        this.automatedTesterModel = automatedTesterModel;
        pnlAutomatedTesterSettings = new PnlAutomatedTesterSettings(automatedTesterModel);
        pnlAutomatedTesterTestOutput = new PnlAutomatedTesterTestOutput(automatedTesterModel);
        initLayout();
        GuiUtils.tableSelectLast(pnlAutomatedTesterTestOutput.jtblTestRuns);
    }

    public void initLayout() {
        spltAutomatedTester = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,pnlAutomatedTesterSettings, pnlAutomatedTesterTestOutput);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(spltAutomatedTester,gbc);
        pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.scrollConversation.setMinimumSize(new Dimension(pnlAutomatedTesterSettings.pnlAutomatedTesterTargets.jtblConversation.getWidth(),100));


        int parentWidth = (int)this.getSize().getWidth();
        pnlAutomatedTesterSettings.setPreferredSize(new Dimension(parentWidth/2,(int)this.getPreferredSize().getHeight()));
        pnlAutomatedTesterTestOutput.setPreferredSize(new Dimension(parentWidth/2,(int)this.getPreferredSize().getHeight()));

        spltAutomatedTester.setResizeWeight(0.5);
    }
}
