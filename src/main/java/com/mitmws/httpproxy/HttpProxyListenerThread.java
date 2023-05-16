package com.mitmws.httpproxy;

import com.mitmws.configuration.ApplicationConfig;
import com.mitmws.httpproxy.trafficlogger.TrafficLogger;
import com.mitmws.logging.AppLog;
import com.mitmws.mvc.model.BreakpointModel;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Logger;

public class HttpProxyListenerThread extends Thread {

    private int MAX_PROXY_CLIENT_HANDLERS = 500;
    private HttpProxyClientHandlerThread[] clientHandlers;
    private boolean shutdownRequested = false;
    private InetAddress listenAddr = null;
    private int listenPort = 0;
    private int requestsHandled = 0;
    private ApplicationConfig appConfig = null;
    private TrafficLogger trafficLoger;
    private ServerSocket serverSocket = null;
    private HostCache hostCache = new HostCache();
    private BreakpointModel breakpointModel;
    private Logger LOGGER = AppLog.getLogger(HttpProxyListenerThread.class.getName());

    public ArrayList<HttpProxyClientHandlerThread> getActiveSessions() {
        ArrayList<HttpProxyClientHandlerThread> sessions = new ArrayList<HttpProxyClientHandlerThread>();
        for ( HttpProxyClientHandlerThread httpProxyClientHandlerThread : clientHandlers ) {
            if ( httpProxyClientHandlerThread != null ) {
                if ( httpProxyClientHandlerThread.isAlive() ) {
                    sessions.add(httpProxyClientHandlerThread);
                }
            }
        }
        return sessions;
    }

    public InetAddress getListenInetAddress() {
        InetAddress addr = null;
        if ( serverSocket != null ) {
            addr = serverSocket.getInetAddress();
        }
        return addr;
    }

    public InetAddress getListenAddr() {
        return listenAddr;
    }

    public int getListenPort() {
        return listenPort;
    }

    public HttpProxyClientHandlerThread[] getClientHandlers() {
        return clientHandlers;
    }

    public HttpProxyListenerThread(InetAddress address, int port, TrafficLogger trafficLoger, BreakpointModel breakpointModel ) {
        this.breakpointModel = breakpointModel;
        clientHandlers = new HttpProxyClientHandlerThread[MAX_PROXY_CLIENT_HANDLERS];
        for ( int i = 0; i < clientHandlers.length; i++ ) {
            clientHandlers[i] = null;
        }
        listenAddr = address;
        listenPort = port;
        appConfig = new ApplicationConfig();
        this.trafficLoger = trafficLoger;
    }

    public void shutdown() {
        if ( serverSocket != null ) {
            try {
                serverSocket.close();
            } catch (IOException e) {
            }
        }
        shutdownRequested = true;
    }

    private boolean allocate( Socket clientSocket ) {
        for ( int i = 0; i < clientHandlers.length; i++ ) {
            if ( clientHandlers[i] == null ) {
                clientHandlers[i] = new HttpProxyClientHandlerThread(clientSocket, trafficLoger, hostCache, breakpointModel);
                clientHandlers[i].start();
                return true;
            }
        }
        return false;
    }

    @Override
    public void run() {
        serverSocket = null;
        try {
            serverSocket = new ServerSocket(listenPort,512,listenAddr);
            LOGGER.info(String.format("Proxy listening on %s:%d", listenAddr.getHostAddress(),listenPort));
            while (!shutdownRequested) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    allocate(clientSocket);
                } catch (IOException e) {
                }

            }
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
        LOGGER.info(String.format("Stopped listening on %s:%d", listenAddr.getHostAddress(),listenPort));
    }
}
