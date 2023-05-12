package com.wsproxy.anomalydetection;

import java.util.ArrayList;

public class DetectionRuleSelfTestStatus {
    private boolean selfTestOK = false;
    private ArrayList<String> selfTestErrors = new ArrayList<String>();
    public void DetectionRuleSelfTestStatus() {

    }

    public boolean isSelfTestOK() {
        return selfTestOK;
    }

    public void setSelfTestOK(boolean selfTestOK) {
        this.selfTestOK = selfTestOK;
    }

    public ArrayList<String> getSelfTestErrors() {
        return selfTestErrors;
    }

    public void addSelfTestError( String error ) {
        selfTestErrors.add(error);
    }
}
