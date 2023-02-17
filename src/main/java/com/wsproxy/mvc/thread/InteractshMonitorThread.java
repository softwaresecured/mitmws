package com.wsproxy.mvc.thread;

import com.wsproxy.logging.AppLog;
import com.wsproxy.mvc.model.InteractshModel;
import java.io.IOException;
import java.util.logging.Logger;

public class InteractshMonitorThread extends  Thread {
    private final Logger LOGGER = AppLog.getLogger(InteractshMonitorThread.class.getName());
    boolean shutdownRequested = false;
    private InteractshModel interactshModel;
    public InteractshMonitorThread(InteractshModel interactshModel ) {
        this.interactshModel = interactshModel;
    }
    public void shutdown() {
        shutdownRequested = true;
    }
    public void run() {
        LOGGER.info("Interact-sh monitor started");
        try {
            interactshModel.reloadConfig();
            interactshModel.register();
            LOGGER.info(String.format("Registered correlation %s", interactshModel.getCorrelationId()));
            LOGGER.info(String.format("Test payload %s", interactshModel.getPayload()));
            while ( !shutdownRequested ) {
                interactshModel.poll();
                Thread.sleep(interactshModel.getPollIntervalMs());
            }
            interactshModel.deregister();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("Interact-sh monitor stopped");
    }
}
