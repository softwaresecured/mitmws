package com.mitmws.mvc.controller;

import com.mitmws.mvc.model.MainModel;
import com.mitmws.mvc.thread.TrafficSearchThread;
import com.mitmws.projects.ProjectDataServiceException;
import com.mitmws.util.GuiUtils;
import com.mitmws.httpproxy.trafficlogger.*;
import com.mitmws.mvc.view.panels.search.PnlTrafficSearchView;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class TrafficSearchController implements PropertyChangeListener {

    private PnlTrafficSearchView pnlTrafficSearchView;
    private MainModel mainModel;
    public TrafficSearchController( MainModel mainModel, PnlTrafficSearchView pnlTrafficSearchView) {
        this.mainModel = mainModel;
        this.pnlTrafficSearchView = pnlTrafficSearchView;
        this.mainModel.getTrafficSearchModel().addListener(this);
        this.mainModel.addListener(this);
        initEventListeners();
    }

    public void initEventListeners() {
        /*
            Search
         */
        pnlTrafficSearchView.btnSearch.addActionListener(actionEvent -> {
            this.mainModel.getTrafficSearchModel().setSearchText(pnlTrafficSearchView.jtxtSearch.getText());
            if ( mainModel.getTrafficSearchThread() == null ) {
                GuiUtils.clearTable(pnlTrafficSearchView.tblSearchResults);
                pnlTrafficSearchView.jtxtSearch.setBackground(Color.WHITE);
                if ( pnlTrafficSearchView.jchkRegex.isSelected() ) {
                    if ( !validateSearchRegex()) {
                        pnlTrafficSearchView.jtxtSearch.setBackground(new Color(240,128,128));
                        return;
                    }
                }
                mainModel.setTrafficSearchThread(new TrafficSearchThread(mainModel));
                mainModel.getTrafficSearchThread().start();
            }
            else {
                mainModel.getTrafficSearchThread().shutdown();
            }
        });
        /*
            Spinner
         */
        pnlTrafficSearchView.jspnMaxResults.addChangeListener( changeEvent -> {
            this.mainModel.getTrafficSearchModel().setMaxResults((Integer) pnlTrafficSearchView.jspnMaxResults.getValue());
        });

        /*
            Checkboxes
         */
        pnlTrafficSearchView.jchkRegex.addChangeListener( changeEvent -> {
            this.mainModel.getTrafficSearchModel().setSearchRegex(pnlTrafficSearchView.jchkRegex.isSelected());
        });
        pnlTrafficSearchView.jchkProxySource.addChangeListener( changeEvent -> {
            this.mainModel.getTrafficSearchModel().setSearchProxy(pnlTrafficSearchView.jchkProxySource.isSelected());
        });
        pnlTrafficSearchView.jchkManualTesterSource.addChangeListener( changeEvent -> {
            this.mainModel.getTrafficSearchModel().setSearchManualTester(pnlTrafficSearchView.jchkManualTesterSource.isSelected());
        });
        pnlTrafficSearchView.jchkAutomatedTesterSource.addChangeListener( changeEvent -> {
            this.mainModel.getTrafficSearchModel().setSearchAutomatedTester(pnlTrafficSearchView.jchkAutomatedTesterSource.isSelected());
        });
        pnlTrafficSearchView.jchkImmediateSource.addChangeListener( changeEvent -> {
            this.mainModel.getTrafficSearchModel().setSearchImmediate(pnlTrafficSearchView.jchkImmediateSource.isSelected());
        });
        /*
            Hit enter for search
         */
        pnlTrafficSearchView.jtxtSearch.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {

            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
                    pnlTrafficSearchView.btnSearch.doClick();
                }
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {

            }
        });

        /*
            Results row select
         */
        pnlTrafficSearchView.tblSearchResults.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            String selectedId = null;
            String trafficRecordType = null;
            if ( pnlTrafficSearchView.tblSearchResults.getSelectedRow() >= 0 ) {
                selectedId = (String) pnlTrafficSearchView.tblSearchResults.getValueAt(pnlTrafficSearchView.tblSearchResults.getSelectedRow(),0);
                trafficRecordType = (String) pnlTrafficSearchView.tblSearchResults.getValueAt(pnlTrafficSearchView.tblSearchResults.getSelectedRow(),1);
                loadTextSearchResult(selectedId);
            }
        });
    }

    /*
        Loads the currently selected record into the textbox at the bottom
     */
    public void loadTextSearchResult(String recordId ) {
        String recText = null;
        try {
            WebsocketTrafficRecord rec = mainModel.getProjectModel().getProjectDataService().getWebsocketTrafficRecordByUUID(recordId);
            if ( rec.getFrame() != null ) {
                if ( rec.getFrame().getPayloadUnmasked() != null ) {
                    recText = new String(rec.getFrame().getPayloadUnmasked());
                    mainModel.getTrafficSearchModel().setSearchResultText(recText);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ProjectDataServiceException e) {
            e.printStackTrace();
        }
    }

    public boolean validateSearchRegex() {
        try {
            Pattern p = Pattern.compile(pnlTrafficSearchView.jtxtSearch.getText());
        }
        catch ( PatternSyntaxException e ) {
            return false;
        }
        return true;
    }



    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if ( "MainModel.trafficSearchThread".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() == null ) {
                pnlTrafficSearchView.btnSearch.setText("Search");
            }
            else {
                pnlTrafficSearchView.btnSearch.setText("Stop");
            }
        }

        if ( "TrafficSearchModel.maxResults".equals(propertyChangeEvent.getPropertyName())) {
            pnlTrafficSearchView.jspnMaxResults.setValue((int)propertyChangeEvent.getNewValue());
        }
        if ( "TrafficSearchModel.searchProxy".equals(propertyChangeEvent.getPropertyName())) {
            this.pnlTrafficSearchView.jchkProxySource.setSelected((boolean)propertyChangeEvent.getNewValue());
        }
        if ( "TrafficSearchModel.searchManualTester".equals(propertyChangeEvent.getPropertyName())) {
            this.pnlTrafficSearchView.jchkManualTesterSource.setSelected((boolean)propertyChangeEvent.getNewValue());
        }
        if ( "TrafficSearchModel.searchAutomatedTester".equals(propertyChangeEvent.getPropertyName())) {
            this.pnlTrafficSearchView.jchkAutomatedTesterSource.setSelected((boolean)propertyChangeEvent.getNewValue());
        }
        if ( "TrafficSearchModel.searchImmediate".equals(propertyChangeEvent.getPropertyName())) {
            this.pnlTrafficSearchView.jchkImmediateSource.setSelected((boolean)propertyChangeEvent.getNewValue());
        }
        if ( "TrafficSearchModel.searchRegex".equals(propertyChangeEvent.getPropertyName())) {
            this.pnlTrafficSearchView.jtxtSearch.setText((String)propertyChangeEvent.getNewValue());
        }
        if ( "TrafficSearchModel.searchResultText".equals(propertyChangeEvent.getPropertyName())) {
            this.pnlTrafficSearchView.jtxtTextResult.setText((String)propertyChangeEvent.getNewValue());
        }

    }
}
