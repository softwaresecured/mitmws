package com.wsproxy.mvc.controller;

import com.wsproxy.environment.EnvironmentItemScope;
import com.wsproxy.environment.EnvironmentItemType;
import com.wsproxy.environment.EnvironmentVariable;
import com.wsproxy.httpproxy.trafficlogger.WebsocketTrafficRecord;
import com.wsproxy.projects.ProjectDataServiceException;
import com.wsproxy.util.GuiUtils;
import com.wsproxy.mvc.model.EnvironmentModel;
import com.wsproxy.mvc.model.MainModel;
import com.wsproxy.mvc.view.panels.environment.PnlEnvironmentView;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class EnvironmentController implements PropertyChangeListener {

    private PnlEnvironmentView pnlEnvironmentView;
    private EnvironmentModel environmentModel;
    private MainModel mainModel;
    public EnvironmentController(MainModel mainModel, PnlEnvironmentView pnlEnvironmentView) {
        this.mainModel = mainModel;
        this.pnlEnvironmentView = pnlEnvironmentView;
        environmentModel = mainModel.getEnvironmentModel();
        loadEnvironment();
        initEventListeners();
        environmentModel.getCurrentEnvironmentVariable().addListener(this);
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnSave.setEnabled(true);
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnDelete.setEnabled(true);
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.pnlEnvironmentVariableRegexTesterToolbar.btnClear.setEnabled(false);
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.pnlEnvironmentVariableRegexTesterToolbar.btnTest.setEnabled(false);
        GuiUtils.tableSelectFirst(pnlEnvironmentView.tblEnvironment);
    }

    public void initEventListeners() {
        /*
            Environment variable table row select
         */
        pnlEnvironmentView.tblEnvironment.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            int rowId = pnlEnvironmentView.tblEnvironment.getSelectedRow();
            if ( rowId >= 0 ) {
                boolean editable = !pnlEnvironmentView.tblEnvironment.getValueAt(rowId, 1).toString().equals("BUILTIN");
                pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnDelete.setEnabled(editable);
                pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnSave.setEnabled(editable);
                pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnNew.setEnabled(true);
                pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.pnlEnvironmentVariableRegexTesterToolbar.btnTest.setEnabled(true);
                String environmentVarName = pnlEnvironmentView.tblEnvironment.getValueAt(rowId, 3).toString();
                if (environmentVarName != null) {
                    EnvironmentVariable curVar = environmentModel.getEnvironment().getVariable(environmentVarName);
                    if ( curVar != null ) {
                        environmentModel.getCurrentEnvironmentVariable().setEnvironmentVariable(curVar);
                    }
                }
            }
        });

        /*
            Variable enable/disable
         */
        pnlEnvironmentView.tblEnvironment.getModel().addTableModelListener( tableModelEvent -> {
            if ( tableModelEvent.getColumn() == 0 ) {
                TableModel model = (TableModel) tableModelEvent.getSource();
                environmentModel.getCurrentEnvironmentVariable().setEnabled((Boolean) model.getValueAt(tableModelEvent.getFirstRow(), tableModelEvent.getColumn()));
                environmentModel.getEnvironment().saveEnvironment();
            }
        });
        /*
            Test row select
         */
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.tblEnvVarTest.getModel().addTableModelListener( tableModelEvent -> {
            if ( pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.tblEnvVarTest.getModel().getRowCount() > 0 ) {
                pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.pnlEnvironmentVariableRegexTesterToolbar.btnClear.setEnabled(true);
            }
        });
        /*
            New variable
         */
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnNew.addActionListener(actionEvent -> {
            EnvironmentVariable newVar = new EnvironmentVariable();
            newVar.setName(String.format("Untitled%d", environmentModel.getEnvironment().getEnvironmentVariables().size()));
            newVar.setEnabled(false);
            newVar.setEnvironmentItemScope(EnvironmentItemScope.ALL);
            newVar.setEnvironmentItemType(EnvironmentItemType.VARIABLE_STRING_REPLACEMENT);
            environmentModel.getCurrentEnvironmentVariable().setEnvironmentVariable(newVar);
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.setEnabled(true);
            pnlEnvironmentView.tblEnvironment.clearSelection();
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnDelete.setEnabled(false);
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnNew.setEnabled(false);
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnSave.setEnabled(true);
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.pnlEnvironmentVariableRegexTesterToolbar.btnTest.setEnabled(true);
        });
        /*
            Save variable
         */
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnSave.addActionListener(actionEvent -> {
            environmentModel.getCurrentEnvironmentVariable().setValidationIssues(validateEnvironmentVariable());
            if ( environmentModel.getCurrentEnvironmentVariable().getValidationIssues().size() == 0 ) {
                saveCurrentEnvironmentVariable();
                //updateSelectedVariable(currentEnvironmentVariable.getName());
            }
        });
        /*
            Delete variable
         */
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnDelete.addActionListener( actionEvent -> {
            String deleteVar = pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.getText();
            environmentModel.getEnvironment().deleteVariable(deleteVar);
            for ( int i = 0; i < environmentModel.getEnvironmentTableModel().getRowCount(); i++ ) {
                String varName = (String) environmentModel.getEnvironmentTableModel().getValueAt(i,3);
                if ( varName != null ) {
                    if ( varName.equals(deleteVar) ) {
                        environmentModel.getEnvironmentTableModel().removeRow(i);
                        break;
                    }
                }
            }
            environmentModel.getEnvironment().saveEnvironment();
            syncEnvironmentTable();
        });

        /*
            Test
         */
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.pnlEnvironmentVariableRegexTesterToolbar.btnTest.addActionListener(actionEvent -> {
            DefaultTableModel trafficModel = (DefaultTableModel) pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.tblEnvVarTest.getModel();
            EnvironmentVariable currentEnvironmentVariable = environmentModel.getCurrentEnvironmentVariable().getEnvironmentVariable();
            if ( currentEnvironmentVariable != null ) {
                currentEnvironmentVariable.setStoredVariable(null);
            }
            ArrayList<WebsocketTrafficRecord> records;
            int offset = 0;
            try {
                do {
                    records = mainModel.getProjectModel().getProjectDataService().getSearchableRecords(offset,10);
                    for ( WebsocketTrafficRecord record : records ) {
                        if ( record.getFrame().getPayloadUnmasked() != null ) {
                            byte payload[] = record.getFrame().getPayloadUnmasked();
                            String payloadStr = new String(payload);
                            String environmentModifiedPayloadStr = new String(currentEnvironmentVariable.processBytes(mainModel.getInteractshModel(),record.getFrame().getConversationUUID(),payload));
                            String operation = null;
                            if ( environmentModifiedPayloadStr != null ) {
                                if ( !environmentModifiedPayloadStr.equals(payloadStr)) {
                                    operation = String.format("Applied %s", currentEnvironmentVariable.getName());
                                }
                                if ( currentEnvironmentVariable.getStoredVariable() != null ) {
                                    operation = String.format("Applied %s, stored %d bytes", currentEnvironmentVariable.getName(), currentEnvironmentVariable.getStoredVariable().length());
                                }
                                if ( operation != null ) {
                                    trafficModel.addRow(new Object[] {
                                            record.getTrafficSource().toString(),
                                            operation,
                                            environmentModifiedPayloadStr
                                    });
                                }
                            }
                        }
                    }
                    offset = records.size();
                } while ( records.size() > 0 );
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ProjectDataServiceException e) {
                e.printStackTrace();
            }
        });

        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jcmbEnvItemType.addActionListener(actionEvent -> {
            environmentModel.getCurrentEnvironmentVariable().setEnvironmentItemType(pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.getSelectedEnvVarItemType());
        });

        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.pnlEnvironmentVariableRegexTesterToolbar.btnClear.addActionListener(actionEvent -> {
            DefaultTableModel model = (DefaultTableModel) pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.tblEnvVarTest.getModel();
            model.setRowCount(0);
        });
    }


    public void loadEnvironment() {
        environmentModel.getEnvironment().loadEnvironment();
        for ( EnvironmentVariable envVar : environmentModel.getEnvironment().getEnvironmentVariables()) {
            environmentModel.getEnvironmentTableModel().addRow(new Object[] {
                    envVar.isEnabled(),
                    envVar.getEnvironmentItemType().toString(),
                    envVar.getEnvironmentItemScope().toString(),
                    envVar.getName()
            });
        }
    }

    public void saveCurrentEnvironmentVariable() {
        EnvironmentVariable currentEnvironmentVariable = environmentModel.getCurrentEnvironmentVariable().getEnvironmentVariable();
        if ( currentEnvironmentVariable != null ) {
            String newVarName = currentEnvironmentVariable.getName();
            if ( environmentModel.getEnvironment().getVariable(currentEnvironmentVariable.getName()) != null ) {
                currentEnvironmentVariable = environmentModel.getEnvironment().getVariable(newVarName);
            }

            // Env var header
            currentEnvironmentVariable.setEnvironmentItemType(pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.getSelectedEnvVarItemType());
            currentEnvironmentVariable.setName(pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.getText());
            currentEnvironmentVariable.setDescription(pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtDescription.getText());

            // String variable
            currentEnvironmentVariable.setStringReplacementText(pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.jtxtStringReplacementText.getText());
            currentEnvironmentVariable.setStringReplacementMatchText(pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.jtxtStringReplacementMatch.getText());

            // Regex variable
            currentEnvironmentVariable.setMatchRegexGroup((Integer) pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jspnMatchRegexGroup.getValue());
            currentEnvironmentVariable.setRegexMatchGroupEnabled(pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jchkMatchGroup.isSelected());
            currentEnvironmentVariable.setRegexStringReplacementText(pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtRegexReplacementText.getText());
            currentEnvironmentVariable.setMatchRegexPattern(GuiUtils.getPattern(pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtMatchRegex.getText()));

            // Script variable
            currentEnvironmentVariable.setScriptMatchRegexGroup((Integer) pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jspnScriptMatchRegexGroup.getValue());
            if ( pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jcmbScripts.getSelectedItem() != null ) {
                currentEnvironmentVariable.setScriptName(pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jcmbScripts.getSelectedItem().toString());
            }

            currentEnvironmentVariable.setScriptMatchRegex(GuiUtils.getPattern(pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jtxtScriptMatchRegex.getText()));


            // Session variable
            currentEnvironmentVariable.setInputRegexMatchGroup((Integer) pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jspnInputMatchGroup.getValue());
            currentEnvironmentVariable.setOutputRegexMatchGroup((Integer) pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jspnOutputMatchGroup.getValue());
            currentEnvironmentVariable.setInputRegexPattern(GuiUtils.getPattern(pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexInput.getText()));
            currentEnvironmentVariable.setOutputRegexPattern(GuiUtils.getPattern(pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexOutput.getText()));
            environmentModel.getEnvironment().setVariable(currentEnvironmentVariable);

            //loadVariableFields(currentEnvironmentVariable);
            syncEnvironmentTable();
            environmentModel.getEnvironment().saveEnvironment();
            //pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.setEnabled(false);
        }
        else {
            //System.out.println("Current env var is null");
        }
    }

    public void syncEnvironmentTable() {
        for ( EnvironmentVariable envVar : environmentModel.getEnvironment().getEnvironmentVariables()) {
            boolean updated = false;
            for ( int i = 0; i < pnlEnvironmentView.tblEnvironment.getRowCount(); i++ ) {
                String varName = (String) pnlEnvironmentView.tblEnvironment.getValueAt(i,3);
                if (varName.equals(envVar.getName())) {
                    pnlEnvironmentView.tblEnvironment.setValueAt( envVar.isEnabled(),i,0);
                    pnlEnvironmentView.tblEnvironment.setValueAt( envVar.getEnvironmentItemType().toString(),i,1);
                    pnlEnvironmentView.tblEnvironment.setValueAt( envVar.getEnvironmentItemScope().toString(),i,2);
                    pnlEnvironmentView.tblEnvironment.setValueAt( envVar.getName(),i,3);
                    updated = true;
                }
            }
            if ( !updated ) {
                environmentModel.getEnvironmentTableModel().addRow(new Object[] {
                        envVar.isEnabled(),
                        envVar.getEnvironmentItemType().toString(),
                        envVar.getEnvironmentItemScope().toString(),
                        envVar.getName()
                });
                GuiUtils.tableSelectLast(pnlEnvironmentView.tblEnvironment);
            }
        }

    }

    public void selectEditor() {
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.setVisible(false);
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.setVisible(false);
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.setVisible(false);
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.setVisible(false);
        EnvironmentVariable envVar = environmentModel.getCurrentEnvironmentVariable().getEnvironmentVariable();
        switch ( envVar.getEnvironmentItemType() ) {
            case VARIABLE_STRING_REPLACEMENT:
                pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.setVisible(true);
                break;
            case VARIABLE_REGEX_REPLACEMENT:
                pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.setVisible(true);
                break;
            case VARIABLE_SESSION:
                pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.setVisible(true);
                break;
            case VARIABLE_SCRIPT:
                pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.setVisible(true);
                break;
        }
    }

    /*
        Validates the fields in the UI
     */
    public ArrayList<String> validateEnvironmentVariable() {
        Pattern pattern;
        ArrayList<String> validationIssues = new ArrayList<>();
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.setBackground(Color.WHITE);
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtMatchRegex.setBackground(Color.WHITE);
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jtxtScriptMatchRegex.setBackground(Color.WHITE);
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexInput.setBackground(Color.WHITE);
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexOutput.setBackground(Color.WHITE);
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.jtxtStringReplacementText.setBackground(Color.WHITE);


        // Variable name - uniqueness
        String curVarName = pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.getText();
        EnvironmentVariable nameCheck = environmentModel.getEnvironment().getVariable(curVarName);
        if ( nameCheck != null ) {
            if ( !nameCheck.getId().equals(environmentModel.getCurrentEnvironmentVariable().getEnvironmentVariable().getId()) ) {
                validationIssues.add("The variable name must be unique");
                pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.setBackground(new Color(240, 128, 128));
            }
        }

        // Variable name - length
        if ( pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.getText().length() == 0 ) {
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.setBackground(new Color(240,128,128));
            validationIssues.add("The environment variable requires a name");
        }
        // Variable content
        if ( environmentModel.getCurrentEnvironmentVariable().getEnvironmentVariable() != null ) {
            switch (pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.getSelectedEnvVarItemType()) {
                case VARIABLE_STRING_REPLACEMENT:
                    if ( pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.jtxtStringReplacementText.getText().length() == 0 ) {
                        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.jtxtStringReplacementText.setBackground(new Color(240, 128, 128));
                        validationIssues.add("Replacement string required");
                    }
                    break;
                case VARIABLE_REGEX_REPLACEMENT:
                    if  ( pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtMatchRegex.getText().length() > 0 ) {
                        try {
                            pattern = Pattern.compile(pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtMatchRegex.getText());
                            // if using groups
                            if ( pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jchkMatchGroup.isSelected()) {
                                if ( GuiUtils.getMatchGroupCount(pattern) > 0 ) {
                                    if ( (Integer) pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jspnMatchRegexGroup.getValue() > GuiUtils.getMatchGroupCount(pattern) || GuiUtils.getMatchGroupCount(pattern) == 0 ) {
                                        validationIssues.add("The match target match group is not within range of the match groups provided by the regex");                                }
                                }
                                else {
                                    pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtMatchRegex.setBackground(Color.YELLOW);
                                    validationIssues.add("The match input regex needs a match group");
                                }
                            }
                            else {
                                // TODO
                            }
                        }
                        catch ( PatternSyntaxException e ) {
                            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtMatchRegex.setBackground(new Color(240,128,128));
                            validationIssues.add("Bad regex");
                        }
                    }
                    break;
                case VARIABLE_SESSION:
                    // The input pattern
                    if  ( pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexInput.getText().length() > 0 ) {
                        try {
                            pattern = Pattern.compile(pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexInput.getText());
                            if ( GuiUtils.getMatchGroupCount(pattern) > 0 ) {
                                if ( (Integer) pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jspnInputMatchGroup.getValue() > GuiUtils.getMatchGroupCount(pattern) || GuiUtils.getMatchGroupCount(pattern) == 0 ) {
                                    pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexInput.setBackground(Color.YELLOW);
                                    validationIssues.add("The input target match group is not within range of the match groups provided by the regex");
                                }
                            }
                            else {
                                pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexInput.setBackground(Color.YELLOW);
                                validationIssues.add("The input regex needs a match group");
                            }

                        }
                        catch ( PatternSyntaxException e ) {
                            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexInput.setBackground(new Color(240,128,128));
                            validationIssues.add("Bad regex");
                        }
                    }

                    // The output pattern
                    if ( pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexOutput.getText().length() > 0 ) {
                        try {
                            pattern = GuiUtils.getPattern(pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexOutput.getText());
                            if ( GuiUtils.getMatchGroupCount(pattern) > 0 ) {
                                if ( (Integer) pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jspnOutputMatchGroup.getValue() > GuiUtils.getMatchGroupCount(pattern) || GuiUtils.getMatchGroupCount(pattern) == 0 ) {
                                    pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexOutput.setBackground(Color.YELLOW);
                                    validationIssues.add("The output target match group is not within range of the match groups provided by the regex");
                                }
                            }
                            else {
                                pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexOutput.setBackground(Color.YELLOW);
                                validationIssues.add("The output regex needs a match group");
                            }
                        }
                        catch ( PatternSyntaxException e ) {
                            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexOutput.setBackground(new Color(240,128,128));
                            validationIssues.add("Bad regex");
                        }
                    }
                    break;
                case VARIABLE_SCRIPT:
                    if  ( pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jtxtScriptMatchRegex.getText().length() > 0 ) {
                        try {
                            pattern = Pattern.compile(pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jtxtScriptMatchRegex.getText());
                            if ( GuiUtils.getMatchGroupCount(pattern) > 0 ) {
                                if ( (Integer) pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jspnScriptMatchRegexGroup.getValue() > GuiUtils.getMatchGroupCount(pattern) || GuiUtils.getMatchGroupCount(pattern) == 0 ) {
                                    validationIssues.add("The match target match group is not within range of the match groups provided by the regex");                                }
                            }
                            else {
                                pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jtxtScriptMatchRegex.setBackground(Color.YELLOW);
                                validationIssues.add("The match input regex needs a match group");
                            }

                        }
                        catch ( PatternSyntaxException e ) {
                            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jtxtScriptMatchRegex.setBackground(new Color(240,128,128));
                            validationIssues.add("Bad regex");
                        }
                    }
                    break;
            }
        }
        return validationIssues;
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        // Current variable
        EnvironmentVariable envVar = environmentModel.getCurrentEnvironmentVariable().getEnvironmentVariable();
        if ( "EnvironmentVariableModel.name".equals(propertyChangeEvent.getPropertyName())) {
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.setText(envVar.getName());
        }

        if ( "EnvironmentVariableModel.description".equals(propertyChangeEvent.getPropertyName())) {
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtDescription.setText(envVar.getDescription());
        }

        if ( "EnvironmentVariableModel.enabled".equals(propertyChangeEvent.getPropertyName())) {
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.setEnabled(false);
        }
        if ( "EnvironmentVariableModel.environmentItemScope".equals(propertyChangeEvent.getPropertyName())) {
            GuiUtils.setComboBoxItem(pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jcmbEnvItemScope, envVar.getEnvironmentItemScope().toString());
        }
        if ( "EnvironmentVariableModel.environmentItemType".equals(propertyChangeEvent.getPropertyName())) {
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.selectVarTypeByEnumName(envVar.getEnvironmentItemType());
            selectEditor();
        }
        if ( "EnvironmentVariableModel.inputRegexMatchGroup".equals(propertyChangeEvent.getPropertyName())) {
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jspnInputMatchGroup.setValue(envVar.getInputRegexMatchGroup());
        }
        if ( "EnvironmentVariableModel.outputRegexMatchGroup".equals(propertyChangeEvent.getPropertyName())) {
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jspnOutputMatchGroup.setValue(envVar.getOutputRegexMatchGroup());
        }
        if ( "EnvironmentVariableModel.inputRegexPattern".equals(propertyChangeEvent.getPropertyName())) {
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexInput.setText(GuiUtils.getPatternString(envVar.getInputRegexPattern()));
        }
        if ( "EnvironmentVariableModel.outputRegexPattern".equals(propertyChangeEvent.getPropertyName())) {
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexOutput.setText(GuiUtils.getPatternString(envVar.getOutputRegexPattern()));
        }
        if ( "EnvironmentVariableModel.storedVariable".equals(propertyChangeEvent.getPropertyName())) {
        }
        if ( "EnvironmentVariableModel.regexStringReplacementText".equals(propertyChangeEvent.getPropertyName())) {
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.jtxtStringReplacementText.setText(envVar.getStringReplacementText());
        }
        if ( "EnvironmentVariableModel.stringReplacementMatchText".equals(propertyChangeEvent.getPropertyName())) {
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.jtxtStringReplacementMatch.setText(envVar.getStringReplacementMatchText());
        }
        if ( "EnvironmentVariableModel.matchRegexPattern".equals(propertyChangeEvent.getPropertyName())) {
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtMatchRegex.setText(GuiUtils.getPatternString(envVar.getMatchRegexPattern()));
        }
        if ( "EnvironmentVariableModel.matchRegexGroup".equals(propertyChangeEvent.getPropertyName())) {
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jspnMatchRegexGroup.setValue(envVar.getMatchRegexGroup());
        }
        if ( "EnvironmentVariableModel.regexMatchGroupEnabled".equals(propertyChangeEvent.getPropertyName())) {
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jchkMatchGroup.setSelected(envVar.getRegexMatchGroupEnabled());
        }
        if ( "EnvironmentVariableModel.regexStringReplacementText".equals(propertyChangeEvent.getPropertyName())) {
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtRegexReplacementText.setText(envVar.getRegexStringReplacementText());
        }
        if ( "EnvironmentVariableModel.scriptMatchRegex".equals(propertyChangeEvent.getPropertyName())) {
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jtxtScriptMatchRegex.setText(GuiUtils.getPatternString(envVar.getScriptMatchRegex()));
        }
        if ( "EnvironmentVariableModel.scriptName".equals(propertyChangeEvent.getPropertyName())) {
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jcmbScripts.setSelectedItem(envVar.getScriptName());
        }
        if ( "EnvironmentVariableModel.scriptMatchRegexGroup".equals(propertyChangeEvent.getPropertyName())) {
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jspnScriptMatchRegexGroup.setValue(envVar.getScriptMatchRegexGroup());
        }

        if ( "EnvironmentVariableModel.environmentVariable".equals(propertyChangeEvent.getPropertyName())) {
            loadEnvironmentVariable(envVar);
        }

        if ( "EnvironmentVariableModel.validationIssues".equals(propertyChangeEvent.getPropertyName())) {
            String errorStr = "";
            for ( String validationIssue : environmentModel.getCurrentEnvironmentVariable().getValidationIssues() ) {
                errorStr += String.format("%s\n", validationIssue);
            }
            pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.jtxtValidationIssues.setText(errorStr);
        }




    }
    public void loadEnvironmentVariable( EnvironmentVariable envVar) {
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.setText(envVar.getName());
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtDescription.setText(envVar.getDescription());
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.setEnabled(false);
        GuiUtils.setComboBoxItem(pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jcmbEnvItemScope, envVar.getEnvironmentItemScope().toString());
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.selectVarTypeByEnumName(envVar.getEnvironmentItemType());
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jspnInputMatchGroup.setValue(envVar.getInputRegexMatchGroup());
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jspnOutputMatchGroup.setValue(envVar.getOutputRegexMatchGroup());
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexInput.setText(GuiUtils.getPatternString(envVar.getInputRegexPattern()));
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexOutput.setText(GuiUtils.getPatternString(envVar.getOutputRegexPattern()));
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.jtxtStringReplacementText.setText(envVar.getStringReplacementText());
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.jtxtStringReplacementMatch.setText(envVar.getStringReplacementMatchText());
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtMatchRegex.setText(GuiUtils.getPatternString(envVar.getMatchRegexPattern()));
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jspnMatchRegexGroup.setValue(envVar.getMatchRegexGroup());
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jchkMatchGroup.setSelected(envVar.getRegexMatchGroupEnabled());
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtRegexReplacementText.setText(envVar.getRegexStringReplacementText());
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jtxtScriptMatchRegex.setText(GuiUtils.getPatternString(envVar.getScriptMatchRegex()));
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jcmbScripts.setSelectedItem(envVar.getScriptName());
        pnlEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jspnScriptMatchRegexGroup.setValue(envVar.getScriptMatchRegexGroup());
        selectEditor();
    }
}
