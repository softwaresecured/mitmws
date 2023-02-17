package com.wsproxy.environment;

import com.wsproxy.httpproxy.HttpMessage;
import com.wsproxy.httpproxy.HttpMessageParseException;
import com.wsproxy.httpproxy.websocket.WebsocketFrame;
import com.wsproxy.integrations.python.Script;
import com.wsproxy.integrations.python.ScriptManager;
import com.wsproxy.mvc.controller.InteractshController;
import com.wsproxy.mvc.model.InteractshModel;

import javax.script.ScriptException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnvironmentVariable implements Serializable {
    private String id;
    private String name = null;
    private String description = null;
    private boolean enabled = true;
    private EnvironmentItemScope environmentItemScope = null;
    private EnvironmentItemType environmentItemType = null;
    private boolean isTemporary = false;
    // session
    public int inputRegexMatchGroup = 1;
    public int outputRegexMatchGroup = 1;
    private Pattern inputRegexPattern = null;
    private Pattern outputRegexPattern = null;
    private String storedVariable = null;

    // string
    private String stringReplacementText = null;
    private String stringReplacementMatchText = null;

    // regex
    private Pattern matchRegexPattern = null;
    public int matchRegexGroup = 1;
    public boolean regexMatchGroupEnabled = false;
    private String regexStringReplacementText = null;

    // script
    private Pattern scriptMatchRegex = null;
    private String scriptName = null;
    private int scriptMatchRegexGroup = -1;

    public EnvironmentVariable() {
        id = UUID.randomUUID().toString();
    }


    public boolean isTemporary() {
        return isTemporary;
    }

    public void setTemporary(boolean temporary) {
        isTemporary = temporary;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "EnvironmentVariable{" +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", enabled=" + enabled +
                ", matchRegexPattern=" + matchRegexPattern +
                ", matchRegexGroup=" + matchRegexGroup +
                ", inputRegexMatchGroup=" + inputRegexMatchGroup +
                ", outputRegexMatchGroup=" + outputRegexMatchGroup +
                ", inputRegexPattern=" + inputRegexPattern +
                ", outputRegexPattern=" + outputRegexPattern +
                ", stringReplacementText='" + stringReplacementText + '\'' +
                ", regexStringReplacementText='" + regexStringReplacementText + '\'' +
                ", environmentItemScope=" + environmentItemScope +
                ", environmentItemType=" + environmentItemType +
                ", storedVariable='" + storedVariable + '\'' +
                '}';
    }

    public String getStringReplacementMatchText() {
        return stringReplacementMatchText;
    }

    public void setStringReplacementMatchText(String stringReplacementMatchText) {
        this.stringReplacementMatchText = stringReplacementMatchText;
    }

    public int getInputRegexMatchGroup() {
        return inputRegexMatchGroup;
    }

    public void setInputRegexMatchGroup(int inputRegexMatchGroup) {
        this.inputRegexMatchGroup = inputRegexMatchGroup;
    }

    public HttpMessage processHttp ( InteractshModel interactshModel, String conversationUUID, EnvironmentItemScope scope, HttpMessage msg ) {
        byte header[] = processBytes(interactshModel, conversationUUID, msg.getHeaderBytes());
        byte body[] = msg.getBodyBytes();
        try {
            msg.fromParts(header,body);
        } catch (HttpMessageParseException e) {
            ;//e.printStackTrace();
        }
        /*
            TODO:
                - Apply the environment to the body
                - Update headers accordingly
         */
        return msg;
    }

    public WebsocketFrame processWsFrame ( InteractshModel interactshModel, String conversationUUID, EnvironmentItemScope scope, WebsocketFrame frame ) {
        if ( frame.getPayloadUnmasked() != null ) {
            frame.setPayloadUnmasked(processBytes(interactshModel, conversationUUID, frame.getPayloadUnmasked()));
        }
        return frame;
    }

    public byte[] processBytes ( InteractshModel interactshModel, String conversationUUID, byte[] input ) {
        String inputStr = new String(input);
        if ( enabled ) {
            Matcher m;
            switch ( environmentItemType ) {

                case BUILTIN:
                    input = processBuiltin(interactshModel, conversationUUID, input);
                    break;
                case VARIABLE_STRING_REPLACEMENT:
                    input = new String(input).replaceAll(stringReplacementMatchText, stringReplacementText).getBytes();
                    break;
                case VARIABLE_SCRIPT:
                    ScriptManager scriptManager = new ScriptManager();
                    try {
                        String regexProcessedString = new String(input);
                        m = scriptMatchRegex.matcher(regexProcessedString);
                        Script script = scriptManager.getScript("variables", getScriptName());
                        if ( m.find() && m.groupCount() >= getScriptMatchRegexGroup()-1 ) {
                            StringBuilder sb = new StringBuilder();
                            String textIn = m.group(getScriptMatchRegexGroup());
                            //System.out.println(String.format("Script arguments: %s", textIn));
                            String textOut = (String) script.executeFunction("execute",new String[]{textIn});
                            sb.append(regexProcessedString, 0, m.start(getScriptMatchRegexGroup()));
                            sb.append(textOut);
                            sb.append(regexProcessedString.substring(m.end(getScriptMatchRegexGroup())));
                            regexProcessedString = sb.toString();
                        }
                        input = regexProcessedString.getBytes(StandardCharsets.UTF_8);
                    } catch (ScriptException e) {
                        e.printStackTrace();
                    }
                    break;
                case VARIABLE_REGEX_REPLACEMENT:
                    String regexProcessedString = new String(input);
                    // if using groups
                    m = matchRegexPattern.matcher(regexProcessedString);
                    if ( regexMatchGroupEnabled ) {
                        if ( m.find() && m.groupCount() >= getMatchRegexGroup()-1 ) {
                            StringBuilder sb = new StringBuilder();
                            //System.out.println(String.format("Extracting match group: %d - %d", m.start(getMatchRegexGroup()), m.end(getMatchRegexGroup())));
                            sb.append(regexProcessedString, 0, m.start(getMatchRegexGroup()));
                            sb.append(regexStringReplacementText);
                            sb.append(regexProcessedString.substring(m.end(getMatchRegexGroup())));
                            regexProcessedString = sb.toString();
                        }
                        input = regexProcessedString.getBytes(StandardCharsets.UTF_8);
                    }
                    else {
                        input = m.replaceAll(regexStringReplacementText).getBytes(StandardCharsets.UTF_8);
                    }
                    break;
                case VARIABLE_SESSION:
                    // Get
                    if ( inputRegexPattern != null ) {
                        m = inputRegexPattern.matcher(inputStr);
                        if ( m.find() ) {
                            if ( m.groupCount() >= inputRegexMatchGroup) {
                                storedVariable = inputStr.substring(m.start(inputRegexMatchGroup),m.end(inputRegexMatchGroup));
                            }
                        }
                    }

                    // Set
                    if ( outputRegexPattern != null && storedVariable != null  ) {
                        m = outputRegexPattern.matcher(inputStr);
                        if ( m.find() ) {
                            if ( m.groupCount() >= 1 ) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(inputStr, 0, m.start(outputRegexMatchGroup));
                                sb.append(storedVariable);
                                sb.append(inputStr.substring(m.end(outputRegexMatchGroup)));
                                input = sb.toString().getBytes();
                            }
                        }
                    }
                    break;
                default:
            }
        }
        return input;
    }

    public byte[] processBuiltin( InteractshModel interactshModel, String conversationUUID, byte[] input ) {
        if ( input != null ) {
            String inputStr = new String(input);
            if ( inputStr.contains("__RINT__")) {
                if ( enabled ) {
                    Random r = new Random();
                    int rint = 10000000 + r.nextInt(89999999);
                    inputStr = inputStr.replaceAll("__RINT__", String.format("%d", rint));
                }
            }
            if ( inputStr.contains("__UUID__")) {
                if ( enabled ) {
                    String uuid = UUID.randomUUID().toString();
                    inputStr = inputStr.replaceAll("__UUID__",uuid);
                }
            }

            if ( inputStr.contains("__TIMESTAMP_SEC__")) {
                if ( enabled ) {
                    inputStr = inputStr.replaceAll("__TIMESTAMP_SEC__", String.format("%d", System.currentTimeMillis()/1000));
                }
            }

            if ( inputStr.contains("__TIMESTAMP_MSEC__")) {
                if ( enabled ) {
                    inputStr = inputStr.replaceAll("__TIMESTAMP_SEC__", String.format("%d", System.currentTimeMillis()));
                }
            }

            if ( inputStr.contains("__INTERACTSH_PAYLOAD__")) {
                if ( enabled ) {
                    if ( conversationUUID != null && interactshModel != null ) {
                        String interactShPayload = interactshModel.getPayload();
                        interactshModel.associatePayload(interactShPayload,conversationUUID);
                        inputStr = inputStr.replaceAll("__INTERACTSH_PAYLOAD__",interactShPayload);
                    }
                }
            }

            if ( inputStr.contains("__DEFAULT_USER_AGENT__")) {
                if ( enabled ) {
                    String defaultUa = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Edg/91.0.864.59";
                    inputStr = inputStr.replaceAll("__DEFAULT_USER_AGENT__",defaultUa);
                }
            }
            if ( inputStr.contains("__SEC_WEBSOCKET_KEY__")) {
                if ( enabled ) {
                    String uuid = UUID.randomUUID().toString();
                    // 16 chars ASCII 32 to 127
                    Random r = new Random();
                    byte[] websocketKeyBytes = new byte[16];
                    byte[] wsKey = null;
                    for ( int i = 0 ; i < websocketKeyBytes.length; i++ ) {
                        websocketKeyBytes[i] = (byte) (r.nextInt(127-32)+32);
                    }
                    wsKey = Base64.getEncoder().encode(websocketKeyBytes);
                    inputStr = inputStr.replaceAll("__SEC_WEBSOCKET_KEY__",new String(wsKey));
                }
            }
            input = inputStr.getBytes();
        }
        return input;
    }

    public Pattern getMatchRegexPattern() {
        return matchRegexPattern;
    }

    public void setMatchRegexPattern(Pattern matchRegexPattern) {
        this.matchRegexPattern = matchRegexPattern;
    }

    public Pattern getInputRegexPattern() {
        return inputRegexPattern;
    }

    public String getStringReplacementText() {
        return stringReplacementText;
    }

    public void setStringReplacementText(String stringReplacementText) {
        this.stringReplacementText = stringReplacementText;
    }

    public void setInputRegexPattern(Pattern inputRegexPattern) {
        this.inputRegexPattern = inputRegexPattern;
    }

    public Pattern getOutputRegexPattern() {
        return outputRegexPattern;
    }

    public void setOutputRegexPattern(Pattern outputRegexPattern) {
        this.outputRegexPattern = outputRegexPattern;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public EnvironmentItemScope getEnvironmentItemScope() {
        return environmentItemScope;
    }

    public void setEnvironmentItemScope(EnvironmentItemScope environmentItemScope) {
        this.environmentItemScope = environmentItemScope;
    }

    public EnvironmentItemType getEnvironmentItemType() {
        return environmentItemType;
    }

    public void setEnvironmentItemType(EnvironmentItemType environmentItemType) {
        this.environmentItemType = environmentItemType;
    }

    public int getOutputRegexMatchGroup() {
        return outputRegexMatchGroup;
    }

    public void setOutputRegexMatchGroup(int outputRegexMatchGroup) {
        this.outputRegexMatchGroup = outputRegexMatchGroup;
    }

    public int getMatchRegexGroup() {
        return matchRegexGroup;
    }

    public void setMatchRegexGroup(int matchRegexGroup) {
        this.matchRegexGroup = matchRegexGroup;
    }

    public String getStoredVariable() {
        return storedVariable;
    }

    public void setStoredVariable(String storedVariable) {
        this.storedVariable = storedVariable;
    }

    public String getRegexStringReplacementText() {
        return regexStringReplacementText;
    }

    public void setRegexStringReplacementText(String regexStringReplacementText) {
        this.regexStringReplacementText = regexStringReplacementText;
    }

    public Pattern getScriptMatchRegex() {
        return scriptMatchRegex;
    }

    public void setScriptMatchRegex(Pattern scriptMatchRegex) {
        this.scriptMatchRegex = scriptMatchRegex;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public int getScriptMatchRegexGroup() {
        return scriptMatchRegexGroup;
    }

    public void setScriptMatchRegexGroup(int scriptMatchRegexGroup) {
        this.scriptMatchRegexGroup = scriptMatchRegexGroup;
    }

    public void setRegexMatchGroupEnabled(boolean status ) {
        regexMatchGroupEnabled = status;
    }

    public boolean getRegexMatchGroupEnabled() {
        return regexMatchGroupEnabled;
    }

}
