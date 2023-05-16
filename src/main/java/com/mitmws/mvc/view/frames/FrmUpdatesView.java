package com.mitmws.mvc.view.frames;

import com.mitmws.mvc.model.UpdatesModel;
import com.mitmws.mvc.view.panels.updates.PnlUpdateContentViewer;
import com.mitmws.mvc.view.panels.updates.PnlUpdatesTableViewer;

import javax.swing.*;
import java.awt.*;

public class FrmUpdatesView extends JFrame {
    private UpdatesModel updatesModel;
    public JSplitPane splt;
    public PnlUpdateContentViewer pnlUpdateContentViewer = new PnlUpdateContentViewer();
    public PnlUpdatesTableViewer pnlUpdatesTableViewer;
    public FrmUpdatesView(UpdatesModel updatesModel) {
        this.updatesModel = updatesModel;
        initLayout();
    }
    public void initLayout() {
        setTitle("Updates");
        setSize(800,600);

        pnlUpdatesTableViewer = new PnlUpdatesTableViewer(updatesModel);
        splt = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,pnlUpdatesTableViewer, pnlUpdateContentViewer);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(splt,gbc);
        int parentWidth = (int)this.getSize().getWidth();
        pnlUpdatesTableViewer.setPreferredSize(new Dimension(parentWidth/2,(int)this.getPreferredSize().getHeight()));
        pnlUpdateContentViewer.setPreferredSize(new Dimension(parentWidth/2,(int)this.getPreferredSize().getHeight()));
        splt.setResizeWeight(0.25);
    }
}
