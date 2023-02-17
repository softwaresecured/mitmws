package com.wsproxy.mvc.thread;

import com.wsproxy.configuration.ApplicationConfig;
import com.wsproxy.httpproxy.trafficlogger.TrafficRecord;
import com.wsproxy.httpproxy.trafficlogger.TrafficSource;
import com.wsproxy.logging.AppLog;
import com.wsproxy.mvc.model.MainModel;
import com.wsproxy.projects.ProjectDataServiceException;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    Reads traffic from the HttpProxy's traffic queue and writes them to the disk
    Also writes them to UI
 */
public class TrafficLogQueueProcessorThread extends Thread {
    private Logger LOGGER = AppLog.getLogger(TrafficLogQueueProcessorThread.class.getName());
    private MainModel mainModel;
    private boolean shutdownRequested = false;
    private ApplicationConfig applicationConfig = new ApplicationConfig();


    public TrafficLogQueueProcessorThread(MainModel mainModel) {
        this.mainModel = mainModel;
    }

    public void run() {
        Pattern excludeRe = null;
        try {
            // inbound_proxy.url_exclude_regex
            String exlcudeReStr = applicationConfig.getProperty("inbound_proxy.url_exclude_regex");
            if ( exlcudeReStr != null ) {
                excludeRe = Pattern.compile(exlcudeReStr);
            }
            LOGGER.info("Traffic log queue processor started");
            long lastSync = 0;
            while ( !shutdownRequested ) {
                TrafficRecord rec = mainModel.getProxy().getLogger().get();
                if ( rec != null ) {
                    mainModel.getProjectModel().getProjectDataService().saveTrafficRecord(rec);
                    if ( rec.getId() >= 0 ) {
                        if ( rec.getHttpTrafficRecord() != null ) {
                            boolean httpExcluded = false;
                            if ( excludeRe != null ) {
                                Matcher m = excludeRe.matcher(rec.getHttpTrafficRecord().getRequest().getUrl());
                                if ( m.matches() ) {
                                    httpExcluded = true;
                                }
                            }
                            if ( !httpExcluded ) {
                                mainModel.getProjectModel().getProjectDataService().saveHttpTrafficRecord(rec.getHttpTrafficRecord(),rec.getId());
                                if ( rec.getTrafficSource().equals(TrafficSource.PROXY)) {
                                    mainModel.getTrafficModel().addHttpTraffic(rec.getHttpTrafficRecord());
                                    if ( rec.getHttpTrafficRecord().getResponse().getStatusCode() == 101 ) {
                                        mainModel.getTrafficModel().updateWebsocketConnections(rec);
                                    }
                                    mainModel.getMainStatusBarModel().incHttpCount();
                                }
                            }
                        }
                        if ( rec.getWebsocketTrafficRecord() != null ) {
                            mainModel.getProjectModel().getProjectDataService().saveWebsocketTrafficRecord(rec.getWebsocketTrafficRecord(),rec.getId());

                            if ( rec.getTrafficSource().equals(TrafficSource.PROXY)) {
                                mainModel.getTrafficModel().addWebsocketTraffic(rec.getWebsocketTrafficRecord().getFrame(),rec.getHighlightColour());
                                mainModel.getMainStatusBarModel().incWebsocketCount();
                                mainModel.getAnalyzerModel().submitRecord(rec);
                            }

                            if( rec.getTrafficSource().equals(TrafficSource.MANUAL_TEST)) {
                                mainModel.getManualTesterModel().addWebsocketTraffic(rec.getWebsocketTrafficRecord().getFrame(),rec.getTestName(),rec.getHighlightColour());
                                mainModel.getMainStatusBarModel().incManualTestWebsocketCount();
                            }

                            if( rec.getTrafficSource().equals(TrafficSource.AUTOMATED_TEST)) {
                                mainModel.getAutomatedTesterModel().addWebsocketTraffic(rec.getTestName(),rec.getWebsocketTrafficRecord().getFrame(),rec.getHighlightColour());
                                mainModel.getMainStatusBarModel().incAutomatedTestWebsocketCount();
                            }

                            if( rec.getTrafficSource().equals(TrafficSource.IMMEDIATE)) {
                                mainModel.getImmediateModel().addWebsocketTraffic(rec.getWebsocketTrafficRecord().getFrame(),rec.getTestName());
                                mainModel.getMainStatusBarModel().incImmediateTestWebsocketCount();
                            }

                        }
                    }
                }
                else {
                    Thread.sleep(10);
                }
                if( System.currentTimeMillis()-lastSync > 1000 ) {
                    lastSync = System.currentTimeMillis();
                    mainModel.syncActiveConnections();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ProjectDataServiceException e) {
            e.printStackTrace();
        }
        LOGGER.info("Traffic log queue processor stopped");
    }
    public void shutdown() {
        shutdownRequested = true;
    }


}

