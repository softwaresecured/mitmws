package com.mitmws.mvc.thread;

import com.mitmws.logging.AppLog;
import com.mitmws.mvc.model.MainModel;

import java.util.logging.Logger;

public class MaintenanceThread extends Thread {
    private MainModel mainModel;
    private boolean shutdownRequested = false;
    private Logger LOGGER = AppLog.getLogger(MaintenanceThread.class.getName());
    public MaintenanceThread (MainModel mainModel ) {
        this.mainModel = mainModel;
    }

    public void run() {
        LOGGER.info("Maintenance thread started");
        while ( !shutdownRequested ) {
            // Cleanup search thread
            if ( mainModel.getTrafficSearchThread() != null ) {
                try {
                    mainModel.getTrafficSearchThread().join(100);
                    if ( !mainModel.getTrafficSearchThread().isAlive() ) {
                        mainModel.setTrafficSearchThread(null);
                        LOGGER.info("Cleaned up traffic search thread");
                    }
                } catch (InterruptedException e) {
                    ;
                }
            }
            // Try to join traffic loader thread
            if ( mainModel.getTrafficLoaderThread() != null ) {
                try {
                    mainModel.getTrafficLoaderThread().join(100);
                    if ( !mainModel.getTrafficLoaderThread().isAlive() ) {
                        mainModel.setTrafficLoaderThread(null);
                        LOGGER.info("Cleaned up traffic loader thread");
                    }
                } catch (InterruptedException e) {
                    ;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Try to join an immediate thread
            if ( mainModel.getImmediateThread() != null ) {
                try {
                    mainModel.getImmediateThread().join(100);
                    if ( !mainModel.getImmediateThread().isAlive()) {
                        mainModel.setImmediateThread(null);
                        LOGGER.info("Cleaned up immediate thread");
                    }
                } catch (InterruptedException e) {
                    ;
                }
            }

            // Join proto tester
            if ( mainModel.getProtocolTesterThread() != null ) {
                try {
                    mainModel.getProtocolTesterThread().join(100);
                    if ( !mainModel.getProtocolTesterThread().isAlive()) {
                        mainModel.setProtocolTesterThread(null);
                        LOGGER.info("Cleaned up protocol tester thread");
                    }
                } catch (InterruptedException e) {
                    ;
                }
            }

            // Raw tester thread
            if ( mainModel.getRawTesterThread() != null ) {
                try {
                    mainModel.getRawTesterThread().join(100);
                    if ( !mainModel.getRawTesterThread().isAlive()) {
                        mainModel.setRawTesterThread(null);
                        LOGGER.info("Cleaned up raw tester thread");
                    }
                } catch (InterruptedException e) {
                    ;
                }
            }

            /*
                Update sessions on the immediate tab
             */
            mainModel.getImmediateModel().setActiveSessionNames(mainModel.getProxy().getActiveWebsocketSessionNames());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // update counts of proxy/http server
            mainModel.getMainStatusBarModel().setHttpServerActiveSessions(mainModel.getHttpServer().getActiveClientHandlerThreadCount());
            mainModel.getMainStatusBarModel().setProxyServerActiveSessions(mainModel.getProxy().getActiveWebsocketConnections().length);
        }
        LOGGER.info("Stopped");
    }
    public void shutdown() {
        shutdownRequested = true;
    }
}
