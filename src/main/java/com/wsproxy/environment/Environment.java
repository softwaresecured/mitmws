package com.wsproxy.environment;

import com.wsproxy.configuration.ApplicationConfig;
import com.wsproxy.httpproxy.HttpMessage;
import com.wsproxy.httpproxy.websocket.WebsocketFrame;
import com.wsproxy.httpproxy.websocket.WebsocketFrameType;
import com.wsproxy.mvc.model.InteractshModel;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Environment implements Serializable {

    private String envFile = null;
    private EnvironmentVariableSet environmentVariableSet = new EnvironmentVariableSet();
    private ApplicationConfig applicationConfig = new ApplicationConfig();
    private InteractshModel interactshModel;
    private String currentConversationId;

    public Environment() {
        loadBuiltIn();
    }

    // TODO: Not sure why this exists
    public Environment( boolean loadEnv)  {
        if ( loadEnv ) {
            loadBuiltIn();
        }
    }

    private void loadBuiltIn() {
        environmentVariableSet.addVariables(loadBuiltInVariables());
        envFile = String.format("%s/%s", applicationConfig.getConfigDirPath(),".env");
    }

    public void clearTemp() {
        loadEnvironment();
        ArrayList<String> tempVars = new ArrayList<String>();
        for ( EnvironmentVariable variable : environmentVariableSet.getEnvironmentVariables() ) {
            if ( variable.isTemporary() ) {
                tempVars.add(variable.getName());
            }
        }
        for ( String tempVar : tempVars ) {
            deleteVariable(tempVar);
        }
        saveEnvironment();
    }
    public void setInteractshModel( InteractshModel interactshModel ) {
        this.interactshModel = interactshModel;
    }

    public void setCurrentConversationId ( String currentConversationId ) {
        this.currentConversationId = currentConversationId;
    }

    public void reset() {
        environmentVariableSet.setEnvironmentVariables(new ArrayList<>());
    }

    /*
        These are helpful built in variables that are commonly used
        Might expand to include timestamps, a random string etc
        Files might be useful, like curl's -d '@/path/to/file'
     */


    public EnvironmentVariable getVarById(String id ) {
        EnvironmentVariable envVar = null;
        for ( EnvironmentVariable curVar : environmentVariableSet.getEnvironmentVariables() ) {
            if ( curVar.getId().equals(id)) {
                envVar = curVar;
            }
        }
        return envVar;
    }

    public static ArrayList<EnvironmentVariable> loadBuiltInVariables() {
        ArrayList<EnvironmentVariable> env = new ArrayList<>();
        EnvironmentVariable e = new EnvironmentVariable();
        e.setName("__RINT__");
        e.setDescription("A random integer");
        e.setEnvironmentItemScope(EnvironmentItemScope.ALL);
        e.setEnvironmentItemType(EnvironmentItemType.BUILTIN);
        env.add(e);

        e = new EnvironmentVariable();
        e.setName("__INTERACTSH_PAYLOAD__");
        e.setDescription("An interactsh payload");
        e.setEnvironmentItemScope(EnvironmentItemScope.ALL);
        e.setEnvironmentItemType(EnvironmentItemType.BUILTIN);
        env.add(e);

        e = new EnvironmentVariable();
        e.setName("__TIMESTAMP_SEC__");
        e.setDescription("The unix time in seconds");
        e.setEnvironmentItemScope(EnvironmentItemScope.ALL);
        e.setEnvironmentItemType(EnvironmentItemType.BUILTIN);
        env.add(e);

        e = new EnvironmentVariable();
        e.setName("__TIMESTAMP_MSEC__");
        e.setDescription("The unix time in milliseconds");
        e.setEnvironmentItemScope(EnvironmentItemScope.ALL);
        e.setEnvironmentItemType(EnvironmentItemType.BUILTIN);
        env.add(e);

        e = new EnvironmentVariable();
        e.setName("__UUID__");
        e.setDescription("A UUID");
        e.setEnvironmentItemScope(EnvironmentItemScope.ALL);
        e.setEnvironmentItemType(EnvironmentItemType.BUILTIN);
        env.add(e);

        e = new EnvironmentVariable();
        e.setName("__SEC_WEBSOCKET_KEY__");
        e.setDescription("A value used in the websocket handshake. It is 16 ASCII chars in the range of 32 to 127. It is hashed and base64'd.");
        e.setEnvironmentItemScope(EnvironmentItemScope.ALL);
        e.setEnvironmentItemType(EnvironmentItemType.BUILTIN);
        env.add(e);

        e = new EnvironmentVariable();
        e.setName("__DEFAULT_USER_AGENT__");
        e.setStringReplacementMatchText("__DEFAULT_USER_AGENT__");
        e.setStringReplacementText("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
        e.setDescription("The default user agent");
        e.setEnvironmentItemScope(EnvironmentItemScope.ALL);
        e.setEnvironmentItemType(EnvironmentItemType.VARIABLE_STRING_REPLACEMENT);
        env.add(e);
        return env;
    }

    public String[] getVarNames() {
        String[] varNames = new String[environmentVariableSet.getEnvironmentVariables().size()];
        for ( int i = 0; i < environmentVariableSet.getEnvironmentVariables().size(); i++ ) {
            varNames[i] = environmentVariableSet.getEnvironmentVariables().get(i).getName();
        }
        return varNames;
    }

    public void deleteVariable ( String varName ) {
        for ( int i = 0; i < environmentVariableSet.getEnvironmentVariables().size(); i++ ) {
            if ( environmentVariableSet.getEnvironmentVariables().get(i).getName().equals(varName)) {
                environmentVariableSet.getEnvironmentVariables().remove(i);
                break;
            }
        }
    }

    public void setVariable ( EnvironmentVariable environmentVariable ) {
        boolean updated = false;
        for ( int i = 0; i < environmentVariableSet.getEnvironmentVariables().size(); i++ ) {
            if ( environmentVariableSet.getEnvironmentVariables().get(i).getName().equals(environmentVariable.getName())) {
                environmentVariableSet.getEnvironmentVariables().set(i,environmentVariable);
                updated = true;
                break;
            }
        }
        if ( !updated ) {
            environmentVariableSet.getEnvironmentVariables().add(environmentVariable);
        }
    }

    public EnvironmentVariable getVariable(String varName ) {
        for ( EnvironmentVariable envVar : environmentVariableSet.getEnvironmentVariables() ) {
            if ( envVar.getName().equals(varName)) {
                return envVar;
            }
        }
        return null;
    }

    public void loadEnvironment() {
        try {
            byte[] envBytes = Files.readAllBytes(Paths.get(envFile));
            ByteArrayInputStream bis = new ByteArrayInputStream(envBytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            ois.setObjectInputFilter(ObjectInputFilter.Config.createFilter("com.wsproxy.environment.EnvironmentVariableSet"));
            Object obj = ois.readObject();
            environmentVariableSet = (EnvironmentVariableSet) obj;

        } catch (FileNotFoundException e) {
            ;
        } catch (IOException e) {
            ;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void saveEnvironment() {
        try {
            FileOutputStream logStream = new FileOutputStream(envFile);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = null;
            oos = new ObjectOutputStream(bos);
            oos.writeObject(environmentVariableSet);
            oos.flush();
            logStream.write(bos.toByteArray());
            logStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<EnvironmentVariable> getEnvironmentVariables() {
        return environmentVariableSet.getEnvironmentVariables();
    }

    public HttpMessage process (EnvironmentItemScope scope, HttpMessage msg ) {
        for ( EnvironmentVariable envVar : environmentVariableSet.getEnvironmentVariables() ) {
            if ( envVar.getEnvironmentItemScope().equals(scope) || envVar.getEnvironmentItemScope().equals(EnvironmentItemScope.ALL)) {
                msg = envVar.processHttp(interactshModel, currentConversationId, scope, msg.getCopy());
            }
        }
        return msg;
    }

    public WebsocketFrame process ( EnvironmentItemScope scope, WebsocketFrame frame ) {
        for ( EnvironmentVariable envVar : environmentVariableSet.getEnvironmentVariables() ) {
            if ( frame.getOpcode().equals(WebsocketFrameType.PING) || frame.getOpcode().equals(WebsocketFrameType.PONG) || frame.getOpcode().equals(WebsocketFrameType.TEXT)) {
                if ( envVar.getEnvironmentItemScope().equals(scope) || envVar.getEnvironmentItemScope().equals(EnvironmentItemScope.ALL)) {
                    frame = envVar.processWsFrame(interactshModel, currentConversationId, scope, frame);
                }
            }
        }
        return frame;
    }

    public byte[] process(byte[] buff) {
        for ( EnvironmentVariable envVar : environmentVariableSet.getEnvironmentVariables() ) {
            buff = envVar.processBytes(interactshModel, currentConversationId, buff);
        }
        return buff;
    }



}
