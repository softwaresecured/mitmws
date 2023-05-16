package com.mitmws.mvc.view.panels;

import com.mitmws.util.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class PnlEncodeDecodeOutput extends JPanel {

    public JTextArea jtxtBase64 = new JTextArea();
    public JTextArea jtxtURL = new JTextArea();
    public JTextArea jtxtURLFull = new JTextArea();
    public JTextArea jtxtSlashX = new JTextArea();
    public JTextArea jtxtSlashU = new JTextArea();
    public JTextArea jtxtHTML = new JTextArea();
    public JTextArea jtxtJavascript = new JTextArea();


    public PnlEncodeDecodeOutput() {
        initLayout();
    }

    public void initLayout() {
        jtxtBase64.setLineWrap(true);
        jtxtURL.setLineWrap(true);
        jtxtURLFull.setLineWrap(true);
        jtxtSlashX.setLineWrap(true);
        jtxtHTML.setLineWrap(true);
        jtxtJavascript.setLineWrap(true);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(GuiUtils.frameWrapComponent(GuiUtils.scrollPaneWrap(jtxtBase64,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS),"Base64"),gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(GuiUtils.frameWrapComponent(GuiUtils.scrollPaneWrap(jtxtURL,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS),"URL"),gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(GuiUtils.frameWrapComponent(GuiUtils.scrollPaneWrap(jtxtURLFull,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS),"URL (full)"),gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(GuiUtils.frameWrapComponent(GuiUtils.scrollPaneWrap(jtxtSlashX,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS),"\\x escape"),gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(GuiUtils.frameWrapComponent(GuiUtils.scrollPaneWrap(jtxtSlashU,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS),"\\u escape"),gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(GuiUtils.frameWrapComponent(GuiUtils.scrollPaneWrap(jtxtHTML,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS),"HTML"),gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(GuiUtils.frameWrapComponent(GuiUtils.scrollPaneWrap(jtxtJavascript,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS),"Javascript"),gbc);
    }
}
