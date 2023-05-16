package com.mitmws.mvc.view.panels.immediate;
import com.mitmws.mvc.model.ImmediateModel;

import javax.swing.*;
import java.awt.*;

public class PnlImmediateView extends JPanel {
    private ImmediateModel immediateModel;
    public PnlImmediateViewer pnlImmediateViewer;
    public PnlImmediateEditor pnlImmediateEditor;
    public JSplitPane splt;
    public PnlImmediateView( ImmediateModel immediateModel) {
        this.immediateModel = immediateModel;
        initLayout();
    }
    public void initLayout() {
        pnlImmediateViewer = new PnlImmediateViewer(immediateModel.getWebsocketTrafficTableModel());
        pnlImmediateEditor = new PnlImmediateEditor(immediateModel);
        splt = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,pnlImmediateEditor, pnlImmediateViewer);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(splt,gbc);
        int parentWidth = (int)this.getSize().getWidth();
        pnlImmediateViewer.setPreferredSize(new Dimension(parentWidth/2,(int)this.getPreferredSize().getHeight()));
        pnlImmediateEditor.setPreferredSize(new Dimension(parentWidth/2,(int)this.getPreferredSize().getHeight()));
        splt.setResizeWeight(0.5);
    }
}
