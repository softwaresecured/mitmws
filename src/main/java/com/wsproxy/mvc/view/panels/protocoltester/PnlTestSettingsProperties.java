package com.wsproxy.mvc.view.panels.protocoltester;

import com.wsproxy.mvc.controller.HttpRequestResponseController;
import com.wsproxy.mvc.model.HttpRequestResponseModel;
import com.wsproxy.mvc.view.panels.PnlHttpRequestResponse;

import javax.swing.*;
import java.awt.*;

public class PnlTestSettingsProperties extends JPanel {
    public JPanel pnlFrameTypes = new JPanel();

    public JTextArea jtxtTextSample = new JTextArea();
    public JTextArea jtxtBinarySample = new JTextArea();

    public JCheckBox jchkContinuation  = new JCheckBox("CONTINUATION");
    public JCheckBox jchkText  = new JCheckBox("TEXT");
    public JCheckBox jchkBin  = new JCheckBox("BINARY");
    public JCheckBox jchkRes1  = new JCheckBox("RES1");
    public JCheckBox jchkRes2  = new JCheckBox("RES2");
    public JCheckBox jchkRes3  = new JCheckBox("RES3");
    public JCheckBox jchkRes4  = new JCheckBox("RES4");
    public JCheckBox jchkRes5  = new JCheckBox("RES5");
    public JCheckBox jchkClose  = new JCheckBox("CLOSE");
    public JCheckBox jchkPing  = new JCheckBox("PING");
    public JCheckBox jchkPong  = new JCheckBox("PONG");


    public PnlTestSettingsProperties() {
        initLayout();
    }
    public void initLayout() {

        jtxtTextSample.setLineWrap(true);
        jtxtBinarySample.setLineWrap(true);

        JScrollPane scrollBinary = new JScrollPane(jtxtBinarySample);
        scrollBinary.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollBinary.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);


        JScrollPane scrollText = new JScrollPane(jtxtTextSample);
        scrollText.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollText.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        pnlFrameTypes.setBorder(BorderFactory.createTitledBorder("Frame types"));
        pnlFrameTypes.setLayout(new FlowLayout());
        pnlFrameTypes.add(jchkContinuation);
        pnlFrameTypes.add(jchkText);
        pnlFrameTypes.add(jchkBin);
        pnlFrameTypes.add(jchkRes1);
        pnlFrameTypes.add(jchkRes2);
        pnlFrameTypes.add(jchkRes3);
        pnlFrameTypes.add(jchkRes4);
        pnlFrameTypes.add(jchkRes5);
        pnlFrameTypes.add(jchkClose);
        pnlFrameTypes.add(jchkPing);
        pnlFrameTypes.add(jchkPong);


        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0;
        gbc.weightx = 1;
        add(new JLabel("Text payload"), gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(scrollText, gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.weightx = 1;
        add(new JLabel("Binary payload"), gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(scrollBinary, gbc);


        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weighty = 0;
        gbc.weightx = 1;
        add(pnlFrameTypes, gbc);
    }

    public JTextArea getJtxtTextSample() {
        return jtxtTextSample;
    }

    public JTextArea getJtxtBinarySample() {
        return jtxtBinarySample;
    }

    public JCheckBox getJchkContinuation() {
        return jchkContinuation;
    }

    public JCheckBox getJchkText() {
        return jchkText;
    }

    public JCheckBox getJchkBin() {
        return jchkBin;
    }

    public JCheckBox getJchkRes1() {
        return jchkRes1;
    }

    public JCheckBox getJchkRes2() {
        return jchkRes2;
    }

    public JCheckBox getJchkRes3() {
        return jchkRes3;
    }

    public JCheckBox getJchkRes4() {
        return jchkRes4;
    }

    public JCheckBox getJchkRes5() {
        return jchkRes5;
    }

    public JCheckBox getJchkClose() {
        return jchkClose;
    }

    public JCheckBox getJchkPing() {
        return jchkPing;
    }

    public JCheckBox getJchkPong() {
        return jchkPong;
    }
}
