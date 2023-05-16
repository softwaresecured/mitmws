package com.mitmws.httpserver;
import com.mitmws.configuration.ApplicationConfig;
import com.mitmws.integrations.python.Script;
import com.mitmws.logging.AppLog;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Logger;

public class HttpServerListenerThread extends Thread {
    private final int MAX_CLIENT_HANDLERS = 100;
    private ApplicationConfig applicationConfig = new ApplicationConfig();
    private ServerSocket serverSocket = null;
    private boolean shutdownRequested = false;
    private HashMap<String, Script> routes;
    private HttpServerClientHandlerThread[] clientHandlerThreads;
    private static Logger LOGGER = AppLog.getLogger(HttpServerListenerThread.class.getName());
    public HttpServerListenerThread( HashMap<String,Script> routes ) {
        this.routes = routes;
        clientHandlerThreads = new HttpServerClientHandlerThread[MAX_CLIENT_HANDLERS];
        for ( int i = 0 ; i < MAX_CLIENT_HANDLERS; i++ ) {
            clientHandlerThreads[i] = null;
        }
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

    public boolean allocate( Socket clientSocket ) {
        for ( int i = 0; i < clientHandlerThreads.length; i++ ) {
            if ( clientHandlerThreads[i] == null ) {
                clientHandlerThreads[i] = new HttpServerClientHandlerThread(clientSocket,routes);
                clientHandlerThreads[i].start();
                return true;
            }
        }
        return false;
    }

    public String getListenAddr() {
        String listenAddr = null;
        if ( serverSocket != null ) {
            listenAddr = String.format("%s:%d", serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort());
        }
        return listenAddr;
    }

    public void run() {
        serverSocket = null;
        int listenPort = Integer.parseInt(applicationConfig.getProperty("util.httpserver.listen_port"));
        String listenAddr = applicationConfig.getProperty("util.httpserver.listen_address");
        try {
            serverSocket = new ServerSocket(listenPort,512, InetAddress.getByName(listenAddr));
            while (!shutdownRequested) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    allocate(clientSocket);
                } catch (IOException e) {
                    ;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public HttpServerClientHandlerThread[] getClientHandlerThreads() {
        return clientHandlerThreads;
    }
}
