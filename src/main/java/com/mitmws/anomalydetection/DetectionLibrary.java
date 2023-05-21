package com.mitmws.anomalydetection;

import com.mitmws.configuration.ApplicationConfig;
import com.mitmws.httpproxy.trafficlogger.WebsocketTrafficRecord;
import com.mitmws.httpproxy.websocket.WebsocketFrame;
import com.mitmws.logging.AppLog;
import com.mitmws.mvc.model.InteractShTestPayload;
import com.mitmws.mvc.model.InteractshModel;
import com.mitmws.util.ScriptUtil;
import jnr.ffi.annotations.In;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class DetectionLibrary {
    private DetectionType detectionType;
    private HashMap<Integer, DetectionRule> rules = null;
    private ApplicationConfig applicationConfig = new ApplicationConfig();
    private static Logger LOGGER = AppLog.getLogger(DetectionLibrary.class.getName());
    public DetectionLibrary( DetectionType detectionType ) {
        this.detectionType = detectionType;
        rules = new HashMap<>();
    }

    public int getEnabledRuleCount() {
        int count = 0;
        for ( int ruleId : rules.keySet() ) {
            if ( rules.get(ruleId).isEnabled() ) {
                count += 1;
            }
        }
        return count;
    }

    /*
        total number of payloads in this library
     */
    public int getEnabledPayloadCount() {
        InteractshModel interactshModel = new InteractshModel();
        interactshModel.setTestMode(true);
        int count = 0;
        for ( int ruleId : rules.keySet() ) {
            try {
                if ( rules.get(ruleId).isEnabled() && rules.get(ruleId).getTestScope().equals("APPLICATION")) {
                    if ( rules.get(ruleId).getActiveRuleType().equals("PAYLOAD") ) {
                        count += rules.get(ruleId).getPayloads().size();
                    }
                    if ( rules.get(ruleId).getActiveRuleType().equals("PAYLOAD-INTERACTSH") ) {
                        count += rules.get(ruleId).getOOBPayloads(interactshModel).size();
                    }
                }
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    public HashMap<Integer, DetectionRule> getRules() {
        return rules;
    }

    /*
        runTests argument determines if a test is run on loading of the rule
        the detector threads don't do self tests because the assumption is that they were already tested by the main
        thread
     */
    public void load(boolean runTests) {
        String scriptRuleType = "active";
        if ( detectionType.equals(DetectionType.PASSIVE)) {
            scriptRuleType = "passive";
        }
        ArrayList<String> scripts = ScriptUtil.getScriptsByType(String.format("rules/%s", scriptRuleType));
        for ( String script : scripts ) {
            try {
                if ( script.matches("^\\d+\\.jy$")) {
                    int ruleNo = Integer.parseInt(script.split("\\.")[0]);
                    String scriptFileName = String.format("%s/scripts/rules/%s/%s", applicationConfig.getConfigDirPath(),scriptRuleType, script);
                    DetectionRule rule = new DetectionRule(scriptFileName);

                    if ( runTests ) {
                        if ( testRule(ruleNo,rule, detectionType) ) {
                            rules.put(ruleNo,rule);
                        }
                    }
                    else {
                        rules.put(ruleNo,rule);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ScriptException e) {
                e.printStackTrace();
            }

        }
    }

    /*
        Runs tests on a rule
     */

    private boolean testRule ( int ruleNo, DetectionRule rule, DetectionType detectionType ) {
        boolean testOk = true;
        String ruleName = "";
        // If it has no name we can't write detailed errors about it
        try {
            String name = rule.getName();
            if ( name == null || name.length() == 0 ) {
                rule.setErrorFlag(true);
                return false;
            }
            ruleName = name;
        } catch (ScriptException e) {
            rule.setErrorFlag(true);
            return false;

        }


        try {
            // Test that the rule has all the functions that we're expecting for a proper active/passive rule

            // Applies to all rules
            String description = rule.getDescription();
            String category = rule.getCategory();
            String testScope = rule.getTestScope();
            ArrayList<String> frameScopes = rule.getFrameScope();

            ArrayList<WebsocketTrafficRecord> conversation = new ArrayList<WebsocketTrafficRecord>();
            ArrayList<ArrayList<WebsocketTrafficRecord>> conversations = new ArrayList<ArrayList<WebsocketTrafficRecord>>();

            // Analyze active
            if ( detectionType.equals(DetectionType.ACTIVE)) {
                rule.getDetectedAnodmaliesForSequences(conversations);
                String activeRuleType = rule.getActiveRuleType(); // active only
                double ratio;
                int range;
                String result;


                switch (activeRuleType) {
                    case "PAYLOAD":
                        ArrayList<String> payloads = rule.getPayloads();
                        break;
                    case "FUZZ-FRAME":
                        WebsocketFrame testFrame = new WebsocketFrame();
                        ratio = rule.getFuzzRatio();
                        range = rule.getFuzzRange();
                        result = rule.getPayloadMutationBySeed("test",1,1);
                        break;
                    case "FUZZ-PAYLOAD":
                        String payload = rule.getPayloadMutationBySeed("Test",1,1);
                        break;
                    case "FUZZ-FRAME-HEADER":
                        ratio = rule.getFuzzRatio();
                        range = rule.getFuzzRange();
                        result = rule.getFrameMutationBySeed("test",1,1,1);
                        break;
                    case "PAYLOAD-INTERACTSH":
                        InteractshModel interactshModel = new InteractshModel();
                        interactshModel.setTestMode(true);
                        ArrayList<InteractShTestPayload> oobPayloads = rule.getOOBPayloads(interactshModel);
                        break;
                    default:
                        break;
                }
            }
            else {
                rule.getDetectedAnomaliesForSequence(conversation);
            }

            // Finally run the self test
            DetectionRuleSelfTestStatus selfTestResults = rule.selfTest();
            if (!selfTestResults.isSelfTestOK()) {
                for (String error : selfTestResults.getSelfTestErrors()) {
                    LOGGER.severe(String.format("%d/%s - rule self test failed: %s", ruleNo, ruleName, error));
                }
            }
        } catch ( ClassCastException e ) {
            LOGGER.severe(String.format("%d/%s - rule test failed: %s", ruleNo, ruleName, e.getMessage()));
            testOk = false;
        } catch (ScriptException e) {
            LOGGER.severe(String.format("%d/%s - rule test failed: %s", ruleNo, ruleName, e.getMessage()));
            testOk = false;
        }
        if ( testOk ) {
            LOGGER.info(String.format("%d/%s - rule test OK", ruleNo, ruleName));
        }
        return testOk;
    }

    /*
        Runs all rules of a particular class on the records ( passive )
     */
    public ArrayList<DetectedAnomaly> detectAnomalies(ArrayList<WebsocketTrafficRecord> records, String testName ) {
        ArrayList<DetectedAnomaly> anomalies = new ArrayList<DetectedAnomaly>();
        for ( Integer ruleId : rules.keySet()) {
            DetectionRule rule = rules.get(ruleId);
            if ( rule.isEnabled() ) {
                ArrayList<DetectedAnomaly> detectedAnomalies = null;
                try {
                    detectedAnomalies = rule.getDetectedAnomaliesForSequence(records);
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
                if ( detectedAnomalies != null ) {
                    for ( DetectedAnomaly anomaly : detectedAnomalies ) {
                        anomaly.setDetector("Passive");
                        anomaly.setTestName(testName);
                    }
                    anomalies.addAll(detectedAnomalies);
                }
            }
        }
        return anomalies;
    }
    /*
        Run a specific rule on a group of conversations ( active )
     */
    public ArrayList<DetectedAnomaly> detectAnomalies(ArrayList<ArrayList<WebsocketTrafficRecord>> conversations, String testName, int ruleId ) {
        ArrayList<DetectedAnomaly> anomalies = null;
        DetectionRule rule = rules.get(ruleId);
        if ( rule != null ) {
            try {
                anomalies = rule.getDetectedAnodmaliesForSequences(conversations);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
            if ( anomalies != null ) {
                for ( DetectedAnomaly anomaly : anomalies ) {
                    anomaly.setDetector("Active");
                    anomaly.setTestName(testName);
                }
            }
        }
        return anomalies;
    }
}
