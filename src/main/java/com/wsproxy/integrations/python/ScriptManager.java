package com.wsproxy.integrations.python;
import com.wsproxy.configuration.ApplicationConfig;
import javax.script.ScriptException;
import java.io.Serializable;

public class ScriptManager implements Serializable {
    public void ScriptManager() {

    }
    public Script getScript(String scriptType, String scriptName ) throws ScriptException {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        String scriptFileName = String.format("%s/scripts/%s/%s", applicationConfig.getConfigDirPath(),scriptType,scriptName);
        return getScript(scriptFileName);
    }

    public Script getScript( String fullPath ) throws ScriptException {
        return new Script(fullPath);
    }

}
