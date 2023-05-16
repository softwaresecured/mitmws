package com.mitmws.mvc.thread;

import com.mitmws.mvc.model.AutomatedTesterModel;

import java.util.ArrayList;

public class AutomatedTestManagerCleanupThread extends Thread {
    private ArrayList<AutomatedTestManagerActivityThread> threads = null;
    private boolean shutdownRequested = false;
    private AutomatedTesterModel automatedTesterModel;
    public AutomatedTestManagerCleanupThread(AutomatedTesterModel automatedTesterModel, ArrayList<AutomatedTestManagerActivityThread> threads ) {
        this.automatedTesterModel = automatedTesterModel;
        this.threads = threads;
    }

    public void shutdown() {
        shutdownRequested = true;
    }
    public void run() {
        while ( !shutdownRequested) {
            boolean pruned = false;
            if ( threads != null ) {
                for ( int i = 0 ; i < threads.size(); i++ ) {
                    try {
                        threads.get(i).join(10);
                        if ( !threads.get(i).isAlive()) {
                            threads.remove(i);
                            pruned = true;
                            automatedTesterModel.getAutomatedTestExecutionModel().setStatus("STOPPED");
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if ( !pruned ) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
