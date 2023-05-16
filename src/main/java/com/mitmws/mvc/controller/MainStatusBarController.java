package com.mitmws.mvc.controller;

import com.mitmws.mvc.model.MainStatusBarModel;
import com.mitmws.mvc.view.panels.mainform.PnlMainStatusBarView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

public class MainStatusBarController implements PropertyChangeListener  {
    private MainStatusBarModel mainStatusBarModel;
    private PnlMainStatusBarView pnlMainStatusBarView;

    public MainStatusBarController(MainStatusBarModel mainStatusBarModel, PnlMainStatusBarView pnlMainStatusBarView) {
        this.mainStatusBarModel = mainStatusBarModel;
        this.pnlMainStatusBarView = pnlMainStatusBarView;
        mainStatusBarModel.addListener(this);
    }

    public MainStatusBarModel getMainStatusBarModel() {
        return mainStatusBarModel;
    }

    public PnlMainStatusBarView getPnlMainStatusBarView() {
        return pnlMainStatusBarView;
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String countProperties[] = { "MainStatusBarModel.httpCount",
                "MainStatusBarModel.websocketCount",
                "MainStatusBarModel.websocketManualTestCount",
                "MainStatusBarModel.websocketAutomatedTestCount",
                "MainStatusBarModel.websocketImmediateTestCount"
        };
        if ( Arrays.asList(countProperties).contains(propertyChangeEvent.getPropertyName()) ) {
            pnlMainStatusBarView.lblTrafficStatus.setText(String.format(" | Proxy H:%d W:%d, Manual W:%d, Auto W:%d, Immediate W:%d",
                    mainStatusBarModel.getHttpCount(),
                    mainStatusBarModel.getWebsocketCount(),
                    mainStatusBarModel.getWebsocketManualTestCount(),
                    mainStatusBarModel.getWebsocketAutomatedTestCount(),
                    mainStatusBarModel.getWebsocketImmediateTestCount()
                    )
            );
        }

        if ( "MainStatusBarModel.proxyListenAddr".equals(propertyChangeEvent.getPropertyName())) {
            if ( mainStatusBarModel.getProxyListenAddr() != null ) {
                pnlMainStatusBarView.lblProxyStatus.setText(String.format("Proxy server (running) %s, %d connected", mainStatusBarModel.getProxyListenAddr(), mainStatusBarModel.getProxyServerActiveSessions()));
            }
            else {
                pnlMainStatusBarView.lblProxyStatus.setText("Proxy server (stopped)");
            }
        }

        if ( "MainStatusBarModel.httpListenAddr".equals(propertyChangeEvent.getPropertyName())) {
            if ( mainStatusBarModel.getHttpListenAddr() != null ) {
                pnlMainStatusBarView.lblHttpServerStatus.setText(String.format(" | HTTP server (running) %s, %d connected", mainStatusBarModel.getHttpListenAddr(),mainStatusBarModel.getHttpServerActiveSessions()));
            }
            else {
                pnlMainStatusBarView.lblHttpServerStatus.setText(" | HTTP server (stopped)");
            }
        }

        if ( "MainStatusBarModel.statusMessage".equals(propertyChangeEvent.getPropertyName())) {
            pnlMainStatusBarView.lblStatusMsg.setText((String) propertyChangeEvent.getNewValue());
        }

        if ( "MainStatusBarModel.proxyServerActiveSessions".equals(propertyChangeEvent.getPropertyName()) ) {
            if ( mainStatusBarModel.getProxyListenAddr() != null ) {
                pnlMainStatusBarView.lblProxyStatus.setText(String.format("Proxy server (running) %s, %d connected", mainStatusBarModel.getProxyListenAddr(),mainStatusBarModel.getProxyServerActiveSessions()));
            }
        }
        if ( "MainStatusBarModel.httpServerActiveSessions".equals(propertyChangeEvent.getPropertyName()) ) {
            if ( mainStatusBarModel.getHttpListenAddr() != null ) {
                pnlMainStatusBarView.lblHttpServerStatus.setText(String.format(" | HTTP server (running) %s, %d connected", mainStatusBarModel.getHttpListenAddr(),mainStatusBarModel.getHttpServerActiveSessions()));
            }
        }
    }
}
