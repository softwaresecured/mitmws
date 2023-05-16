package com.mitmws.logging;

import com.mitmws.configuration.ApplicationConfig;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class AppLog {
    private static FileHandler fileHandler = null;
    public AppLog() {
    }

    private static void createHandler() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        createLogDir(applicationConfig);
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %2$s %5$s%6$s%n");
        String fileName = String.format("%s/%s", applicationConfig.getConfigDirPath(),"logs/mitmws.log");
        try {
            fileHandler = new FileHandler(fileName,100000,1);
            fileHandler.setFormatter(new SimpleFormatter());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FileHandler getFileHandler() {
        if ( fileHandler == null ) {
            createHandler();
        }
        return fileHandler;
    }


    public static Logger getLogger(String loggerName ) {
        Logger LOGGER = Logger.getLogger(String.format("app.%s", loggerName));
        for(Handler handler : LOGGER.getHandlers()) {
            if( handler.getClass() == FileHandler.class) {
                LOGGER.removeHandler(handler);
            }
        }
        LOGGER.addHandler(getFileHandler());
        return LOGGER;
    }
    /*
        Create the logs folder if it doesn't already exist
     */
    private static void createLogDir(ApplicationConfig applicationConfig) {
        File confDir = new File(String.format("%s/%s", applicationConfig.getConfigDirPath(),"logs"));
        if (!confDir.exists()) {
            confDir.mkdirs();
        }
    }
}
