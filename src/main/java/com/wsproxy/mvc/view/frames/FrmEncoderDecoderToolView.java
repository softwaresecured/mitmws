package com.wsproxy.mvc.view.frames;

import com.wsproxy.configuration.ApplicationConfig;
import com.wsproxy.mvc.model.EncoderDecoderToolModel;
import com.wsproxy.mvc.view.panels.PnlEncodeDecodeOutput;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class FrmEncoderDecoderToolView extends JFrame {
    private EncoderDecoderToolModel encoderDecoderToolModel;
    private ApplicationConfig applicationConfig = new ApplicationConfig();

    public PnlEncodeDecodeOutput pnlEncode = new PnlEncodeDecodeOutput();
    public PnlEncodeDecodeOutput pnlDecode = new PnlEncodeDecodeOutput();
    private JPanel pnlInput = new JPanel();
    public JTextArea jtxtInputRaw = new JTextArea();

    private JTabbedPane mainTabPane = new JTabbedPane();

    public FrmEncoderDecoderToolView(EncoderDecoderToolModel encoderDecoderToolModel) {
        this.encoderDecoderToolModel = encoderDecoderToolModel;
        initLayout();
    }

    public void initLayout() {
        setTitle("Encoder/decoder");
        setSize(800,600);
        // Main layout

        // Input section
        jtxtInputRaw.setLineWrap(true);
        jtxtInputRaw.setRows(5);
        JScrollPane scrollTextInput = new JScrollPane(jtxtInputRaw);
        scrollTextInput.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollTextInput.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        pnlInput.setBorder(new TitledBorder("Input content"));
        pnlInput.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        pnlInput.add(scrollTextInput,gbc);

        mainTabPane.addTab("Encode", null, pnlEncode);
        mainTabPane.addTab("Decode", null, pnlDecode);


        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.15;
        add(pnlInput,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(mainTabPane,gbc);

    }
}
