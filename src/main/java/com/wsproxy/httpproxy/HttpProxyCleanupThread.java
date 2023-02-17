package com.wsproxy.httpproxy;

import com.wsproxy.logging.AppLog;

import java.util.ArrayList;
import java.util.logging.Logger;

public class HttpProxyCleanupThread extends Thread {
    private boolean shutdownRequested = false;
    private ArrayList<HttpProxyListenerThread> listenerPool = null;
    private Logger LOGGER = AppLog.getLogger(HttpProxyCleanupThread.class.getName());
    public HttpProxyCleanupThread(ArrayList<HttpProxyListenerThread> listenerPool) {
        this.listenerPool = listenerPool;
    }
    public void run() {
        LOGGER.info("Proxy cleanup thread started");
        while ( true ) {
            if ( listenerPool != null ) {
                for (HttpProxyListenerThread httpProxyListenerThread : listenerPool) {
                    for (int j = 0; j < httpProxyListenerThread.getClientHandlers().length; j++) {
                        if ( httpProxyListenerThread.getClientHandlers()[j] != null ) {
                            try {
                                httpProxyListenerThread.getClientHandlers()[j].join(1);
                                if (!httpProxyListenerThread.getClientHandlers()[j].isAlive()) {
                                    httpProxyListenerThread.getClientHandlers()[j] = null;
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            if ( shutdownRequested ) {
                break;
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LOGGER.info("Proxy cleanup thread stopped");
    }
    public void shutdown() {
        LOGGER.info("Shutdown request detected");
        shutdownRequested = true;
    }
}
