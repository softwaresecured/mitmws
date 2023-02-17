package com.wsproxy.tester;

import com.wsproxy.httpproxy.HttpMessage;
import com.wsproxy.httpproxy.HttpMessageParseException;
import com.wsproxy.integrations.python.Script;
import com.wsproxy.integrations.python.ScriptManager;

import javax.script.ScriptException;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;

public class TestSequence implements Serializable {
    private int id = -1;
    private String upgradeHelperScript = null;
    private String eventScript = null;
    private HttpMessage httpMessage = null; // The upgrade request
    private ArrayList<TestSequenceItem> testSequenceItems = null; // The sequence of steps once connected
    private ArrayList<TestTarget> testTargets = null;
    private ScriptManager scriptManager = null;
    public TestSequence( ScriptManager scriptManager ) {
        reset();
        this.scriptManager = scriptManager;
    }
    public TestSequence() {
        reset();
        scriptManager = new ScriptManager();
    }
    public TestSequence(int id, String upgradeHelperScript, HttpMessage httpMessage, ArrayList<TestSequenceItem> testSequenceItems, ArrayList<TestTarget> testTargets) {
        this.id = id;
        scriptManager = new ScriptManager();
        this.upgradeHelperScript = upgradeHelperScript;
        this.httpMessage = httpMessage;
        this.testSequenceItems = testSequenceItems;
        this.testTargets = testTargets;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TestSequence getCopy() {
        /*
        TestSequence testSequence = new TestSequence(scriptManager);
        if ( testSequenceItems != null ) {
            testSequence.setTestSequenceItems((ArrayList<TestSequenceItem>) testSequenceItems.clone());
        }
        if ( testTargets != null ) {
            testSequence.setTestTargets((ArrayList<TestTarget>) testTargets.clone());
        }
        if ( httpMessage != null ) {
            testSequence.setHttpMessage(httpMessage.getCopy());
        }
        if ( upgradeHelperScript != null ) {
            testSequence.setUpgradeHelperScript(upgradeHelperScript);
        }
         */
        TestSequence testSequence = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            oos.flush();

            byte[] bytes = bos.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            testSequence = (TestSequence) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return testSequence;
    }

    public String getEventScript() {
        return eventScript;
    }

    public void setEventScript(String eventScript) {
        this.eventScript = eventScript;
    }

    public String getUpgradeHelperScript() {
        return upgradeHelperScript;
    }

    public void setUpgradeHelperScript(String upgradeHelperScript) {
        this.upgradeHelperScript = upgradeHelperScript;
    }

    /*
        public String[] getPayloadListsIds() {
            return payloadListsIds;
        }

        public void setPayloadListsIds( String[] payloadListsIds) {
            this.payloadListsIds = payloadListsIds;
        }

         */
    private int getFlattenedTargetCount() {
        int fCount = 0;
        if ( testTargets != null ) {
            for ( TestTarget testTarget : testTargets ) {
                if ( testTarget.isEnabled()) {
                    fCount += 1;
                    fCount += testTarget.getEnabledEncodings().size();
                }
            }
        }
        return fCount;
    }

    public int getSequenceRunTimeMsec() {
        int runTime = 0;
        for ( TestSequenceItem testSequenceItem : testSequenceItems ) {
            runTime += testSequenceItem.getDelayMsec();
        }
        return runTime;
    }
    public int getTestCount( int payloadCount ) {
        return getFlattenedTargetCount() * payloadCount;
    }

    public int getStepCount( int payloadCount ) {
        return testSequenceItems.size() * getTestCount(payloadCount);
    }

    public int getEtaSec( int payloadCount) {
        int runSec = 0;
        int testCount = getTestCount(payloadCount);
        int seqRunTime = getSequenceRunTimeMsec();
        if ( getTestCount(payloadCount) > 0 && seqRunTime > 0 ) {
            runSec = ( testCount * seqRunTime ) / 1000;
        }
        return runSec;
    }

    public void saveTestStep(TestSequenceItem item ) {
        for ( int i = 0; i < testSequenceItems.size(); i++ ) {
            if ( testSequenceItems.get(i).getTestId().equals(item.getTestId())) {
                testSequenceItems.set(i,item);
            }
        }
    }
    public TestSequenceItem getTestItemById( String testId ) {
        TestSequenceItem testSequenceItem = null;
        for ( TestSequenceItem item : testSequenceItems ) {
            if ( item.getTestId().equals(testId)) {
                testSequenceItem = item;
            }
        }
        return testSequenceItem;
    }

    public void reset() {
        httpMessage = new HttpMessage();
        testSequenceItems = new ArrayList<>();
        testTargets = new ArrayList<>();

    }

    public void deleteTestStep( String stepId ) {
        for ( int i = 0; i < testSequenceItems.size(); i++ ) {
            if ( testSequenceItems.get(i).getTestId().equals(stepId)) {
                testSequenceItems.remove(i);
                break;
            }
        }
    }

    public void addTestStep ( TestSequenceItem testStep ) {
        testSequenceItems.add(testStep);
    }
    /*
    public void deleteWebsocketMessage( String msgId ) {
        for ( int i = 0; i < testSequenceItems.size(); i++ ) {
            if ( testSequenceItems.get(i).getFrame().getMessageId().equals(msgId)) {
                //System.out.println(String.format("Removing frame %s", wsFrames.get(i).getMessageId()));
                testSequenceItems.remove(i);
                break;
            }
        }
    }
     */

    /*
    public void updateWebsocketMessage( WebsocketFrame frame ) {
        boolean updated = false;
        for ( int i = 0; i < testSequenceItems.size(); i++ ) {
            if ( testSequenceItems.get(i).getFrame().getMessageId().equals(frame.getMessageId())) {
                testSequenceItems.get(i).setFrame(frame);
                updated = true;
                break;
            }
        }
        if ( !updated ) {
            TestSequenceItem item = new TestSequenceItem();
            item.setTestSequenceItemType(TestSequenceItemType.FRAME);
            item.setFrame(frame);
            testSequenceItems.add(item);
        }
    }
    */


    /*
        Used for in tests, applies script if it is present
     */

    public HttpMessage getTestHttpMessage() throws ScriptException {
        HttpMessage message = null;
        // If there is no helper script just get whatever the user gave us
        if ( getUpgradeHelperScript() == null ) {
            message = getHttpMessage();
        }
        else {
            Script upgradeScript = scriptManager.getScript("upgrade", getUpgradeHelperScript());
            String upgradeRequestStr = (String) upgradeScript.executeFunction("execute");
            if ( upgradeRequestStr != null ) {
                message = new HttpMessage();
                try {
                    message.fromBytes(upgradeRequestStr.getBytes());
                } catch ( IllegalArgumentException | HttpMessageParseException e) {
                    throw new ScriptException("Could not parse HTTP request created by helper script");
                }
            }
        }
        return message;
    }

    public HttpMessage getHttpMessage() {
        return httpMessage;
    }

    public void setHttpMessage(HttpMessage httpMessage) {
        this.httpMessage = httpMessage;
    }

    public ArrayList<TestSequenceItem> getTestableSequenceItems() {
        ArrayList<TestSequenceItem> testableItems = new ArrayList<>();
        for ( TestSequenceItem testSequenceItem : testSequenceItems ) {
            if ( testSequenceItem.getTestSequenceItemType().equals(TestSequenceItemType.FRAME)) {
                testableItems.add(testSequenceItem);
            }
        }
        return testableItems;
    }

    public void setTestSequenceItems( ArrayList<TestSequenceItem> testSequenceItems) {
        this.testSequenceItems = testSequenceItems;
    }
    public ArrayList<TestSequenceItem> getTestSequenceItems() {
        return testSequenceItems;
    }

    /*
    public ArrayList<TestSequenceItem> getWsFrames() {
        return testSequenceItems;
    }
     */

    public void setWsFrames(ArrayList<TestSequenceItem> testSequenceItems) {
        this.testSequenceItems = testSequenceItems;
    }

    public ArrayList<TestTarget> getTestTargets() {
        return testTargets;
    }

    public void setTestTargets(ArrayList<TestTarget> testTargets) {
        this.testTargets = testTargets;
    }

    public TestTarget getTestTargetById( String id ) {
        for ( TestTarget target : getTestTargets() ) {
            if ( target.getTargetUUID().equals(id)) {
                return target;
            }
        }
        return null;
    }

    public void deleteTestTargetById( String id ) {
        for ( int i = 0; i < testTargets.size(); i++ ) {
            if ( testTargets.get(i).getTargetUUID().equals(id)) {
                testTargets.remove(i);
                break;
            }
        }
    }
    @Override
    public String toString() {
        return "TestSequence{" +
                "id=" + id +
                ", upgradeHelperScript='" + upgradeHelperScript + '\'' +
                ", httpMessage=" + httpMessage +
                '}';
    }
}
