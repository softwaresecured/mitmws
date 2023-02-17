package com.wsproxy.httpserver;

import com.wsproxy.logging.AppLog;

import java.util.logging.Logger;

public class HttpServerMaintenanceThread extends Thread {
    private boolean shutdownRequested = false;
    private HttpServerClientHandlerThread[] clientHandlerThreads;
    private static Logger LOGGER = AppLog.getLogger(HttpServerClientHandlerThread.class.getName());
    public HttpServerMaintenanceThread( HttpServerClientHandlerThread[] clientHandlerThreads ) {
        this.clientHandlerThreads = clientHandlerThreads;
    }
    public void shutdown() {
        shutdownRequested = true;
    }
    public void run() {
        LOGGER.info("HttpServerMaintenanceThread startup");
        while (!shutdownRequested) {
            for ( int i = 0; i < clientHandlerThreads.length; i++ ) {
                if ( clientHandlerThreads[i] != null ) {
                    try {
                        clientHandlerThreads[i].join(10);
                        if ( !clientHandlerThreads[i].isAlive() ) {
                            clientHandlerThreads[i] = null;

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LOGGER.info("HttpServerMaintenanceThread shutdown");
    }
}
