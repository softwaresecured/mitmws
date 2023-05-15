package com.wsproxy.anomalydetection;

import com.wsproxy.httpproxy.websocket.WebsocketFrame;
import com.wsproxy.httpproxy.trafficlogger.WebsocketTrafficRecord;
import com.wsproxy.integrations.python.Script;
import com.wsproxy.integrations.python.ScriptManager;
import com.wsproxy.mvc.model.InteractShTestPayload;
import com.wsproxy.mvc.model.InteractshModel;
import com.wsproxy.tester.RawWebsocketFrame;
import org.python.core.PyList;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.ArrayList;

public class DetectionRule {
    private String rulePath = null;
    private Script script;
    private boolean enabled = true;
    private boolean hasErrors = false;
    public DetectionRule( String rulePath  ) throws IOException, ScriptException {
        this.rulePath = rulePath;
        ScriptManager scriptManager = new ScriptManager();
        script = scriptManager.getScript(rulePath);
    }

    public boolean isErrorFlagged() {
        return hasErrors;
    }

    public void setErrorFlag(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /*
            Gets the friendly name of the script
         */
    public String getName() throws ScriptException {
        String result = null;
        result = (String) script.executeFunction("getName", null);
        return result;
    }

    public String getCategory() throws ScriptException {
        String result = null;
        result = (String) script.executeFunction("getCategory", null);
        return result;
    }
    public String getTestScope() throws ScriptException {
        String result = null;
        result = (String) script.executeFunction("getTestScope", null);
        return result;
    }

    /*
        Rule types can be:
            - PAYLOAD
            - PAYLOAD-FUZZ
            - FUZZ-FRAME
            - FRAME-CREATOR
        * applies only to active rules
     */
    public String getActiveRuleType() throws ScriptException {
        String result = null;
        result = (String) script.executeFunction("getActiveRuleType", null);
        return result;
    }

    public ArrayList<String> getFrameScope() throws ScriptException {
        ArrayList<String> frameScopes = new ArrayList<String>();
        PyList result = (PyList) script.executeFunction("getFrameScope");
        if ( result != null ) {
            for ( Object obj : result.stream().toArray() ) {
                frameScopes.add((String)obj);
            }
        }
        return frameScopes;
    }

    public int getFuzzRange() throws ScriptException {
        int result = 1;
        result = (int) script.executeFunction("getFuzzRange", null);
        return result;
    }

    public double getFuzzRatio() throws ScriptException {
        double result = 0.0;
        result = (double) script.executeFunction("getFuzzRatio", null);
        return result;
    }

    /*
        Gets the description of the script
     */
    public String getDescription() throws ScriptException {
        String result = null;
        result = (String) script.executeFunction("getDescription", null);
        return result;
    }
    /*
        Gets the test payloads for the rule
     */
    public ArrayList<String> getPayloads() throws ScriptException {
        ArrayList<String> payloads = new ArrayList<String>();
        PyList result = (PyList) script.executeFunction("getPayloads");
        if ( result != null ) {
            for ( Object obj : result.stream().toArray() ) {
                payloads.add((String)obj);
            }
        }
        return payloads;
    }
    /*
        Gets interactsh payloads
     */
    public ArrayList<InteractShTestPayload> getOOBPayloads(InteractshModel interactshModel) throws ScriptException {
        ArrayList<InteractShTestPayload> payloads = new ArrayList<InteractShTestPayload>();
        PyList result = (PyList) script.executeFunction("getOOBPayloads", interactshModel);
        if ( result != null ) {
            for ( Object obj : result.stream().toArray() ) {
                payloads.add((InteractShTestPayload)obj);
            }
        }
        return payloads;
    }

    /*
        Gets anomalies detected by this rule for the given sequence
        This would be run after a test has completed
     */
    public ArrayList<DetectedAnomaly> getDetectedAnomaliesForSequence(ArrayList<WebsocketTrafficRecord> sequence ) throws ScriptException {
        ArrayList<DetectedAnomaly> detectedAnomalies = new ArrayList<DetectedAnomaly>();
        detectedAnomalies = (ArrayList<DetectedAnomaly>) script.executeFunction("analyze", sequence);
        return detectedAnomalies;
    }

    /*
        Gets a list of frames prepared by this rule
     */
    public ArrayList<RawWebsocketFrame> getMutations(WebsocketFrame frame ) throws ScriptException {
        ArrayList<RawWebsocketFrame> frames = new ArrayList<RawWebsocketFrame>();
        PyList result = (PyList) script.executeFunction("getMutations", frame);
        if ( result != null ) {
            for ( Object obj : result.stream().toArray() ) {
                frames.add((RawWebsocketFrame)obj);
            }
        }
        return frames;
    }

    /*
        Gets a frame mutation for a given seed and ratio ( for fuzzers like zzuf etc )
     */
    public RawWebsocketFrame getFrameMutationBySeed(String frame, int seed, double ratio ) throws ScriptException {
        RawWebsocketFrame result = (RawWebsocketFrame) script.executeFunction("getFrameMutationBySeed", frame, seed, ratio );
        if ( result != null ) {
            return result;
        }
        return null;
    }

    /*
        Accepts a payload as a hex string and retuns a hex string of the fuzzed payload
     */
    public String getPayloadMutationBySeed(String payload, int seed, double ratio ) throws ScriptException {
        String result = (String) script.executeFunction("getPayloadMutationBySeed", payload, seed, ratio );
        if ( result != null ) {
            return result;
        }
        return null;
    }

    /*
        Accepts a payload as a hex string and retuns a hex string of the fuzzed payload
        This accounts for the length of the payload so it only fuzzes the first few bytes
    */
    public String getFrameMutationBySeed(String payload, int seed, double ratio, int payloadLength ) throws ScriptException {
        String result = (String) script.executeFunction("getFrameMutationBySeed", payload, seed, ratio, payloadLength );
        if ( result != null ) {
            return result;
        }
        return null;
    }

    /*
    Gets anomalies detected by this rule for the given sequence
    This would be run after a test has completed
 */
    public ArrayList<DetectedAnomaly> getDetectedAnodmaliesForSequences(ArrayList<ArrayList<WebsocketTrafficRecord>> sequences ) throws ScriptException {
        ArrayList<DetectedAnomaly> detectedAnomalies = new ArrayList<DetectedAnomaly>();
        detectedAnomalies = (ArrayList<DetectedAnomaly>) script.executeFunction("analyze", sequences);
        return detectedAnomalies;
    }

    /*
        logic based blind tests
     */

    public ArrayList<DetectedAnomaly> tautologyAnomalies( ArrayList<WebsocketTrafficRecord> controlRun, ArrayList<WebsocketTrafficRecord> trueRun, ArrayList<WebsocketTrafficRecord> falseRun ) {
        return null;
    }

    /*
        time based blind tests
     */
    public ArrayList<DetectedAnomaly> getTimeBasedAnomalies(ArrayList<WebsocketTrafficRecord> controlRun, ArrayList<WebsocketTrafficRecord> testRuns ) {
        return null;
    }

    /*
        Makes the assumption that a typical payload can be broken into 3 parts - starter, payload, ender
        starters and ender array sizes must match
     */

    /*
        Gets the beginning of the payload
     */
    public ArrayList<String> getPayloadStarter() {

        return null;
    }
    /*
    Gets the beginning of the payload
    */
    public ArrayList<String> getPayload() {
        return null;
    }
    /*
    Gets the beginning of the payload
    */
    public ArrayList<String> getPayloadEnder() {

        return null;
    }

    /*
        Runs the rule self test
     */
    public DetectionRuleSelfTestStatus selfTest() throws ScriptException {
        DetectionRuleSelfTestStatus result = (DetectionRuleSelfTestStatus) script.executeFunction("selfTest", null );
        if ( result != null ) {
            return result;
        }
        return null;
    }


}