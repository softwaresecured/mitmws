package com.mitmws.mvc.thread;

import com.mitmws.logging.AppLog;
import com.mitmws.mvc.model.InteractshModel;
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
            LOGGER.severe(String.format("IO exception while contacting interactsh server: %s", e.getMessage()));
        } catch (InterruptedException e) {
            ;
        }
        LOGGER.info("Interact-sh monitor stopped");
    }
}
