package com.mitmws.analyzer;

import com.mitmws.Main;
import com.mitmws.httpproxy.trafficlogger.*;
import com.mitmws.logging.AppLog;
import com.mitmws.projects.ProjectDataService;
import com.mitmws.projects.ProjectDataServiceException;
import com.mitmws.analyzer.models.AnalyzerHostModel;
import com.mitmws.analyzer.models.AnalyzerMessageModel;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.logging.Logger;

public class Analyzer {
    private HashMap<String, AnalyzerHostModel> analyzerHostModel = new HashMap<String,AnalyzerHostModel>();
    private HashMap<String,CacheItem> hostCache = new HashMap<String,CacheItem>();
    private HashMap<String,String> conversationIdHostMap = new HashMap<String,String>();
    private ProjectDataService projectDataService;
    private static Logger LOGGER = AppLog.getLogger(Main.class.getName());
    public Analyzer( ProjectDataService projectDataService ) {
        this.projectDataService = projectDataService;
    }


    public String getEndpointKey( String urlStr ) throws MalformedURLException {
        URL url = new URL(urlStr);
        return url.getHost();
    }


    public void submit (TrafficRecord trafficRecord ) {
    }

    private void analyzeMessage( WebsocketTrafficRecord record, String conversationId ) throws NoSuchAlgorithmException, IOException, ProjectDataServiceException {

    }
}
