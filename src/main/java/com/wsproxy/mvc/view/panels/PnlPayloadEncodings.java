package com.wsproxy.mvc.view.panels;

import javax.swing.*;

public class PnlPayloadEncodings extends JPanel {

    public JCheckBox jchkEncodingJavascript = new JCheckBox("Javascript");
    public JCheckBox jchkEncodingXml = new JCheckBox("XML");
    public JCheckBox jchkEncodingHtml = new JCheckBox("HTML");
    public JCheckBox jchkEncodingBase64 = new JCheckBox("Base64");
    public JCheckBox jchkEncodingURL = new JCheckBox("URL ( full )");


    public PnlPayloadEncodings() {
        initLayout();
    }

    public void resetUi() {
        jchkEncodingJavascript.setSelected(false);
        jchkEncodingXml.setSelected(false);
        jchkEncodingHtml.setSelected(false);
        jchkEncodingBase64.setSelected(false);
        jchkEncodingURL.setSelected(false);
    }
    public void initLayout() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        add(jchkEncodingJavascript);
        add(jchkEncodingXml);
        add(jchkEncodingHtml);
        add(jchkEncodingBase64);
        add(jchkEncodingURL);
        /*
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        add(jchkEncodingJavascript,gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        add(jchkEncodingXml,gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        add(jchkEncodingHtml,gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1;
        add(jchkEncodingBase64,gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1;
        add(jchkEncodingURL,gbc);
        */
    }
}
