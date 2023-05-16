package com.mitmws.mvc.view.panels.environment;

import com.mitmws.environment.EnvironmentItemType;
import javax.swing.*;
import java.awt.*;

public class PnlEnvironmentItemEditor extends JPanel {
    public JButton jbtnNew = new JButton("New");
    public JButton jbtnSave = new JButton("Save");
    public JButton jbtnDelete = new JButton("Delete");

    public JTextField jtxtEnvVarName = new JTextField();
    public JTextArea jtxtDescription = new JTextArea();
    public JComboBox jcmbEnvItemScope = new JComboBox(new String[]{"ALL", "WEBSOCKET", "HTTP"});
    public JComboBox jcmbEnvItemType = null;
    public PnlEnvironmentItemEditor() {
        initLayout();
    }

    public void selectVarTypeByEnumName( EnvironmentItemType environmentItemType ) {
        for ( int i = 0; i < jcmbEnvItemType.getItemCount(); i++ ) {
            if ( getEnvVarEnumName((String) jcmbEnvItemType.getItemAt(i)).equals(environmentItemType)) {
                jcmbEnvItemType.setSelectedIndex(i);
                break;
            }
        }
    }

    public String getEnvVarItemTypeName( EnvironmentItemType environmentItemType) {
        String typeName = "unknown";
        switch ( environmentItemType ) {
            case BUILTIN:
                typeName = "Built-in";
                break;
            case VARIABLE_REGEX_REPLACEMENT:
                typeName = "Regex replacement";
                break;
            case VARIABLE_STRING_REPLACEMENT:
                typeName = "String replacement";
                break;
            case VARIABLE_SESSION:
                typeName = "Session";
                break;
            case VARIABLE_SCRIPT:
                typeName = "Script";
                break;
        }
        return typeName;
    }

    public EnvironmentItemType getSelectedEnvVarItemType() {
        return getEnvVarEnumName (jcmbEnvItemType.getSelectedItem().toString());
    }
    
    public EnvironmentItemType getEnvVarEnumName( String envVarTypeName ) {
        EnvironmentItemType environmentItemType = EnvironmentItemType.VARIABLE_STRING_REPLACEMENT;
        switch ( envVarTypeName ) {
            case "Built-in":
                environmentItemType = EnvironmentItemType.BUILTIN;
                break;
            case "Regex replacement":
                environmentItemType = EnvironmentItemType.VARIABLE_REGEX_REPLACEMENT;
                break;
            case "String replacement":
                environmentItemType = EnvironmentItemType.VARIABLE_STRING_REPLACEMENT;
                break;
            case "Session":
                environmentItemType = EnvironmentItemType.VARIABLE_SESSION;
                break;
            case "Script":
                environmentItemType = EnvironmentItemType.VARIABLE_SCRIPT;
                break;
        }
        return environmentItemType;
    }


    public void initLayout() {
        jcmbEnvItemType = new JComboBox(new String[] {
                getEnvVarItemTypeName(EnvironmentItemType.BUILTIN),
                getEnvVarItemTypeName(EnvironmentItemType.VARIABLE_REGEX_REPLACEMENT),
                getEnvVarItemTypeName(EnvironmentItemType.VARIABLE_STRING_REPLACEMENT),
                getEnvVarItemTypeName(EnvironmentItemType.VARIABLE_SESSION),
                getEnvVarItemTypeName(EnvironmentItemType.VARIABLE_SCRIPT)
        });


        this.setMinimumSize(new Dimension(jtxtDescription.getWidth(),100));
        JScrollPane scrollDescViewer = new JScrollPane(jtxtDescription);
        scrollDescViewer.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollDescViewer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // The toolbar
        JPanel pnlEditorToolbar = new JPanel();
        pnlEditorToolbar.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        pnlEditorToolbar.add(new JLabel("Name"),gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        pnlEditorToolbar.add(jtxtEnvVarName,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        pnlEditorToolbar.add(new JLabel("Scope"),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        pnlEditorToolbar.add(jcmbEnvItemScope,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        pnlEditorToolbar.add(new JLabel("Type"),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        pnlEditorToolbar.add(jcmbEnvItemType,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.weightx = 1;
        pnlEditorToolbar.add(new JPanel(),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 0;
        pnlEditorToolbar.add(jbtnNew,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 0;
        pnlEditorToolbar.add(jbtnSave,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 0;
        pnlEditorToolbar.add(jbtnDelete,gbc);

        // The main layout

        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        add(pnlEditorToolbar,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        add(new JLabel("Description"),gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        add(scrollDescViewer,gbc);
    }
}
