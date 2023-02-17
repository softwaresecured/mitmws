package com.wsproxy.mvc.thread;

import com.wsproxy.configuration.ApplicationConfig;
import com.wsproxy.httpproxy.trafficlogger.TrafficSource;
import com.wsproxy.httpproxy.trafficlogger.WebsocketTrafficRecord;
import com.wsproxy.logging.AppLog;
import com.wsproxy.mvc.model.MainModel;
import com.wsproxy.mvc.view.panels.search.PnlTrafficSearchView;
import com.wsproxy.projects.ProjectDataServiceException;
import com.wsproxy.util.GuiUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrafficSearchThread extends Thread {
    private Logger LOGGER = AppLog.getLogger(TrafficSearchThread.class.getName());
    boolean shutdownRequested = false;
    private final int SEARCH_BATCH_SIZE = 10;
    private MainModel mainModel;
    private Pattern pattern;
    public TrafficSearchThread(MainModel mainModel) {
        this.mainModel = mainModel;
        pattern = null;
        if ( mainModel.getTrafficSearchModel().isSearchRegex() ) {
            pattern = GuiUtils.getPattern(mainModel.getTrafficSearchModel().getSearchText());
        }
    }
    public void shutdown() {
        shutdownRequested = true;
    }

    public void run() {
        int offset = 0;
        try {
            ArrayList<WebsocketTrafficRecord> records;
            String matchInput;
            do {
                records = mainModel.getProjectModel().getProjectDataService().getSearchableRecords(offset,SEARCH_BATCH_SIZE);
                offset += records.size();
                if( records.size() == 0 ) {
                    break;
                }
                for ( WebsocketTrafficRecord record : records ) {
                    if ( record.getFrame().getPayloadUnmasked() != null ) {
                        matchInput = new String(record.getFrame().getPayloadUnmasked());
                        if ( isInScope(record.getTrafficSource()) && matchText(matchInput) && mainModel.getTrafficSearchModel().getResultsModel().getRowCount() < mainModel.getTrafficSearchModel().getMaxResults()) {
                            mainModel.getTrafficSearchModel().addSearchResult(
                                    record.getFrame().getMessageUUID(),
                                    record.getFrame().getCreateTime(),
                                    record.getTrafficSource().toString(),
                                    GuiUtils.getSnippet(matchInput,500));
                            if ( mainModel.getTrafficSearchModel().getResultsModel().getRowCount() > mainModel.getTrafficSearchModel().getMaxResults() ) {
                                break;
                            }
                        }

                    }
                }
                if ( mainModel.getTrafficSearchModel().getResultsModel().getRowCount() > mainModel.getTrafficSearchModel().getMaxResults() ) {
                    break;
                }
                if ( shutdownRequested ) {
                    break;
                }
            } while ( records.size() > 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ProjectDataServiceException e) {
            e.printStackTrace();
        }
        LOGGER.info("Traffic search thread stopping");
    }
    public boolean isInScope( TrafficSource trafficSource ) {
        boolean inScope = false;
        if ( trafficSource.equals(TrafficSource.PROXY) && mainModel.getTrafficSearchModel().isSearchProxy() ) {
            inScope = true;
        }
        if ( trafficSource.equals(TrafficSource.MANUAL_TEST) && mainModel.getTrafficSearchModel().isSearchManualTester() ) {
            inScope = true;
        }
        if ( trafficSource.equals(TrafficSource.AUTOMATED_TEST) && mainModel.getTrafficSearchModel().isSearchAutomatedTester() ) {
            inScope = true;
        }
        if ( trafficSource.equals(TrafficSource.IMMEDIATE) && mainModel.getTrafficSearchModel().isSearchImmediate() ) {
            inScope = true;
        }
        return inScope;
    }

    public boolean matchText( String inputText ) {
        if ( inputText != null ) {
            if ( mainModel.getTrafficSearchModel().isSearchRegex() ) {
                Matcher m = pattern.matcher(inputText);
                return m.find();
            }
            else {
                return inputText.contains(mainModel.getTrafficSearchModel().getSearchText());
            }
        }
        return false;
    }
}