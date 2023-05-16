package com.mitmws.mvc.controller;

import com.mitmws.anomalydetection.DetectionRule;
import com.mitmws.configuration.ApplicationConfig;
import com.mitmws.mvc.model.RulesModel;
import com.mitmws.mvc.view.frames.FrmRulesView;
import com.mitmws.util.GuiUtils;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

public class RulesController implements PropertyChangeListener {

    private RulesModel rulesModel;
    private FrmRulesView frmRulesView;

    public RulesController(RulesModel rulesModel, FrmRulesView frmRulesView) {
        this.rulesModel = rulesModel;
        this.frmRulesView = frmRulesView;
        this.rulesModel.addListener(this);
        initEventListeners();
        rulesModel.reloadAnomalyRules(true);
        GuiUtils.tableSelectFirst(frmRulesView.jtblDetectionRules);
        enabledDefaultRules();
    }

    public void enabledDefaultRules() {


        ApplicationConfig applicationConfig = new ApplicationConfig();
        enableRulesCsv(applicationConfig.getProperty("rules.enabled_active"));
        enableRulesCsv(applicationConfig.getProperty("rules.enabled_passive"));
    }

    private void enableRulesCsv( String csv ) {
        int rules[] = GuiUtils.stringCsvToIntCsv(csv);
        for (int i = 0; i < frmRulesView.jtblDetectionRules.getModel().getRowCount(); i++ ) {
            int ruleId = (int) frmRulesView.jtblDetectionRules.getModel().getValueAt(i,1);
            for ( int j = 0; j < rules.length; j++ ) {
                if ( ruleId == rules[j]) {
                    frmRulesView.jtblDetectionRules.getModel().setValueAt(true,i,0);
                }
            }
        }
    }

    public void initEventListeners() {
        frmRulesView.jtblDetectionRules.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if ( e.getFirstRow() >= 0 && rulesModel.getDetectionRuleTableModel().getRowCount() > 0 ) {
                    Integer ruleId = (Integer) rulesModel.getDetectionRuleTableModel().getValueAt(e.getFirstRow(),1);
                    boolean enabled = (boolean) rulesModel.getDetectionRuleTableModel().getValueAt(e.getFirstRow(),0);
                    if ( ruleId != null ) {
                        HashMap<Integer,DetectionRule> activeRules = rulesModel.getActiveRules().getRules();
                        HashMap<Integer,DetectionRule> passiveRules = rulesModel.getPassiveRules().getRules();
                        if ( activeRules.get(ruleId) != null ) {
                            activeRules.get(ruleId).setEnabled(enabled);
                        }
                        else if ( passiveRules.get(ruleId) != null ) {
                            passiveRules.get(ruleId).setEnabled(enabled);
                        }
                        rulesModel.updateEnabledRuleCount();
                    }
                }
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if ( "RulesModel.detectionRuleTableModel".equals(propertyChangeEvent.getPropertyName())) {

        }
    }
}
