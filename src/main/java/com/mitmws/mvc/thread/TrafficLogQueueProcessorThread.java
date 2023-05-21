package com.mitmws.mvc.thread;

import com.mitmws.configuration.ApplicationConfig;
import com.mitmws.httpproxy.trafficlogger.TrafficLogger;
import com.mitmws.httpproxy.trafficlogger.TrafficRecord;
import com.mitmws.httpproxy.trafficlogger.TrafficSource;
import com.mitmws.logging.AppLog;
import com.mitmws.mvc.model.MainModel;
import com.mitmws.projects.ProjectDataService;
import com.mitmws.projects.ProjectDataServiceException;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    Reads traffic from the HttpProxy's traffic queue and writes them to the disk
    Also writes them to UI
 */
public class TrafficLogQueueProcessorThread extends Thread {
    private Logger LOGGER = AppLog.getLogger(TrafficLogQueueProcessorThread.class.getName());
    private MainModel mainModel = null;
    private boolean shutdownRequested = false;
    private ApplicationConfig applicationConfig = new ApplicationConfig();
    private TrafficLogger trafficLogger = null;
    private ProjectDataService projectDataService = null;

    public TrafficLogQueueProcessorThread(TrafficLogger trafficLogger, ProjectDataService projectDataService) {
        this.trafficLogger = trafficLogger;
        this.projectDataService = projectDataService;
    }

    public TrafficLogQueueProcessorThread(MainModel mainModel) {
        this.mainModel = mainModel;
        trafficLogger = mainModel.getProxy().getLogger();
        projectDataService = mainModel.getProjectModel().getProjectDataService();
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
                TrafficRecord rec = trafficLogger.get();
                if ( rec != null ) {
                    projectDataService.saveTrafficRecord(rec);
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
                                projectDataService.saveHttpTrafficRecord(rec.getHttpTrafficRecord(),rec.getId());
                                // Update UI only if main model present
                                if ( rec.getTrafficSource().equals(TrafficSource.PROXY) && mainModel != null ) {
                                    mainModel.getTrafficModel().addHttpTraffic(rec.getHttpTrafficRecord());
                                    if ( rec.getHttpTrafficRecord().getResponse().getStatusCode() == 101 ) {
                                        mainModel.getTrafficModel().updateWebsocketConnections(rec);
                                        mainModel.getAnalyzerModel().submitRecord(rec);
                                    }
                                    mainModel.getMainStatusBarModel().incHttpCount();
                                }
                            }
                        }

                        if ( rec.getWebsocketTrafficRecord() != null ) {
                            projectDataService.saveWebsocketTrafficRecord(rec.getWebsocketTrafficRecord(),rec.getId());
                            if ( rec.getTrafficSource().equals(TrafficSource.PROXY) && mainModel != null ) {
                                mainModel.getTrafficModel().addWebsocketTraffic(rec.getWebsocketTrafficRecord().getFrame(),rec.getHighlightColour());
                                mainModel.getMainStatusBarModel().incWebsocketCount();
                                mainModel.getAnalyzerModel().submitRecord(rec);
                            }

                            if( rec.getTrafficSource().equals(TrafficSource.MANUAL_TEST) && mainModel != null) {
                                mainModel.getManualTesterModel().addWebsocketTraffic(rec.getWebsocketTrafficRecord().getFrame(),rec.getTestName(),rec.getHighlightColour());
                                mainModel.getMainStatusBarModel().incManualTestWebsocketCount();
                            }

                            if( rec.getTrafficSource().equals(TrafficSource.AUTOMATED_TEST) && mainModel != null) {
                                mainModel.getAutomatedTesterModel().addWebsocketTraffic(rec.getTestName(),rec.getWebsocketTrafficRecord().getFrame(),rec.getHighlightColour());
                                mainModel.getMainStatusBarModel().incAutomatedTestWebsocketCount();
                            }

                            if( rec.getTrafficSource().equals(TrafficSource.IMMEDIATE) && mainModel != null) {
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
                    if ( mainModel != null ) {
                        mainModel.syncActiveConnections();
                    }
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

