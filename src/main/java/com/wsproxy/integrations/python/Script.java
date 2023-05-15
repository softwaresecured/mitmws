package com.wsproxy.integrations.python;
import org.python.core.Options;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Script {
    private ScriptEngineManager manager;
    private ScriptEngine engine;
    private Invocable invocable;
    private String scriptFileName = null;

    // manual config
    public Script() {

    }

    // Used when invoking from filename, will figure out what interface to use based on extension
    public Script(String scriptFileName ) throws ScriptException {
        if ( scriptFileName == null ) {
            throw new ScriptException("Script name is null");
        }
        else {
            if ( scriptFileName.endsWith(".jy")) {
                loadJythonScript(scriptFileName);
            }
            else if ( scriptFileName.endsWith(".py")) {
                loadPythonScript(scriptFileName);
            }
            else {
                throw new ScriptException("Unrecognized file format");
            }
        }
    }


    public void prepareJython( String jythonContent ) throws ScriptException {
        Options.importSite = false;
        manager = new ScriptEngineManager();
        engine = manager.getEngineByName("python");
        engine.eval(jythonContent);
        invocable = (Invocable) engine;
    }

    /*
        A wrapper for ScriptUtil.runScript
     */
    public void loadPythonScript(String scriptFileName) throws ScriptException {
        Options.importSite = false;
        manager = new ScriptEngineManager();
        engine = manager.getEngineByName("python");
        engine.eval(String.format("from com.wsproxy.util import ScriptUtil\n" +
                "def execute(*args):\n" +
                "    return ScriptUtil.runScript(\"%s\", args)\n", scriptFileName));
        invocable = (Invocable) engine;
        this.scriptFileName = scriptFileName;
    }
    public void loadJythonScript(String scriptFileName) throws ScriptException {
        try {
            Options.importSite = false;
            manager = new ScriptEngineManager();
            engine = manager.getEngineByName("python");
            engine.eval(Files.newBufferedReader(Paths.get(scriptFileName), StandardCharsets.UTF_8));
            invocable = (Invocable) engine;
            this.scriptFileName = scriptFileName;
        } catch (IOException e) {
            e.printStackTrace();
            throw new ScriptException(e.getMessage());
        }
    }

    public void evalJython( String jythonContent ) throws ScriptException {
        Options.importSite = false;
        manager = new ScriptEngineManager();
        engine = manager.getEngineByName("python");
        engine.eval(jythonContent);
        invocable = (Invocable) engine;
    }

    public Object executeFunction ( String functionName, Object... args ) throws ScriptException {
        Object result = null;
        try {
            result = invocable.invokeFunction(functionName, args);
        } catch (NoSuchMethodException e) {
            throw new ScriptException(String.format("Can't call %s - not defined", functionName));
        }
        return result;
    }

    public String getScriptFileName() {
        return scriptFileName;
    }
}
