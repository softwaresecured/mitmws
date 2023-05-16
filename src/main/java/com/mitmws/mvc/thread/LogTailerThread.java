package com.mitmws.mvc.thread;
/*
    Tail the logs/wsproxy.log file and add the log lines to the log model
 */
import com.mitmws.configuration.ApplicationConfig;
import com.mitmws.logging.AppLog;
import com.mitmws.mvc.model.LogModel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

public class LogTailerThread extends Thread {
    private int GUI_UPDATE_INTERVAL_MS = 100;
    private int LOG_BUFF_READ_BUFF_LEN = 1024*10;
    private Logger LOGGER = AppLog.getLogger(LogTailerThread.class.getName());
    boolean shutdownRequested = false;
    private LogModel logModel;
    private ApplicationConfig applicationConfig ;
    public LogTailerThread(LogModel logModel) {
        this.logModel = logModel;
        applicationConfig = new ApplicationConfig();

    }
    public void shutdown() {
        shutdownRequested = true;
    }

    public void run() {
        LOGGER.info("Log tailer thread started");
        // TODO: Need to handle file rollover
        FileReader logFileReader = null;
        try {
            String logFileName = String.format("%s/%s", applicationConfig.getConfigDirPath(),"logs/mitmws.log");
            logFileReader = new FileReader(logFileName);
            BufferedReader br = new BufferedReader(logFileReader);
            while ( !shutdownRequested ) {
                String line = br.readLine();
                if ( line != null && line.length()>0 ) {
                    logModel.addLogMsg(line.trim());
                }
                else {
                    Thread.sleep(100);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if ( logFileReader != null ) {
                    logFileReader.close();
                }
            } catch (IOException e) {
            }
        }
        LOGGER.info("Log tailer thread stopping");
    }
}