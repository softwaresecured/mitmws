package com.mitmws.httpserver;

import com.mitmws.integrations.python.Script;
import com.mitmws.integrations.python.ScriptManager;
import com.mitmws.logging.AppLog;
import com.mitmws.util.ScriptUtil;

import javax.script.ScriptException;
import java.util.HashMap;
import java.util.logging.Logger;

public class HttpServer {
    private static Logger LOGGER = AppLog.getLogger(HttpServer.class.getName());
    private HttpServerListenerThread httpServerListenerThread = null;
    private HttpServerMaintenanceThread httpServerMaintenanceThread = null;
    private HashMap<String,Script> routes = new HashMap<String,Script>();
    public HttpServer() {
        registerCustomHandlers();
    }

    public HttpServerListenerThread getHttpServerListenerThread() {
        return httpServerListenerThread;
    }

    public int getActiveClientHandlerThreadCount() {
        int tc = 0;
        for ( int i = 0; i < httpServerListenerThread.getClientHandlerThreads().length; i++ ) {
            if (  httpServerListenerThread.getClientHandlerThreads()[i] != null ) {
                if ( httpServerListenerThread.getClientHandlerThreads()[i].isAlive() ) {
                    tc += 1;
                }
            }
        }
        return tc;
    }

    public void registerCustomHandlers() {
        ScriptManager scriptManager = new ScriptManager();
        for ( String routeScript : ScriptUtil.getScriptsByType("httpserver")) {
            try {
                Script script = scriptManager.getScript("httpserver", routeScript);
                String path = (String) script.executeFunction("getPath");
                registerHandler(String.format("/custom/%s", path),script);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
    }

    public void registerHandler( String path, Script script ) {
        LOGGER.info(String.format("Mapped path %s to script %s", path,script.getScriptFileName()));
        routes.put(path,script);
    }


    public void start() {
        httpServerListenerThread = new HttpServerListenerThread(routes);
        httpServerListenerThread.start();
        httpServerMaintenanceThread = new HttpServerMaintenanceThread(httpServerListenerThread.getClientHandlerThreads());
        httpServerMaintenanceThread.start();
        LOGGER.info("HTTP Server started");
    }

    public void stop() {
        httpServerListenerThread.shutdown();
        httpServerMaintenanceThread.shutdown();
        try {
            httpServerListenerThread.join();
            httpServerMaintenanceThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("HTTP Server stopped");
    }

    public void restart() {

    }

}
