package com.wsproxy.mvc.model;

import com.wsproxy.anomalydetection.DetectionLibrary;
import com.wsproxy.anomalydetection.DetectionRule;
import com.wsproxy.anomalydetection.DetectionType;

import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.table.DefaultTableModel;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

public class RulesModel {
    private DefaultTableModel detectionRuleTableModel;
    private SwingPropertyChangeSupport eventEmitter;
    private DetectionLibrary passiveRules = new DetectionLibrary(DetectionType.PASSIVE);
    private DetectionLibrary activeRules = new DetectionLibrary(DetectionType.ACTIVE);
    private int enabledRuleCount = 0;
    public RulesModel() {
        detectionRuleTableModel = new DefaultTableModel();
        for ( String col: new String[] { "Enabled", "ID", "Detector","Category", "Rule name", "Description"}) {
            detectionRuleTableModel.addColumn(col);
        }
        eventEmitter = new SwingPropertyChangeSupport(this);
    }


    // rules have payloads
    public int getEnabledPayloadCount() {
        int payloadCount = activeRules.getEnabledPayloadCount();
        return payloadCount;
    }

    // count payloads by rule type

    public void updateEnabledRuleCount() {
        enabledRuleCount = passiveRules.getEnabledRuleCount() + activeRules.getEnabledRuleCount();
        setEnabledRuleCount(enabledRuleCount);
    }

    public void setEnabledRuleCount(int enabledRuleCount) {
        this.enabledRuleCount = enabledRuleCount;
        eventEmitter.firePropertyChange("RulesModel.enabledRuleCount", null, enabledRuleCount);
    }

    public int getEnabledRuleCount() {
        return enabledRuleCount;
    }

    public DetectionLibrary getPassiveRules() {
        return passiveRules;
    }

    public DetectionLibrary getActiveRules() {
        return activeRules;
    }

    public void reloadAnomalyRules( boolean disabled ) {
        detectionRuleTableModel.setRowCount(0);
        passiveRules = new DetectionLibrary(DetectionType.PASSIVE);
        activeRules = new DetectionLibrary(DetectionType.ACTIVE);
        passiveRules.load();
        activeRules.load();
        if ( disabled ) {
            for ( Integer ruleId: passiveRules.getRules().keySet() ) {
                passiveRules.getRules().get(ruleId).setEnabled(false);
            }
            for ( Integer ruleId: activeRules.getRules().keySet() ) {
                activeRules.getRules().get(ruleId).setEnabled(false);
            }
        }
        addRules(passiveRules.getRules(),"Passive");
        addRules(activeRules.getRules(),"Active");
        eventEmitter.firePropertyChange("RulesModel.detectionRuleTableModel", null, null);
    }

    public void reloadAnomalyRules() {
        reloadAnomalyRules(true);
    }
    public void addRules(HashMap<Integer, DetectionRule> rules, String detector ) {
        // "Enabled", "ID", "Detector","Rule name", "Description"
        for ( Integer ruleId : rules.keySet()) {
            detectionRuleTableModel.addRow(new Object[]{
                    rules.get(ruleId).isEnabled(),
                    ruleId,
                    detector,
                    rules.get(ruleId).getCategory(),
                    rules.get(ruleId).getName(),
                    rules.get(ruleId).getDescription()
            });
        }
    }
    public DefaultTableModel getDetectionRuleTableModel() {
        return detectionRuleTableModel;
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
