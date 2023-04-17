package com.wsproxy.mvc.controller;

import com.wsproxy.mvc.model.PayloadsModel;
import com.wsproxy.mvc.view.frames.FrmPayloadsView;
import com.wsproxy.tester.PayloadList;
import com.wsproxy.util.GuiUtils;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PayloadsController implements PropertyChangeListener {

    private PayloadsModel payloadsModel;
    private FrmPayloadsView frmPayloadsView;

    public PayloadsController(PayloadsModel payloadsModel, FrmPayloadsView frmPayloadsView) {
        this.payloadsModel = payloadsModel;
        this.frmPayloadsView = frmPayloadsView;
        this.payloadsModel.addListener(this);
        initEventListeners();
        payloadsModel.reloadPayloads(true);
        GuiUtils.tableSelectFirst(frmPayloadsView.jtblPayloadLists);
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

        frmPayloadsView.pnlAutomatedTesterPayloadsToolbar.jbtnReload.addActionListener(actionEvent -> {
            payloadsModel.reloadPayloads();
        });

        frmPayloadsView.pnlAutomatedTesterPayloadsToolbar.jbtnSave.addActionListener(actionEvent -> {
            int rowId = frmPayloadsView.jtblPayloadLists.getSelectedRow();
            if ( rowId >= 0 ) {
                String listName = (String) frmPayloadsView.jtblPayloadLists.getValueAt(rowId, 1);
                if (listName != null) {
                    if ( listName.equals("Custom")) {
                        payloadsModel.updateCustomPayloadList(frmPayloadsView.jtxtLayloadList.getText());
                        GuiUtils.tableSelectFirst(frmPayloadsView.jtblPayloadLists);
                    }
                }
            }
        });

        frmPayloadsView.jtblPayloadLists.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            int rowId = frmPayloadsView.jtblPayloadLists.getSelectedRow();
            if ( rowId >= 0 ) {
                String listName = (String) frmPayloadsView.jtblPayloadLists.getValueAt(rowId,1);
                if ( listName != null ) {
                    // update the buttons
                    frmPayloadsView.pnlAutomatedTesterPayloadsToolbar.jbtnSave.setEnabled(listName.equals("Custom"));
                    frmPayloadsView.jtxtLayloadList.setEditable(listName.equals("Custom"));
                    // update the list
                    for ( PayloadList payloadList : payloadsModel.getPayloadLibrary() ) {
                        if ( payloadList.getPayloadListName().equals(listName)) {
                            StringBuilder sb = new StringBuilder();
                            for ( String payload : payloadList.getPayloads()) {
                                sb.append(String.format("%s\n", payload));
                            }
                            frmPayloadsView.jtxtLayloadList.setText(sb.toString());
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
            frmPayloadsView.pnlAutomatedTesterPayloadsToolbar.jlblEnabledPayloadCount.setText(String.format("%d payloads enabled", (Integer)propertyChangeEvent.getNewValue()));
        }
    }
}
