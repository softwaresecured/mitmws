package com.mitmws.httpproxy;

import com.mitmws.configuration.ApplicationConfig;
import com.mitmws.httpproxy.trafficlogger.TrafficLogger;
import com.mitmws.httpproxy.websocket.WebsocketSession;
import com.mitmws.logging.AppLog;
import com.mitmws.mvc.model.BreakpointModel;

import java.beans.PropertyChangeListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class HttpProxy {

    private ApplicationConfig appConfig = null;
    private TrafficLogger trafficLog = null;
    private ArrayList<HttpProxyListenerThread> httpProxylisteners = null;
    private HttpProxyCleanupThread httpProxyCleanupThread = null;
    private static Logger LOGGER = AppLog.getLogger(HttpProxy.class.getName());
    private BreakpointModel breakpointModel;

    private String projectScope = null;
    public HttpProxy(BreakpointModel breakpointModel) {
        this.breakpointModel = breakpointModel;
        appConfig = new ApplicationConfig();
        httpProxylisteners = new ArrayList<>();
        trafficLog = new TrafficLogger();
        trafficLog.setProjectScope(projectScope);
    }

    public void addCleanupThreadEventListener(PropertyChangeListener listener ) {
        if ( httpProxyCleanupThread != null ) {
            httpProxyCleanupThread.addListener(listener);
        }
    }

    public ArrayList<HttpProxyListenerThread> getHttpProxylisteners() {
        return httpProxylisteners;
    }

    public ArrayList<String> getActiveWebsocketSessionNames() {
        ArrayList<String> names = new ArrayList<String>();
        if ( getHttpProxylisteners() != null ) {
            for ( HttpProxyListenerThread httpProxyListenerThread : getHttpProxylisteners() ) {
                for ( HttpProxyClientHandlerThread httpProxyClientHandlerThread : httpProxyListenerThread.getActiveSessions() ) {
                    if ( httpProxyClientHandlerThread.getWebsocketSession() != null ) {
                        names.add(httpProxyClientHandlerThread.getWebsocketSession().getSessionName());
                    }
                }
            }
        }
        return names;
    }

    public WebsocketSession getWebsocketSessionByName(String name ) {
        if ( getHttpProxylisteners() != null ) {
            for ( HttpProxyListenerThread httpProxyListenerThread : getHttpProxylisteners() ) {
                for ( HttpProxyClientHandlerThread httpProxyClientHandlerThread : httpProxyListenerThread.getActiveSessions() ) {
                    if ( httpProxyClientHandlerThread.getWebsocketSession() != null ) {
                        if ( httpProxyClientHandlerThread.getWebsocketSession().getSessionName().equals(name )) {
                            return httpProxyClientHandlerThread.getWebsocketSession();
                        }
                    }
                }
            }
        }
        return null;
    }

    public ArrayList<String> getActiveWebsocketSessionUpgradeMessageUUIDs() {
        ArrayList<String> uuids = new ArrayList<String>();
        if ( getHttpProxylisteners() != null ) {
            for ( HttpProxyListenerThread httpProxyListenerThread : getHttpProxylisteners() ) {
                for ( HttpProxyClientHandlerThread httpProxyClientHandlerThread : httpProxyListenerThread.getActiveSessions() ) {
                    if ( httpProxyClientHandlerThread.getWebsocketSession() != null ) {
                        uuids.add(httpProxyClientHandlerThread.getWebsocketSession().getUpgradeMsgUUID());
                    }
                }
            }
        }
        return uuids;
    }

    public HttpMessage test(HttpMessage request ) {

        return null;
    }

    public TrafficLogger getLogger() {
        return trafficLog;
    }

    public String getProjectScope() {
        return projectScope;
    }

    public InetAddress[] getListenAddresses() {
        InetAddress[] addr = null;
        if ( httpProxylisteners.size() > 0 ) {
            addr = new InetAddress[httpProxylisteners.size()];
            for ( int i = 0; i < httpProxylisteners.size(); i++ ) {
                addr[i] = httpProxylisteners.get(i).getListenInetAddress();
            }
        }
        return addr;
    }

    public String[] getActiveWebsocketConnections() {
        String[] activeSessions = new String[0];
        ArrayList<String> sessions = new ArrayList<>();
        for ( HttpProxyListenerThread listener : httpProxylisteners ) {
            for ( HttpProxyClientHandlerThread clientHandlerThread : listener.getClientHandlers() ) {
                if ( clientHandlerThread != null ) {
                    String wsSession = null;
                    if ( (wsSession = clientHandlerThread.getWebsocketSessionId()) != null ) {
                        sessions.add(wsSession);
                    }
                }
            }
        }
        if ( sessions.size() > 0 ) {
            activeSessions = new String[sessions.size()];
            for ( int i = 0; i < sessions.size();i++ ) {
                activeSessions[i] = sessions.get(i);
            }
        }
        return activeSessions;
    }

    public void reloadConfig() throws UnknownHostException {
        stopAll();
        removeAllListeners();
        trafficLog.setProjectScope(projectScope);
        addListener(InetAddress.getByName(appConfig.getProperty("inbound_proxy.default_listen_address")),Integer.parseInt(appConfig.getProperty("inbound_proxy.default_listen_port")));
        // load others from file?
    }

    /*
        Returns the first proxy listener
        Original idea was to have multiple proxies but later questioned why
        UI only supports one proxy for now
     */
    public String getFirstInstanceListenAddress() {
        String listenAddr = null;
        for ( HttpProxyListenerThread thread : httpProxylisteners ) {
            if ( thread.getListenAddr() != null ) {
                listenAddr = String.format("%s:%d", thread.getListenInetAddress().getHostAddress(), thread.getListenPort());
                break;
            }
        }
        return listenAddr;
    }

    public void removeAllListeners() {
        httpProxylisteners.clear();
    }
    public void addListener(InetAddress addr, int port ) {
        HttpProxyListenerThread httpProxylistenerThread = new HttpProxyListenerThread( addr, port,trafficLog, breakpointModel);
        httpProxylisteners.add(httpProxylistenerThread);
        LOGGER.info(String.format("Added proxy listener %s:%d", addr.getHostAddress(), port));
    }

    public void stopAll() {
        // Ask them all to shutdown
        LOGGER.info("Stopping listener threads");
        for ( HttpProxyListenerThread curProxyListener : httpProxylisteners ) {
            curProxyListener.shutdown();
        }

        // Join them all
        for ( HttpProxyListenerThread curProxyListener : httpProxylisteners ) {
            try {
                curProxyListener.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Stop / join the cleanup thread
        if ( httpProxyCleanupThread != null ) {
            httpProxyCleanupThread.shutdown();
            try {
                httpProxyCleanupThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LOGGER.info("Proxy listener thread cleanup complete");
    }

    public void startAll() {
        LOGGER.info("Starting proxy listener threads");
        try {
            reloadConfig();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        for ( Thread curProxyListener : httpProxylisteners ) {
            curProxyListener.start();
        }
        httpProxyCleanupThread = new HttpProxyCleanupThread(httpProxylisteners);
        httpProxyCleanupThread.start();
    }
}
