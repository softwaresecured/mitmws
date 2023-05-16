package com.mitmws.mvc.view.panels.environment;

import com.mitmws.util.ScriptUtil;

import javax.swing.*;
import java.awt.*;

public class PnlEnvScriptReplacement extends JPanel {
    public JTextField jtxtScriptMatchRegex = new JTextField();
    public JSpinner jspnScriptMatchRegexGroup = new JSpinner(new SpinnerNumberModel(1,1,1000,1));
    public JComboBox jcmbScripts = new JComboBox();
    public PnlEnvScriptReplacement() {
        initLayout();
    }

    public void initLayout() {
        for ( String curScript : ScriptUtil.getScriptsByType("variables")) {
            jcmbScripts.addItem(curScript);
        }
        jcmbScripts.setPreferredSize(new Dimension(100,(int)jtxtScriptMatchRegex.getPreferredSize().getHeight()));
        jtxtScriptMatchRegex.setPreferredSize(new Dimension(100,(int) jtxtScriptMatchRegex.getPreferredSize().getHeight()));
        jspnScriptMatchRegexGroup.setPreferredSize(new Dimension(50,(int) jtxtScriptMatchRegex.getPreferredSize().getHeight()));
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Match regex"),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        add(jtxtScriptMatchRegex,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        add(new JLabel("Match group"),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        add(jspnScriptMatchRegexGroup,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        add(new JLabel("Script"),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        add(jcmbScripts,gbc);
    }
}
