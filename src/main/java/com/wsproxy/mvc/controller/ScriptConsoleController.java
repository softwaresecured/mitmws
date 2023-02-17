package com.wsproxy.mvc.controller;

import com.wsproxy.integrations.python.Script;
import com.wsproxy.mvc.model.LogModel;
import com.wsproxy.mvc.model.ScriptConsoleModel;
import com.wsproxy.mvc.view.panels.logs.PnlLogs;
import com.wsproxy.mvc.view.panels.scriptconsole.PnlScriptConsole;

import javax.script.ScriptException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ScriptConsoleController implements PropertyChangeListener {

    private ScriptConsoleModel scriptConsoleModel;
    private PnlScriptConsole pnlScriptConsole;

    public ScriptConsoleController(ScriptConsoleModel scriptConsoleModel, PnlScriptConsole pnlScriptConsole) {
        this.scriptConsoleModel = scriptConsoleModel;
        this.pnlScriptConsole = pnlScriptConsole;
        this.scriptConsoleModel.addListener(this);
        this.scriptConsoleModel.setScriptContent("def main():\n\tprint(\"hello\")\n");
        initEventListeners();
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if ( "ScriptConsoleModel.scriptContent".equals(propertyChangeEvent.getPropertyName())) {
            pnlScriptConsole.jtxtScriptContent.setText((String) propertyChangeEvent.getNewValue());
        }
        if ( "ScriptConsoleModel.executionOutput".equals(propertyChangeEvent.getPropertyName())) {
            pnlScriptConsole.jtxtScriptOutput.setText((String) propertyChangeEvent.getNewValue());
        }
    }

    private void initEventListeners() {
        pnlScriptConsole.btnExecute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Script script = new Script();
                scriptConsoleModel.setScriptContent(pnlScriptConsole.jtxtScriptContent.getText());
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
