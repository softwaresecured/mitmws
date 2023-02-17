package com.wsproxy.mvc.controller;

import com.wsproxy.mvc.model.PayloadsModel;
import com.wsproxy.mvc.view.panels.payloads.PnlPayloadsView;
import com.wsproxy.tester.PayloadList;
import com.wsproxy.util.GuiUtils;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class PayloadsController implements PropertyChangeListener {

    private PayloadsModel payloadsModel;
    private PnlPayloadsView pnlPayloadsView;

    public PayloadsController(PayloadsModel payloadsModel, PnlPayloadsView pnlPayloadsView) {
        this.payloadsModel = payloadsModel;
        this.pnlPayloadsView = pnlPayloadsView;
        this.payloadsModel.addListener(this);
        initEventListeners();
        payloadsModel.reloadPayloads(true);
        GuiUtils.tableSelectFirst(pnlPayloadsView.jtblPayloadLists);
    }

    public void initEventListeners() {

        payloadsModel.getPayloadsTableModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if ( e.getFirstRow() >= 0 && payloadsModel.getPayloadsTableModel().getRowCount() > 0 ) {
                    boolean enabled = (boolean) payloadsModel.getPayloadsTableModel().getValueAt(e.getFirstRow(),0);
                    String name = (String) payloadsModel.getPayloadsTableModel().getValueAt(e.getFirstRow(),1);
                    if ( name != null ) {
                        for ( PayloadList payloadList : payloadsModel.getPayloadLibrary() ) {
                            if ( payloadList.getPayloadListName().equals(name)) {
                                payloadList.setEnabled(enabled);
                                payloadsModel.updateEnabledPayloadCount();
                                break;
                            }
                        }
                    }

                }
            }
        });

        pnlPayloadsView.pnlAutomatedTesterPayloadsToolbar.jbtnReload.addActionListener(actionEvent -> {
            payloadsModel.reloadPayloads();
        });

        pnlPayloadsView.pnlAutomatedTesterPayloadsToolbar.jbtnSave.addActionListener(actionEvent -> {
            int rowId = pnlPayloadsView.jtblPayloadLists.getSelectedRow();
            if ( rowId >= 0 ) {
                String listName = (String) pnlPayloadsView.jtblPayloadLists.getValueAt(rowId, 1);
                if (listName != null) {
                    if ( listName.equals("Custom")) {
                        payloadsModel.updateCustomPayloadList(pnlPayloadsView.jtxtLayloadList.getText());
                        GuiUtils.tableSelectFirst(pnlPayloadsView.jtblPayloadLists);
                    }
                }
            }
        });

        pnlPayloadsView.jtblPayloadLists.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            int rowId = pnlPayloadsView.jtblPayloadLists.getSelectedRow();
            if ( rowId >= 0 ) {
                String listName = (String) pnlPayloadsView.jtblPayloadLists.getValueAt(rowId,1);
                if ( listName != null ) {
                    // update the buttons
                    pnlPayloadsView.pnlAutomatedTesterPayloadsToolbar.jbtnSave.setEnabled(listName.equals("Custom"));
                    pnlPayloadsView.jtxtLayloadList.setEditable(listName.equals("Custom"));
                    // update the list
                    for ( PayloadList payloadList : payloadsModel.getPayloadLibrary() ) {
                        if ( payloadList.getPayloadListName().equals(listName)) {
                            StringBuilder sb = new StringBuilder();
                            for ( String payload : payloadList.getPayloads()) {
                                sb.append(String.format("%s\n", payload));
                            }
                            pnlPayloadsView.jtxtLayloadList.setText(sb.toString());
                            break;
                        }
                    }
                }
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if ( "PayloadsModel.enabledPayloadCount".equals(propertyChangeEvent.getPropertyName())) {
            pnlPayloadsView.pnlAutomatedTesterPayloadsToolbar.jlblEnabledPayloadCount.setText(String.format("%d payloads enabled", (Integer)propertyChangeEvent.getNewValue()));
        }
    }
}
