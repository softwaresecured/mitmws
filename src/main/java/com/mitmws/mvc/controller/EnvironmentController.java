package com.mitmws.mvc.controller;

import com.mitmws.environment.EnvironmentItemScope;
import com.mitmws.environment.EnvironmentItemType;
import com.mitmws.environment.EnvironmentVariable;
import com.mitmws.httpproxy.trafficlogger.WebsocketTrafficRecord;
import com.mitmws.projects.ProjectDataServiceException;
import com.mitmws.util.GuiUtils;
import com.mitmws.mvc.model.EnvironmentModel;
import com.mitmws.mvc.model.MainModel;
import com.mitmws.mvc.view.frames.FrmEnvironmentView;

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

    private FrmEnvironmentView frmEnvironmentView;
    private EnvironmentModel environmentModel;
    private MainModel mainModel;
    public EnvironmentController(MainModel mainModel, FrmEnvironmentView frmEnvironmentView) {
        this.mainModel = mainModel;
        this.frmEnvironmentView = frmEnvironmentView;
        environmentModel = mainModel.getEnvironmentModel();
        loadEnvironment();
        initEventListeners();
        environmentModel.getCurrentEnvironmentVariable().addListener(this);
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnSave.setEnabled(true);
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnDelete.setEnabled(true);
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.pnlEnvironmentVariableRegexTesterToolbar.btnClear.setEnabled(false);
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.pnlEnvironmentVariableRegexTesterToolbar.btnTest.setEnabled(false);
        GuiUtils.tableSelectFirst(frmEnvironmentView.tblEnvironment);
    }

    public void initEventListeners() {
        /*
            Environment variable table row select
         */
        frmEnvironmentView.tblEnvironment.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            int rowId = frmEnvironmentView.tblEnvironment.getSelectedRow();
            if ( rowId >= 0 ) {
                boolean editable = !frmEnvironmentView.tblEnvironment.getValueAt(rowId, 1).toString().equals("BUILTIN");
                frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnDelete.setEnabled(editable);
                frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnSave.setEnabled(editable);
                frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnNew.setEnabled(true);
                frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.pnlEnvironmentVariableRegexTesterToolbar.btnTest.setEnabled(true);
                String environmentVarName = frmEnvironmentView.tblEnvironment.getValueAt(rowId, 3).toString();
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
        frmEnvironmentView.tblEnvironment.getModel().addTableModelListener(tableModelEvent -> {
            if ( tableModelEvent.getColumn() == 0 ) {
                TableModel model = (TableModel) tableModelEvent.getSource();
                environmentModel.getCurrentEnvironmentVariable().setEnabled((Boolean) model.getValueAt(tableModelEvent.getFirstRow(), tableModelEvent.getColumn()));
                environmentModel.getEnvironment().saveEnvironment();
            }
        });
        /*
            Test row select
         */
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.tblEnvVarTest.getModel().addTableModelListener(tableModelEvent -> {
            if ( frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.tblEnvVarTest.getModel().getRowCount() > 0 ) {
                frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.pnlEnvironmentVariableRegexTesterToolbar.btnClear.setEnabled(true);
            }
        });
        /*
            New variable
         */
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnNew.addActionListener(actionEvent -> {
            EnvironmentVariable newVar = new EnvironmentVariable();
            newVar.setName(String.format("Untitled%d", environmentModel.getEnvironment().getEnvironmentVariables().size()));
            newVar.setEnabled(false);
            newVar.setEnvironmentItemScope(EnvironmentItemScope.ALL);
            newVar.setEnvironmentItemType(EnvironmentItemType.VARIABLE_STRING_REPLACEMENT);
            environmentModel.getCurrentEnvironmentVariable().setEnvironmentVariable(newVar);
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.setEnabled(true);
            frmEnvironmentView.tblEnvironment.clearSelection();
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnDelete.setEnabled(false);
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnNew.setEnabled(false);
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnSave.setEnabled(true);
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.pnlEnvironmentVariableRegexTesterToolbar.btnTest.setEnabled(true);
        });
        /*
            Save variable
         */
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnSave.addActionListener(actionEvent -> {
            environmentModel.getCurrentEnvironmentVariable().setValidationIssues(validateEnvironmentVariable());
            if ( environmentModel.getCurrentEnvironmentVariable().getValidationIssues().size() == 0 ) {
                saveCurrentEnvironmentVariable();
                //updateSelectedVariable(currentEnvironmentVariable.getName());
            }
        });
        /*
            Delete variable
         */
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jbtnDelete.addActionListener(actionEvent -> {
            String deleteVar = frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.getText();
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
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.pnlEnvironmentVariableRegexTesterToolbar.btnTest.addActionListener(actionEvent -> {
            DefaultTableModel trafficModel = (DefaultTableModel) frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.tblEnvVarTest.getModel();
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

        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jcmbEnvItemType.addActionListener(actionEvent -> {
            environmentModel.getCurrentEnvironmentVariable().setEnvironmentItemType(frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.getSelectedEnvVarItemType());
        });

        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.pnlEnvironmentVariableRegexTesterToolbar.btnClear.addActionListener(actionEvent -> {
            DefaultTableModel model = (DefaultTableModel) frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentVariableRegexTester.tblEnvVarTest.getModel();
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
            currentEnvironmentVariable.setEnvironmentItemType(frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.getSelectedEnvVarItemType());
            currentEnvironmentVariable.setName(frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.getText());
            currentEnvironmentVariable.setDescription(frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtDescription.getText());

            // String variable
            currentEnvironmentVariable.setStringReplacementText(frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.jtxtStringReplacementText.getText());
            currentEnvironmentVariable.setStringReplacementMatchText(frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.jtxtStringReplacementMatch.getText());

            // Regex variable
            currentEnvironmentVariable.setMatchRegexGroup((Integer) frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jspnMatchRegexGroup.getValue());
            currentEnvironmentVariable.setRegexMatchGroupEnabled(frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jchkMatchGroup.isSelected());
            currentEnvironmentVariable.setRegexStringReplacementText(frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtRegexReplacementText.getText());
            currentEnvironmentVariable.setMatchRegexPattern(GuiUtils.getPattern(frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtMatchRegex.getText()));

            // Script variable
            currentEnvironmentVariable.setScriptMatchRegexGroup((Integer) frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jspnScriptMatchRegexGroup.getValue());
            if ( frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jcmbScripts.getSelectedItem() != null ) {
                currentEnvironmentVariable.setScriptName(frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jcmbScripts.getSelectedItem().toString());
            }

            currentEnvironmentVariable.setScriptMatchRegex(GuiUtils.getPattern(frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jtxtScriptMatchRegex.getText()));


            // Session variable
            currentEnvironmentVariable.setInputRegexMatchGroup((Integer) frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jspnInputMatchGroup.getValue());
            currentEnvironmentVariable.setOutputRegexMatchGroup((Integer) frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jspnOutputMatchGroup.getValue());
            currentEnvironmentVariable.setInputRegexPattern(GuiUtils.getPattern(frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexInput.getText()));
            currentEnvironmentVariable.setOutputRegexPattern(GuiUtils.getPattern(frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexOutput.getText()));
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
            for (int i = 0; i < frmEnvironmentView.tblEnvironment.getRowCount(); i++ ) {
                String varName = (String) frmEnvironmentView.tblEnvironment.getValueAt(i,3);
                if (varName.equals(envVar.getName())) {
                    frmEnvironmentView.tblEnvironment.setValueAt( envVar.isEnabled(),i,0);
                    frmEnvironmentView.tblEnvironment.setValueAt( envVar.getEnvironmentItemType().toString(),i,1);
                    frmEnvironmentView.tblEnvironment.setValueAt( envVar.getEnvironmentItemScope().toString(),i,2);
                    frmEnvironmentView.tblEnvironment.setValueAt( envVar.getName(),i,3);
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
                GuiUtils.tableSelectLast(frmEnvironmentView.tblEnvironment);
            }
        }

    }

    public void selectEditor() {
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.setVisible(false);
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.setVisible(false);
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.setVisible(false);
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.setVisible(false);
        EnvironmentVariable envVar = environmentModel.getCurrentEnvironmentVariable().getEnvironmentVariable();
        switch ( envVar.getEnvironmentItemType() ) {
            case VARIABLE_STRING_REPLACEMENT:
                frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.setVisible(true);
                break;
            case VARIABLE_REGEX_REPLACEMENT:
                frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.setVisible(true);
                break;
            case VARIABLE_SESSION:
                frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.setVisible(true);
                break;
            case VARIABLE_SCRIPT:
                frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.setVisible(true);
                break;
        }
    }

    /*
        Validates the fields in the UI
     */
    public ArrayList<String> validateEnvironmentVariable() {
        Pattern pattern;
        ArrayList<String> validationIssues = new ArrayList<>();
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.setBackground(Color.WHITE);
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtMatchRegex.setBackground(Color.WHITE);
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jtxtScriptMatchRegex.setBackground(Color.WHITE);
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexInput.setBackground(Color.WHITE);
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexOutput.setBackground(Color.WHITE);
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.jtxtStringReplacementText.setBackground(Color.WHITE);


        // Variable name - uniqueness
        String curVarName = frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.getText();
        EnvironmentVariable nameCheck = environmentModel.getEnvironment().getVariable(curVarName);
        if ( nameCheck != null ) {
            if ( !nameCheck.getId().equals(environmentModel.getCurrentEnvironmentVariable().getEnvironmentVariable().getId()) ) {
                validationIssues.add("The variable name must be unique");
                frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.setBackground(new Color(240, 128, 128));
            }
        }

        // Variable name - length
        if ( frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.getText().length() == 0 ) {
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.setBackground(new Color(240,128,128));
            validationIssues.add("The environment variable requires a name");
        }
        // Variable content
        if ( environmentModel.getCurrentEnvironmentVariable().getEnvironmentVariable() != null ) {
            switch (frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.getSelectedEnvVarItemType()) {
                case VARIABLE_STRING_REPLACEMENT:
                    if ( frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.jtxtStringReplacementText.getText().length() == 0 ) {
                        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.jtxtStringReplacementText.setBackground(new Color(240, 128, 128));
                        validationIssues.add("Replacement string required");
                    }
                    break;
                case VARIABLE_REGEX_REPLACEMENT:
                    if  ( frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtMatchRegex.getText().length() > 0 ) {
                        try {
                            pattern = Pattern.compile(frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtMatchRegex.getText());
                            // if using groups
                            if ( frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jchkMatchGroup.isSelected()) {
                                if ( GuiUtils.getMatchGroupCount(pattern) > 0 ) {
                                    if ( (Integer) frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jspnMatchRegexGroup.getValue() > GuiUtils.getMatchGroupCount(pattern) || GuiUtils.getMatchGroupCount(pattern) == 0 ) {
                                        validationIssues.add("The match target match group is not within range of the match groups provided by the regex");                                }
                                }
                                else {
                                    frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtMatchRegex.setBackground(Color.YELLOW);
                                    validationIssues.add("The match input regex needs a match group");
                                }
                            }
                            else {
                                // TODO
                            }
                        }
                        catch ( PatternSyntaxException e ) {
                            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtMatchRegex.setBackground(new Color(240,128,128));
                            validationIssues.add("Bad regex");
                        }
                    }
                    break;
                case VARIABLE_SESSION:
                    // The input pattern
                    if  ( frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexInput.getText().length() > 0 ) {
                        try {
                            pattern = Pattern.compile(frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexInput.getText());
                            if ( GuiUtils.getMatchGroupCount(pattern) > 0 ) {
                                if ( (Integer) frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jspnInputMatchGroup.getValue() > GuiUtils.getMatchGroupCount(pattern) || GuiUtils.getMatchGroupCount(pattern) == 0 ) {
                                    frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexInput.setBackground(Color.YELLOW);
                                    validationIssues.add("The input target match group is not within range of the match groups provided by the regex");
                                }
                            }
                            else {
                                frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexInput.setBackground(Color.YELLOW);
                                validationIssues.add("The input regex needs a match group");
                            }

                        }
                        catch ( PatternSyntaxException e ) {
                            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexInput.setBackground(new Color(240,128,128));
                            validationIssues.add("Bad regex");
                        }
                    }

                    // The output pattern
                    if ( frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexOutput.getText().length() > 0 ) {
                        try {
                            pattern = GuiUtils.getPattern(frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexOutput.getText());
                            if ( GuiUtils.getMatchGroupCount(pattern) > 0 ) {
                                if ( (Integer) frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jspnOutputMatchGroup.getValue() > GuiUtils.getMatchGroupCount(pattern) || GuiUtils.getMatchGroupCount(pattern) == 0 ) {
                                    frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexOutput.setBackground(Color.YELLOW);
                                    validationIssues.add("The output target match group is not within range of the match groups provided by the regex");
                                }
                            }
                            else {
                                frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexOutput.setBackground(Color.YELLOW);
                                validationIssues.add("The output regex needs a match group");
                            }
                        }
                        catch ( PatternSyntaxException e ) {
                            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexOutput.setBackground(new Color(240,128,128));
                            validationIssues.add("Bad regex");
                        }
                    }
                    break;
                case VARIABLE_SCRIPT:
                    if  ( frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jtxtScriptMatchRegex.getText().length() > 0 ) {
                        try {
                            pattern = Pattern.compile(frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jtxtScriptMatchRegex.getText());
                            if ( GuiUtils.getMatchGroupCount(pattern) > 0 ) {
                                if ( (Integer) frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jspnScriptMatchRegexGroup.getValue() > GuiUtils.getMatchGroupCount(pattern) || GuiUtils.getMatchGroupCount(pattern) == 0 ) {
                                    validationIssues.add("The match target match group is not within range of the match groups provided by the regex");                                }
                            }
                            else {
                                frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jtxtScriptMatchRegex.setBackground(Color.YELLOW);
                                validationIssues.add("The match input regex needs a match group");
                            }

                        }
                        catch ( PatternSyntaxException e ) {
                            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jtxtScriptMatchRegex.setBackground(new Color(240,128,128));
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
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.setText(envVar.getName());
        }

        if ( "EnvironmentVariableModel.description".equals(propertyChangeEvent.getPropertyName())) {
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtDescription.setText(envVar.getDescription());
        }

        if ( "EnvironmentVariableModel.enabled".equals(propertyChangeEvent.getPropertyName())) {
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.setEnabled(false);
        }
        if ( "EnvironmentVariableModel.environmentItemScope".equals(propertyChangeEvent.getPropertyName())) {
            GuiUtils.setComboBoxItem(frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jcmbEnvItemScope, envVar.getEnvironmentItemScope().toString());
        }
        if ( "EnvironmentVariableModel.environmentItemType".equals(propertyChangeEvent.getPropertyName())) {
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.selectVarTypeByEnumName(envVar.getEnvironmentItemType());
            selectEditor();
        }
        if ( "EnvironmentVariableModel.inputRegexMatchGroup".equals(propertyChangeEvent.getPropertyName())) {
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jspnInputMatchGroup.setValue(envVar.getInputRegexMatchGroup());
        }
        if ( "EnvironmentVariableModel.outputRegexMatchGroup".equals(propertyChangeEvent.getPropertyName())) {
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jspnOutputMatchGroup.setValue(envVar.getOutputRegexMatchGroup());
        }
        if ( "EnvironmentVariableModel.inputRegexPattern".equals(propertyChangeEvent.getPropertyName())) {
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexInput.setText(GuiUtils.getPatternString(envVar.getInputRegexPattern()));
        }
        if ( "EnvironmentVariableModel.outputRegexPattern".equals(propertyChangeEvent.getPropertyName())) {
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexOutput.setText(GuiUtils.getPatternString(envVar.getOutputRegexPattern()));
        }
        if ( "EnvironmentVariableModel.storedVariable".equals(propertyChangeEvent.getPropertyName())) {
        }
        if ( "EnvironmentVariableModel.regexStringReplacementText".equals(propertyChangeEvent.getPropertyName())) {
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.jtxtStringReplacementText.setText(envVar.getStringReplacementText());
        }
        if ( "EnvironmentVariableModel.stringReplacementMatchText".equals(propertyChangeEvent.getPropertyName())) {
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.jtxtStringReplacementMatch.setText(envVar.getStringReplacementMatchText());
        }
        if ( "EnvironmentVariableModel.matchRegexPattern".equals(propertyChangeEvent.getPropertyName())) {
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtMatchRegex.setText(GuiUtils.getPatternString(envVar.getMatchRegexPattern()));
        }
        if ( "EnvironmentVariableModel.matchRegexGroup".equals(propertyChangeEvent.getPropertyName())) {
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jspnMatchRegexGroup.setValue(envVar.getMatchRegexGroup());
        }
        if ( "EnvironmentVariableModel.regexMatchGroupEnabled".equals(propertyChangeEvent.getPropertyName())) {
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jchkMatchGroup.setSelected(envVar.getRegexMatchGroupEnabled());
        }
        if ( "EnvironmentVariableModel.regexStringReplacementText".equals(propertyChangeEvent.getPropertyName())) {
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtRegexReplacementText.setText(envVar.getRegexStringReplacementText());
        }
        if ( "EnvironmentVariableModel.scriptMatchRegex".equals(propertyChangeEvent.getPropertyName())) {
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jtxtScriptMatchRegex.setText(GuiUtils.getPatternString(envVar.getScriptMatchRegex()));
        }
        if ( "EnvironmentVariableModel.scriptName".equals(propertyChangeEvent.getPropertyName())) {
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jcmbScripts.setSelectedItem(envVar.getScriptName());
        }
        if ( "EnvironmentVariableModel.scriptMatchRegexGroup".equals(propertyChangeEvent.getPropertyName())) {
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jspnScriptMatchRegexGroup.setValue(envVar.getScriptMatchRegexGroup());
        }

        if ( "EnvironmentVariableModel.environmentVariable".equals(propertyChangeEvent.getPropertyName())) {
            loadEnvironmentVariable(envVar);
        }

        if ( "EnvironmentVariableModel.validationIssues".equals(propertyChangeEvent.getPropertyName())) {
            String errorStr = "";
            for ( String validationIssue : environmentModel.getCurrentEnvironmentVariable().getValidationIssues() ) {
                errorStr += String.format("%s\n", validationIssue);
            }
            frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.jtxtValidationIssues.setText(errorStr);
        }




    }
    public void loadEnvironmentVariable( EnvironmentVariable envVar) {
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.setText(envVar.getName());
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtDescription.setText(envVar.getDescription());
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jtxtEnvVarName.setEnabled(false);
        GuiUtils.setComboBoxItem(frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.jcmbEnvItemScope, envVar.getEnvironmentItemScope().toString());
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemEditor.selectVarTypeByEnumName(envVar.getEnvironmentItemType());
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jspnInputMatchGroup.setValue(envVar.getInputRegexMatchGroup());
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jspnOutputMatchGroup.setValue(envVar.getOutputRegexMatchGroup());
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexInput.setText(GuiUtils.getPatternString(envVar.getInputRegexPattern()));
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlSessionReplacement.jtxtRegexOutput.setText(GuiUtils.getPatternString(envVar.getOutputRegexPattern()));
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.jtxtStringReplacementText.setText(envVar.getStringReplacementText());
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlStringReplacement.jtxtStringReplacementMatch.setText(envVar.getStringReplacementMatchText());
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtMatchRegex.setText(GuiUtils.getPatternString(envVar.getMatchRegexPattern()));
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jspnMatchRegexGroup.setValue(envVar.getMatchRegexGroup());
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jchkMatchGroup.setSelected(envVar.getRegexMatchGroupEnabled());
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlRegexReplacement.jtxtRegexReplacementText.setText(envVar.getRegexStringReplacementText());
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jtxtScriptMatchRegex.setText(GuiUtils.getPatternString(envVar.getScriptMatchRegex()));
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jcmbScripts.setSelectedItem(envVar.getScriptName());
        frmEnvironmentView.pnlEnvironmentEditor.pnlEnvironmentItemVariableEditor.pnlScriptReplacement.jspnScriptMatchRegexGroup.setValue(envVar.getScriptMatchRegexGroup());
        selectEditor();
    }
}
