package com.wsproxy.anomalydetection;

import com.wsproxy.Main;
import com.wsproxy.configuration.ApplicationConfig;
import com.wsproxy.httpproxy.trafficlogger.WebsocketTrafficRecord;
import com.wsproxy.logging.AppLog;
import com.wsproxy.util.ScriptUtil;

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
        int count = 0;
        for ( int ruleId : rules.keySet() ) {
            if ( rules.get(ruleId).isEnabled() && rules.get(ruleId).getTestScope().equals("APPLICATION") && rules.get(ruleId).getActiveRuleType().equals("PAYLOAD")) {
                count += rules.get(ruleId).getPayloads().size();
            }
        }
        return count;
    }

    public HashMap<Integer, DetectionRule> getRules() {
        return rules;
    }

    /*
        runSelfTest argument determines if a test is run on loading of the rule
        the detector threads don't do self tests because the assumption is that they were already tested by the main
        thread
     */
    public void load(boolean runSelfTest) {
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
                    if ( runSelfTest ) {
                        DetectionRuleSelfTestStatus result = rule.selfTest();
                        if ( !result.isSelfTestOK() ) {
                            for ( String error : result.getSelfTestErrors()) {
                                LOGGER.severe(String.format("%d/%s - rule self test failed: %s", ruleNo, rule.getName(),error));
                            }
                        }
                        else {
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
        Runs all rules of a particular class on the records ( passive )
     */
    public ArrayList<DetectedAnomaly> detectAnomalies(ArrayList<WebsocketTrafficRecord> records, String testName ) {
        ArrayList<DetectedAnomaly> anomalies = new ArrayList<DetectedAnomaly>();
        for ( Integer ruleId : rules.keySet()) {
            DetectionRule rule = rules.get(ruleId);
            if ( rule.isEnabled() ) {
                ArrayList<DetectedAnomaly> detectedAnomalies = rule.getDetectedAnomaliesForSequence(records);
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
            anomalies = rule.getDetectedAnodmaliesForSequences(conversations);
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
