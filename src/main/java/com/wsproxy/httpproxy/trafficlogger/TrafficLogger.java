package com.wsproxy.httpproxy.trafficlogger;

import com.wsproxy.httpproxy.HttpMessage;
import com.wsproxy.httpproxy.HttpProxyCleanupThread;
import com.wsproxy.httpproxy.websocket.WebsocketFrame;
import com.wsproxy.logging.AppLog;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrafficLogger {
    private final int TRAFFIC_LOG_QUEUE_LEN=1024;
    private ArrayBlockingQueue trafficLogQueue = null;
    private Pattern projectScopeRe = null;
    private Pattern urlExcludeRe = null;
    private final Logger LOGGER = AppLog.getLogger(HttpProxyCleanupThread.class.getName());
    public TrafficLogger() {
        trafficLogQueue = new ArrayBlockingQueue(TRAFFIC_LOG_QUEUE_LEN);
    }

    public void setUrlExclude( String exclude) {
        if ( exclude != null ) {
            urlExcludeRe = Pattern.compile(exclude);
        }
    }

    public void setProjectScope( String projectScope ) {
        if ( projectScope != null ) {
            projectScopeRe = Pattern.compile(projectScope);
        }
    }

    public boolean inScope (String url ) {
        URL checkUrl = null;
        try {
            if ( urlExcludeRe != null ) {
                Matcher m = urlExcludeRe.matcher(url);
                if ( m.matches() ) {
                    return false;
                }
            }
            if ( projectScopeRe != null ) {
                checkUrl = new URL(url);
                Matcher m = projectScopeRe.matcher(checkUrl.getHost());
                if ( !m.matches() ) {
                    return false;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return true;
    }
    public void logRFC6455Message(TrafficSource trafficSource, WebsocketFrame frame, String testName ) {
        logRFC6455Message( trafficSource,  frame,  testName, Color.WHITE );
    }

    public void logRFC6455Message(TrafficSource trafficSource, WebsocketFrame frame, String testName, Color higlightColor ) {
        if ( frame != null ) {
            String payload = "";
            if ( frame.getPayload() != null && frame.getMasked() == 0 ) {
                payload = new String(frame.getPayload());
                if ( payload.length() > 100 ) {
                    payload = payload.substring(0,100);
                }
            }
            //LOGGER.info(String.format("[RFC6455] %s %s/%s", frame.getDirection().toString(), frame.getOpcode().toString(),payload));
            TrafficRecord rec = new TrafficRecord(trafficSource);
            rec.setHighlightColour(higlightColor);
            rec.setTestName(testName);
            rec.setWebsocketTrafficRecord( new WebsocketTrafficRecord(frame));
            add(rec);
        }
    }

    public void logRFC6455Message(TrafficSource trafficSource, WebsocketFrame frame, String testName, int testRunId ) {
        logRFC6455Message(trafficSource, frame, testName, testRunId, Color.WHITE);
    }

    public void logRFC6455Message(TrafficSource trafficSource, WebsocketFrame frame, String testName, int testRunId, Color higlightColor ) {
        if ( frame != null ) {
            String payload = "";
            if ( frame.getPayload() != null && frame.getMasked() == 0 ) {
                payload = new String(frame.getPayload());
                if ( payload.length() > 100 ) {
                    payload = payload.substring(0,100);
                }
            }
            TrafficRecord rec = new TrafficRecord(trafficSource);
            rec.setHighlightColour(Color.WHITE);
            rec.setTestName(testName);
            rec.setTestRunId(testRunId);
            rec.setHighlightColour(higlightColor);
            rec.setWebsocketTrafficRecord( new WebsocketTrafficRecord(frame));
            add(rec);
        }
    }

    public void logRFC2616Message(TrafficSource trafficSource, HttpMessage request, HttpMessage response, String testName ) {
        logRFC2616Message( trafficSource,  request,  response,  testName, Color.WHITE );
    }

    public void logRFC2616Message(TrafficSource trafficSource, HttpMessage request, HttpMessage response, String testName,Color higlightColor ) {
        if ( request != null && response != null ) {
            if ( inScope(request.getUrl())) {
                //LOGGER.info(String.format("[RFC2616] %s %d %s %s", request.getMessageId(), response.getStatusCode(),request.getHttpMethod(), request.getUrl()));
                TrafficRecord rec = new TrafficRecord(trafficSource);
                rec.setHighlightColour(higlightColor);
                rec.setTestName(testName);
                rec.setHttpTrafficRecord(new HttpTrafficRecord( request, response));
                add(rec);
            }
        }
    }

    public void logRFC2616Message(TrafficSource trafficSource, HttpMessage request, HttpMessage response, String testName, int testRunId ) {
        if ( request != null && response != null ) {
            TrafficRecord rec = new TrafficRecord(trafficSource);
            rec.setHighlightColour(Color.WHITE);
            rec.setTestName(testName);
            rec.setTestRunId(testRunId);
            rec.setHttpTrafficRecord(new HttpTrafficRecord( request, response));
            add(rec);
        }
    }

    private void add( TrafficRecord rec ) {
        try {
            trafficLogQueue.put(rec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public TrafficRecord get() {
        TrafficRecord rec = null;
        rec = (TrafficRecord) trafficLogQueue.poll();
        return rec;
    }
}
