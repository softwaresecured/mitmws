package com.mitmws.mvc.thread;

import com.mitmws.mvc.model.ManualTestExecutionModel;

import java.util.ArrayList;
public class ManualTestManagerCleanupThread extends Thread {
    private ArrayList<ManualTestManagerActivityThread> threads = null;
    private boolean shutdownRequested = false;
    private ManualTestExecutionModel manualTestExecutionModel;
    public ManualTestManagerCleanupThread(ManualTestExecutionModel manualTestExecutionModel, ArrayList<ManualTestManagerActivityThread> threads) {
        this.threads = threads;
        this.manualTestExecutionModel = manualTestExecutionModel;
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
                            manualTestExecutionModel.setStatus("STOPPED");
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
