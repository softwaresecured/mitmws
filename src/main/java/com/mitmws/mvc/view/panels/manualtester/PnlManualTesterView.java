package com.mitmws.mvc.view.panels.manualtester;
import com.mitmws.logging.AppLog;
import com.mitmws.mvc.model.ManualTesterModel;
import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

public class PnlManualTesterView extends JPanel {
    public PnlWsConversationEditor pnlWsConversationEditor = null;
    public PnlWsConversationHistory pnlWsConversationHistory = null;
    public JSplitPane spltManualTester = null;
    public static Logger LOGGER = AppLog.getLogger(PnlManualTesterView.class.getName());

    private ManualTesterModel manualTesterModel;
    public PnlManualTesterView(ManualTesterModel manualTesterModel) {
        this.manualTesterModel = manualTesterModel;
        initLayout();
    }

    public void initLayout() {
        pnlWsConversationEditor = new PnlWsConversationEditor(manualTesterModel);
        pnlWsConversationHistory = new PnlWsConversationHistory(manualTesterModel);
        pnlWsConversationEditor.pnlConversationBuilder.pnlTestStepEditor.setEnabled(false);
        spltManualTester = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,pnlWsConversationEditor, pnlWsConversationHistory);
        setLayout( new BoxLayout(this,BoxLayout.Y_AXIS));
        add(spltManualTester);
        pnlWsConversationEditor.pnlConversationBuilder.pnlWsConversationToolbar.jbtnAutomate.setEnabled(false);
        int parentWidth = (int)this.getSize().getWidth();
        pnlWsConversationEditor.setPreferredSize(new Dimension(parentWidth/2,(int)this.getPreferredSize().getHeight()));
        pnlWsConversationHistory.setPreferredSize(new Dimension(parentWidth/2,(int)this.getPreferredSize().getHeight()));
        spltManualTester.setResizeWeight(0.5);
    }

}
