package com.wsproxy.mvc.view.panels.manualtester;

import com.wsproxy.util.ScriptUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class PnlUpgradeRequestScriptConfig extends JPanel {
    public JCheckBox jchkUpgrade = new JCheckBox("Upgrade script");
    public JComboBox jcmbUpgradeScripts = new JComboBox();
    public JCheckBox jchkEventScript = new JCheckBox("Event script");
    public JComboBox jcmbEventScripts = new JComboBox();

    public PnlUpgradeRequestScriptConfig() {
        initLayout();
        reloadScripts();
    }

    public void reloadScripts() {

        jcmbEventScripts.removeAll();
        jcmbUpgradeScripts.removeAll();
        ArrayList<String> scripts = ScriptUtil.getScriptsByType("upgrade");
        for ( String script : scripts ) {
            jcmbUpgradeScripts.addItem(script);
        }
        scripts = ScriptUtil.getScriptsByType("events");
        for ( String script : scripts ) {
            jcmbEventScripts.addItem(script);
        }
    }

    public void initLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jchkUpgrade,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jcmbUpgradeScripts,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 0;
        add(jcmbUpgradeScripts,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        add(jchkEventScript,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(jcmbEventScripts,gbc);
    }
}
