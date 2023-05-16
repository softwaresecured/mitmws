package com.mitmws.mvc.view.panels.manualtester;
import com.mitmws.mvc.view.panels.PnlFrameEditor;

import javax.swing.*;
import java.awt.*;

public class PnlTestStepEditor extends JPanel {
    public PnlTestStepEditorToolbar pnlTestStepEditorToolbar = new PnlTestStepEditorToolbar();
    public PnlFrameEditor pnlFrameEditor = new PnlFrameEditor();
    public PnlTestStepOperationEditor pnlTestStepOperationEditor = new PnlTestStepOperationEditor();
    public PnlTestStepEditor() {
        initLayout();
    }

    public void initLayout() {
        pnlTestStepEditorToolbar.setVisible(false);
        pnlFrameEditor.setVisible(false);
        pnlTestStepOperationEditor.setVisible(false);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(pnlTestStepEditorToolbar,gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(pnlFrameEditor,gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(pnlTestStepOperationEditor,gbc);
    }

}
