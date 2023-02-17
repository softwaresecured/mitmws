package com.wsproxy.logging;

import com.wsproxy.configuration.ApplicationConfig;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class PerfLog {
    private static FileHandler handler = null;
    public PerfLog() {

    }

    private static void createHandler() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        createLogDir(applicationConfig);
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %2$s %5$s%6$s%n");
        String fileName = String.format("%s/%s", applicationConfig.getConfigDirPath(),"logs/wsproxy-perf.log");

        try {
            handler = new FileHandler(fileName,100000,5);
            handler.setFormatter(new SimpleFormatter());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Logger getLogger(String loggerName ) {
        if ( handler == null ) {
            createHandler();
        }
        Logger LOGGER = Logger.getLogger(String.format("perf.%s", loggerName));
        LOGGER.addHandler(handler);
        return LOGGER;
    }
    private static void createLogDir(ApplicationConfig applicationConfig) {
        File confDir = new File(String.format("%s/%s", applicationConfig.getConfigDirPath(),"logs"));
        if (!confDir.exists()) {
            confDir.mkdirs();
        }
    }
}
