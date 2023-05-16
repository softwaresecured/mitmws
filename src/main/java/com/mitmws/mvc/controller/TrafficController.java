package com.mitmws.mvc.controller;
import com.mitmws.mvc.model.ProjectModel;
import com.mitmws.projects.ProjectDataServiceException;
import com.mitmws.util.GuiUtils;
import com.mitmws.httpproxy.trafficlogger.HttpTrafficRecord;
import com.mitmws.httpproxy.trafficlogger.WebsocketTrafficRecord;
import com.mitmws.mvc.model.TrafficModel;
import com.mitmws.mvc.view.panels.trafficpanel.PnlTrafficView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;

public class TrafficController implements PropertyChangeListener {
    private TrafficModel trafficModel;
    private ProjectModel projectModel;
    private PnlTrafficView pnlTrafficView;
    public TrafficController(TrafficModel trafficModel, ProjectModel projectModel, PnlTrafficView pnlTrafficView)  {
        this.trafficModel = trafficModel;
        this.projectModel = projectModel;
        this.pnlTrafficView = pnlTrafficView;
        this.trafficModel.addListener(this);
        initEventListeners();
    }

    public void initEventListeners() {
        /*
            Applies the traffic filter
         */
        pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.pnlHttpTrafficToolbar.jbtnApply.addActionListener( actionEvent -> {
            boolean errors = false;
            if ( !GuiUtils.validateRegex(pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.pnlHttpTrafficToolbar.jtxtProjectScope.getText()) ) {
                pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.pnlHttpTrafficToolbar.jtxtProjectScope.setBackground(GuiUtils.ERROR_COLOR);
                errors = true;
            }
            if ( !GuiUtils.validateRegex(pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.pnlHttpTrafficToolbar.jtxtExlcudeRegex.getText()) ) {
                pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.pnlHttpTrafficToolbar.jtxtExlcudeRegex.setBackground(GuiUtils.ERROR_COLOR);
                errors = true;
            }
            if ( !errors ) {
                trafficModel.setTrafficRowFilters(pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.pnlHttpTrafficToolbar.jtxtProjectScope.getText(),pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.pnlHttpTrafficToolbar.jtxtExlcudeRegex.getText());
            }
        });
        /*
            Http table row select
            - Loads the http message
         */
        pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.tblHttpTraffic.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            int rowId = pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.tblHttpTraffic.getSelectedRow();
            if ( rowId >= 0 ) {
                pnlTrafficView.pnlHttpTraffic.pnlHttpRequestResponsePairViewer.pnlCurHttpRequest.jtxtHttpMessage.setText("");
                String messageId = (String) pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.tblHttpTraffic.getValueAt(rowId, 0);
                if ( messageId != null ) {
                    try {
                        HttpTrafficRecord rec = null;
                        rec = projectModel.getProjectDataService().getHttpTrafficRecordByUUID(messageId);
                        if ( rec != null ) {

                            String requestBody = rec.getRequest().getBodyPreviewString();
                            String responseBody = rec.getResponse().getBodyPreviewString();
                            String request = String.format("%s\r\n%s", new String(rec.getRequest().getHeaderBytes()), requestBody);
                            String response = String.format("%s\r\n%s", new String(rec.getResponse().getHeaderBytes()), responseBody);

                            pnlTrafficView.pnlHttpTraffic.pnlHttpRequestResponsePairViewer.pnlCurHttpRequest.jtxtHttpMessage.setText(request);
                            pnlTrafficView.pnlHttpTraffic.pnlHttpRequestResponsePairViewer.pnlCurHttpresponse.jtxtHttpMessage.setText(response);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ProjectDataServiceException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        /*
            Websocket table row select
            - Loads the websocket record
         */
        pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.tblWebsocketTraffic.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            int rowId = pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.tblWebsocketTraffic.getSelectedRow();
            if ( rowId >= 0 ) {
                String messageId = (String) pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.tblWebsocketTraffic.getValueAt(rowId, 0);
                if ( messageId != null ) {
                    WebsocketTrafficRecord rec = null;
                    try {
                        rec = projectModel.getProjectDataService().getWebsocketTrafficRecordByUUID(messageId);
                        pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.websocketFrameModel.setWebsocketFrame(rec.getFrame());
                        pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.websocketFrameModel.setEditable(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ProjectDataServiceException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        /*
            Webscoket connections row click
            - Selects the conversation for the selected connection
         */
        pnlTrafficView.pnlWebsocketTraffic.pnlConnectionsViewer.tblWebsocketConnections.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            int rowId = pnlTrafficView.pnlWebsocketTraffic.pnlConnectionsViewer.tblWebsocketConnections.getSelectedRow();
            if ( rowId >= 0 ) {
                String messageId = pnlTrafficView.pnlWebsocketTraffic.pnlConnectionsViewer.tblWebsocketConnections.getValueAt(rowId, 0).toString();
                if ( messageId != null ) {
                    if (messageId.equals("--")) {
                        GuiUtils.updateFullWebsocketTableTrafficRowFilter(null,
                                pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.pnlWebsocketTrafficToolbar,
                                pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.tblWebsocketTraffic
                        );
                        pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.tblHttpTraffic.getSelectionModel().clearSelection();
                    }
                    else {
                        // a specific convo
                        GuiUtils.updateFullWebsocketTableTrafficRowFilter(messageId,
                                pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.pnlWebsocketTrafficToolbar,
                                pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.tblWebsocketTraffic
                        );
                        GuiUtils.tableSelectFirst(pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.tblWebsocketTraffic);
                        // select the convo's upgrade message in http
                        for ( int i = 0; i < pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.tblHttpTraffic.getRowCount(); i++ ) {
                            if ( pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.tblHttpTraffic.getValueAt(i, 0).toString().equals(messageId)) {
                                pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.tblHttpTraffic.getSelectionModel().setSelectionInterval(i,i);
                                break;
                            }
                        }
                    }
                }
            }
        });

        /*
            Apply http traffic exclude filter
         */
        pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.pnlHttpTrafficToolbar.jchkExclude.addActionListener( actionEvent -> {
            pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.pnlHttpTrafficToolbar.jbtnApply.doClick();
        });

        /*
            Websocket traffic filter
         */
        pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.pnlWebsocketTrafficToolbar.jbtnApply.addActionListener( actionEvent -> {
            int rowId = pnlTrafficView.pnlWebsocketTraffic.pnlConnectionsViewer.tblWebsocketConnections.getSelectedRow();
            String messageId = null;
            if ( rowId > 0 ) {
                messageId = pnlTrafficView.pnlWebsocketTraffic.pnlConnectionsViewer.tblWebsocketConnections.getValueAt(rowId, 0).toString();
            }
            if ( GuiUtils.validateRegex(pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.pnlWebsocketTrafficToolbar.jtxtPayloadRegex.getText())) {
                pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.pnlWebsocketTrafficToolbar.jtxtPayloadRegex.setBackground(Color.WHITE);
                GuiUtils.updateFullWebsocketTableTrafficRowFilter(messageId, pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.pnlWebsocketTrafficToolbar,pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.tblWebsocketTraffic);
            }
            else {
                pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.pnlWebsocketTrafficToolbar.jtxtPayloadRegex.setBackground(GuiUtils.ERROR_COLOR);
            }
        });
    }

    public void updateHttpTrafficRowFilter() {
        TableRowSorter sorter = new TableRowSorter<>((DefaultTableModel) trafficModel.getHttpTrafficModel());
        RowFilter<DefaultTableModel, Object> rf = null;
        try {
            ArrayList<RowFilter<Object,Object>> rfs = new ArrayList<>();
            if ( trafficModel.getScopeRegex() != null && trafficModel.getScopeRegex().length() > 0 ) {
                rfs.add(RowFilter.regexFilter(trafficModel.getScopeRegex(), 6));
            }
            if ( trafficModel.getExcludeRegex() != null && trafficModel.getExcludeRegex().length() > 0) {
                if ( pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.pnlHttpTrafficToolbar.jchkExclude.isSelected() ) {
                    rfs.add(RowFilter.notFilter(RowFilter.regexFilter(trafficModel.getExcludeRegex(), 6)));
                }
            }
            rf = RowFilter.andFilter(rfs);
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        sorter.setRowFilter(rf);
        pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.tblHttpTraffic.setRowSorter(sorter);
    }

    // Filters the websocket traffic based on upgrade message id, direction, etc
    public void updateWebsocketTrafficRowFilter() {
        // sorter for proxy traffic
        TableRowSorter sorter = new TableRowSorter<>((DefaultTableModel) pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.tblWebsocketTraffic.getModel());
        RowFilter<DefaultTableModel, Object> rf = null;
        try {
            ArrayList<RowFilter<Object,Object>> rfs = new ArrayList<>();
            // Filter for payload, direction, ping|pong
            // Direction - 3
            if ( !pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.pnlWebsocketTrafficToolbar.jcmbDirections.getSelectedItem().toString().equals("Both")) {
                if ( pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.pnlWebsocketTrafficToolbar.jcmbDirections.getSelectedItem().toString().equals("Inbound")) {
                    rfs.add(RowFilter.regexFilter("^<--$", 3));
                }
                else {
                    rfs.add(RowFilter.regexFilter("^-->$", 3));
                }
            }
            // Opcode - 9
            if ( pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.pnlWebsocketTrafficToolbar.jchkHidePingPong.isSelected()) {
                rfs.add(RowFilter.regexFilter("^((?!PING|PONG).)*$", 9));
            }
            // Payload - 12

            // conversation
            int rowId = pnlTrafficView.pnlWebsocketTraffic.pnlConnectionsViewer.tblWebsocketConnections.getSelectedRow();
            if ( rowId > 0 ) {
                String messageId = pnlTrafficView.pnlWebsocketTraffic.pnlConnectionsViewer.tblWebsocketConnections.getValueAt(rowId, 0).toString();
                rfs.add(RowFilter.regexFilter(messageId, 1));
            }

            if ( pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.pnlWebsocketTrafficToolbar.jtxtPayloadRegex.getText().length() > 0 ) {
                rfs.add(RowFilter.regexFilter(pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.pnlWebsocketTrafficToolbar.jtxtPayloadRegex.getText(), 12));
            }
            rf = RowFilter.andFilter(rfs);
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        sorter.setRowFilter(rf);
        pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.tblWebsocketTraffic.setRowSorter(sorter);
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if ( "TrafficModel.rowFilterChange".equals(propertyChangeEvent.getPropertyName())) {
            pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.pnlHttpTrafficToolbar.jtxtProjectScope.setBackground(Color.WHITE);
            pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.pnlHttpTrafficToolbar.jtxtExlcudeRegex.setBackground(Color.WHITE);
            updateHttpTrafficRowFilter();
        }
        if ( "TrafficModel.excludeRegex".equals(propertyChangeEvent.getPropertyName())) {
            pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.pnlHttpTrafficToolbar.jtxtExlcudeRegex.setText(trafficModel.getExcludeRegex());
        }
        if ( "TrafficModel.websocketConnectionsModel".equals(propertyChangeEvent.getPropertyName())) {
            if ( pnlTrafficView.pnlWebsocketTraffic.pnlConnectionsViewer.tblWebsocketConnections.getRowCount() > 0 ) {
                if ( pnlTrafficView.pnlWebsocketTraffic.pnlConnectionsViewer.tblWebsocketConnections.getSelectedRow() == -1 ) {
                    GuiUtils.tableSelectFirst(pnlTrafficView.pnlWebsocketTraffic.pnlConnectionsViewer.tblWebsocketConnections);
                }
            }
        }

        if ( "TrafficModel.websocketTrafficModel".equals(propertyChangeEvent.getPropertyName())) {
            if ( pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.tblWebsocketTraffic.getRowCount() > 0 ) {
                if ( pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.tblWebsocketTraffic.getSelectedRow() == -1 ) {
                    GuiUtils.tableSelectFirst(pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.tblWebsocketTraffic);
                }
            }
        }
    }
}
