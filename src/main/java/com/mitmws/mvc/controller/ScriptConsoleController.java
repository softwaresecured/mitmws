package com.mitmws.mvc.controller;

import com.mitmws.integrations.python.Script;
import com.mitmws.mvc.model.ScriptConsoleModel;
import com.mitmws.mvc.view.frames.FrmScriptConsole;

import javax.script.ScriptException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ScriptConsoleController implements PropertyChangeListener {

    private ScriptConsoleModel scriptConsoleModel;
    private FrmScriptConsole frmScriptConsole;

    public ScriptConsoleController(ScriptConsoleModel scriptConsoleModel, FrmScriptConsole frmScriptConsole) {
        this.scriptConsoleModel = scriptConsoleModel;
        this.frmScriptConsole = frmScriptConsole;
        this.scriptConsoleModel.addListener(this);
        this.scriptConsoleModel.setScriptContent("def main():\n\tprint(\"hello\")\n");
        initEventListeners();
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if ( "ScriptConsoleModel.scriptContent".equals(propertyChangeEvent.getPropertyName())) {
            frmScriptConsole.jtxtScriptContent.setText((String) propertyChangeEvent.getNewValue());
        }
        if ( "ScriptConsoleModel.executionOutput".equals(propertyChangeEvent.getPropertyName())) {
            frmScriptConsole.jtxtScriptOutput.setText((String) propertyChangeEvent.getNewValue());
        }
    }

    private void initEventListeners() {
        frmScriptConsole.btnExecute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Script script = new Script();
                scriptConsoleModel.setScriptContent(frmScriptConsole.jtxtScriptContent.getText());
                try {
                    script.evalJython(scriptConsoleModel.getScriptContent());
                    String result = (String) script.executeFunction("main",null);
                    if ( result != null ) {
                        scriptConsoleModel.setExecutionOutput(result);
                    }
                    else {
                        scriptConsoleModel.setExecutionOutput("");
                    }
                } catch (ScriptException ex) {
                    StringBuilder sb = new StringBuilder();
                    for ( StackTraceElement stackTraceElement : ex.getStackTrace()) {
                        sb.append(String.format("\t%s\n", stackTraceElement.toString()));
                    }
                    scriptConsoleModel.setExecutionOutput(String.format("%s\n%s", ex.getMessage(),sb.toString()));
                }
            }
        });
    }
}
