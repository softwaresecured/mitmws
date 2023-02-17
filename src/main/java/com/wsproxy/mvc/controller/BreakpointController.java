package com.wsproxy.mvc.controller;

import com.wsproxy.environment.Environment;
import com.wsproxy.environment.EnvironmentItemScope;
import com.wsproxy.httpproxy.BreakPointItem;
import com.wsproxy.httpproxy.trafficlogger.WebsocketDirection;
import com.wsproxy.httpproxy.websocket.WebsocketFrame;
import com.wsproxy.httpproxy.websocket.WebsocketFrameType;
import com.wsproxy.mvc.model.BreakpointModel;
import com.wsproxy.mvc.model.InteractshModel;
import com.wsproxy.mvc.model.LogModel;
import com.wsproxy.mvc.popupmenus.PopupMenuPayloadEditorInsert;
import com.wsproxy.mvc.view.panels.breakpoints.PnlBreakPointsView;
import com.wsproxy.mvc.view.panels.logs.PnlLogs;
import com.wsproxy.util.GuiUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class BreakpointController implements PropertyChangeListener {

    private BreakpointModel breakpointModel;
    private InteractshModel interactshModel;
    private PnlBreakPointsView pnlBreakPointsView;
    private PopupMenuPayloadEditorInsert popupMenuPayloadEditorInsert;
    private Environment environment = new Environment();

    public BreakpointController(BreakpointModel breakpointModel, InteractshModel interactshModel, PnlBreakPointsView pnlBreakPointsView) {
        this.breakpointModel = breakpointModel;
        this.interactshModel = interactshModel;
        this.pnlBreakPointsView = pnlBreakPointsView;
        this.breakpointModel.addListener(this);
        popupMenuPayloadEditorInsert = new PopupMenuPayloadEditorInsert(pnlBreakPointsView.pnlWebsocketFrameView.jtxtFramePayload,interactshModel);
        initEventListeners();
        environment.setInteractshModel(interactshModel);
    }

    public void initEventListeners() {
        pnlBreakPointsView.btnNew.addActionListener( ActiveEvent -> {
            BreakPointItem newItem = new BreakPointItem(
                    Pattern.compile(".*"),
                    Pattern.compile(".*"),
                    WebsocketDirection.BOTH);
            newItem.setName(String.format("Untitled-%d", pnlBreakPointsView.jtblBreakpointRules.getRowCount()));
            breakpointModel.setCurrentBreakpointItem(newItem);
            loadCurrentItem(newItem);
            pnlBreakPointsView.lblWarnings.setVisible(false);
            pnlBreakPointsView.jtblBreakpointRules.getSelectionModel().setSelectionInterval(-1,-1);
        });

        pnlBreakPointsView.btnSave.addActionListener( ActiveEvent -> {
            saveCurrentItem();
            syncBreakpointRules();
            selectCurrentBreakpointItem();
        });

        pnlBreakPointsView.btnDelete.addActionListener( ActiveEvent -> {
            if ( breakpointModel.getCurrentBreakpointItem() != null ) {
                int idx = -1;
                for ( int i = 0; i < breakpointModel.getBreakpointItems().size(); i++ ) {
                    if ( breakpointModel.getBreakpointItems().get(i).getId().equals(breakpointModel.getCurrentBreakpointItem().getId())) {
                        idx = i;
                        break;
                    }
                }
                if ( idx >= 0 ) {
                    breakpointModel.getBreakpointItems().remove(idx);
                }
            }
            breakpointModel.setCurrentBreakpointItem(null);
            syncBreakpointRules();
            loadCurrentItem(null);
            selectCurrentBreakpointItem();
            pnlBreakPointsView.btnNew.setEnabled(true);
            pnlBreakPointsView.lblWarnings.setVisible(false);
        });

        pnlBreakPointsView.jtblBreakpointRules.getSelectionModel().addListSelectionListener( ListSelectionListener -> {
            loadSelectedBreakpointItem();
        });

        pnlBreakPointsView.btnDrop.addActionListener( ActiveEvent -> {
            int rowId = pnlBreakPointsView.jtblBreakpointQueue.getSelectedRow();
            if ( rowId >= 0 ) {
                String id = (String) pnlBreakPointsView.jtblBreakpointQueue.getValueAt(rowId,0);
                for ( String trappedFrameId : breakpointModel.getTrappedFrameMap().keySet() ) {
                    WebsocketFrame frame = breakpointModel.getTrappedFrameMap().get(trappedFrameId);
                    if ( frame != null && frame.getMessageUUID().equals(id) ) {
                        frame.setDropped(true);
                        break;
                    }
                }
            }
        });

        pnlBreakPointsView.btnContinue.addActionListener( ActiveEvent -> {
            int rowId = pnlBreakPointsView.jtblBreakpointQueue.getSelectedRow();
            if ( rowId >= 0 ) {
                String id = (String) pnlBreakPointsView.jtblBreakpointQueue.getValueAt(rowId,0);
                for ( String trappedFrameId : breakpointModel.getTrappedFrameMap().keySet() ) {
                    WebsocketFrame frame = breakpointModel.getTrappedFrameMap().get(trappedFrameId);
                    if ( frame != null && frame.getMessageUUID().equals(id) ) {
                        WebsocketFrame releasedFrame = environment.process(EnvironmentItemScope.WEBSOCKET, pnlBreakPointsView.websocketFrameModel.getWebsocketFrame());
                        breakpointModel.getTrappedFrameMap().put(id,releasedFrame);
                        frame.setTrapped(false);
                        break;
                    }
                }
            }
        });

        pnlBreakPointsView.jtblBreakpointQueue.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int rowId = pnlBreakPointsView.jtblBreakpointQueue.getSelectedRow();
                if ( rowId >= 0 ) {
                    String selectedFrameId = (String) pnlBreakPointsView.jtblBreakpointQueue.getValueAt(rowId,0);
                    WebsocketFrame frame = breakpointModel.getTrappedFrameMap().get(selectedFrameId);
                    if ( frame != null ) {
                        pnlBreakPointsView.websocketFrameModel.setWebsocketFrame(frame);
                    }
                }
                else {
                    pnlBreakPointsView.websocketFrameModel.setWebsocketFrame(null);
                }
                toggleActionsPanel();
            }
        });
        pnlBreakPointsView.pnlFrameTypes.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ( e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1 ) {
                    pnlBreakPointsView.jchkContinuation.setSelected(true);
                    pnlBreakPointsView.jchkText.setSelected(true);
                    pnlBreakPointsView.jchkBin.setSelected(true);
                    pnlBreakPointsView.jchkRes1.setSelected(true);
                    pnlBreakPointsView.jchkRes2.setSelected(true);
                    pnlBreakPointsView.jchkRes3.setSelected(true);
                    pnlBreakPointsView.jchkRes4.setSelected(true);
                    pnlBreakPointsView.jchkRes5.setSelected(true);
                    pnlBreakPointsView.jchkClose.setSelected(true);
                    pnlBreakPointsView.jchkPing.setSelected(true);
                    pnlBreakPointsView.jchkPong.setSelected(true);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

    }



    public void togglePanelComponents( boolean status ) {
        for ( JPanel panel : new JPanel[]{ pnlBreakPointsView.pnlSettingsHeader, pnlBreakPointsView.pnlFrameTypes, pnlBreakPointsView.pnlSettingsFooter}) {
            for ( Component c : panel.getComponents()) {
                c.setEnabled(status);
            }
        }
        pnlBreakPointsView.pnlWebsocketFrameView.setEnabled(status);
    }

    public boolean validateNewBreakpoint() {
        pnlBreakPointsView.lblWarnings.setVisible(false);
        pnlBreakPointsView.jtxtConversationScopeRegex.setBackground(Color.WHITE);
        pnlBreakPointsView.jtxtPayloadScopeRegex.setBackground(Color.WHITE);

        if ( breakpointModel.getCurrentBreakpointItem() == null ) {
            return false;
        }

        if( getSelectedFrameScopes().size() == 0 ) {
            pnlBreakPointsView.lblWarnings.setVisible(true);
            pnlBreakPointsView.lblWarnings.setText("At least one frame type must be selected");
            return false;
        }


        try {
            Pattern p = Pattern.compile(pnlBreakPointsView.jtxtConversationScopeRegex.getText());
        } catch ( PatternSyntaxException e ) {
            pnlBreakPointsView.jtxtConversationScopeRegex.setBackground(new Color(240,128,128));
            pnlBreakPointsView.lblWarnings.setVisible(true);
            pnlBreakPointsView.lblWarnings.setText("Invalid conversation scope regex");
            return false;
        }

        try {
            Pattern p = Pattern.compile(pnlBreakPointsView.jtxtPayloadScopeRegex.getText());
        } catch ( PatternSyntaxException e ) {
            pnlBreakPointsView.jtxtPayloadScopeRegex.setBackground(new Color(240,128,128));
            pnlBreakPointsView.lblWarnings.setVisible(true);
            pnlBreakPointsView.lblWarnings.setText("Invalid payload scope regex");
            return false;
        }

        return true;
    }


    public void selectCurrentBreakpointItem() {
        pnlBreakPointsView.jtblBreakpointRules.getSelectionModel().setSelectionInterval(-1,-1);
        if ( breakpointModel.getCurrentBreakpointItem() != null ) {
            int idx = -1;
            for ( int i = 0; i < breakpointModel.getBreakpointTableModel().getRowCount(); i++ ) {
                String curId = (String) breakpointModel.getBreakpointTableModel().getValueAt(i,0);
                if ( curId.equals(breakpointModel.getCurrentBreakpointItem().getId())) {
                    idx = i;
                    break;
                }
            }
            if ( idx >= 0 ) {
                pnlBreakPointsView.jtblBreakpointRules.getSelectionModel().setSelectionInterval(idx, idx);
            }
        }

    }

    // Makes sure the table reflects what is stored in the arraylist of breakpoints
    public void syncBreakpointRules() {
        System.out.println("Syncing breakpoint rules");
        // Remove
        ArrayList<String> currentItemIds = new ArrayList<String>();
        ArrayList<String> currentTableItemIds = new ArrayList<String>();
        ArrayList<String> pruneItemIds = new ArrayList<String>();
        for ( BreakPointItem item : breakpointModel.getBreakpointItems() ) {
            currentItemIds.add(item.getId());
        }
        System.out.println(String.format("There are currently %d items", currentItemIds.size()));
        for ( int i = 0; i < breakpointModel.getBreakpointTableModel().getRowCount(); i++ ) {
            String curId = (String) breakpointModel.getBreakpointTableModel().getValueAt(i,0);
            if ( !currentItemIds.contains(curId)) {
                pruneItemIds.add(curId);
            }
        }

        for ( String pruneId : pruneItemIds ) {
            for ( int i = 0; i < breakpointModel.getBreakpointTableModel().getRowCount(); i++ ) {
                String curId = (String) breakpointModel.getBreakpointTableModel().getValueAt(i,0);
                if ( pruneId.equals(curId)) {
                    breakpointModel.getBreakpointTableModel().removeRow(i);
                    break;
                }
            }
        }
        // Update
        for ( int i = 0; i < breakpointModel.getBreakpointTableModel().getRowCount(); i++ ) {
            String curId = (String) breakpointModel.getBreakpointTableModel().getValueAt(i,0);
            currentTableItemIds.add(curId);
            for ( BreakPointItem item : breakpointModel.getBreakpointItems() ) {
                if ( item.getId().equals(curId)) {
                    breakpointModel.getBreakpointTableModel().setValueAt(item.getName(),i,1);
                    breakpointModel.getBreakpointTableModel().setValueAt(item.getConversationScope().pattern(),i,2);
                    breakpointModel.getBreakpointTableModel().setValueAt(item.getPayloadScope().pattern(),i,3);
                    break;
                }
            }

        }

        // Add
        for ( BreakPointItem item : breakpointModel.getBreakpointItems() ) {
            if ( !currentTableItemIds.contains(item.getId())) {
                breakpointModel.getBreakpointTableModel().addRow(new Object[] {
                        item.getId(),
                        item.getName(),
                        item.getConversationScope(),
                        item.getPayloadScope()
                });
            }
        }
    }

    // Gets the currently selected frame scopes
    public ArrayList<WebsocketFrameType> getSelectedFrameScopes() {
        ArrayList<WebsocketFrameType> websocketFrameTypes = new ArrayList<WebsocketFrameType>();
        if (pnlBreakPointsView.jchkContinuation.isSelected()) {
            websocketFrameTypes.add(WebsocketFrameType.CONTINUATION);
        }
        if (pnlBreakPointsView.jchkText.isSelected()) {
            websocketFrameTypes.add(WebsocketFrameType.TEXT);
        }
        if (pnlBreakPointsView.jchkBin.isSelected()) {
            websocketFrameTypes.add(WebsocketFrameType.BINARY);
        }
        if (pnlBreakPointsView.jchkRes1.isSelected()) {
            websocketFrameTypes.add(WebsocketFrameType.RESERVED1);
        }
        if (pnlBreakPointsView.jchkRes2.isSelected()) {
            websocketFrameTypes.add(WebsocketFrameType.RESERVED2);
        }
        if (pnlBreakPointsView.jchkRes3.isSelected()) {
            websocketFrameTypes.add(WebsocketFrameType.RESERVED3);
        }
        if (pnlBreakPointsView.jchkRes4.isSelected()) {
            websocketFrameTypes.add(WebsocketFrameType.RESERVED4);
        }
        if (pnlBreakPointsView.jchkRes5.isSelected()) {
            websocketFrameTypes.add(WebsocketFrameType.RESERVED5);
        }
        if (pnlBreakPointsView.jchkClose.isSelected()) {
            websocketFrameTypes.add(WebsocketFrameType.CLOSE);
        }

        if (pnlBreakPointsView.jchkPing.isSelected()) {
            websocketFrameTypes.add(WebsocketFrameType.PING);
        }
        if (pnlBreakPointsView.jchkPong.isSelected()) {
            websocketFrameTypes.add(WebsocketFrameType.PONG);
        }
        return websocketFrameTypes;
    }

    // Saves the UI to the current breakpoint
    public void saveCurrentItem() {
        pnlBreakPointsView.jtxtConversationScopeRegex.setBackground(Color.WHITE);
        pnlBreakPointsView.jtxtPayloadScopeRegex.setBackground(Color.WHITE);
        if ( validateNewBreakpoint() ) {
            Pattern scopePattern = Pattern.compile(pnlBreakPointsView.jtxtConversationScopeRegex.getText());
            Pattern payloadPattern = Pattern.compile(pnlBreakPointsView.jtxtPayloadScopeRegex.getText());

            breakpointModel.getCurrentBreakpointItem().setName(pnlBreakPointsView.jtxtName.getText());
            breakpointModel.getCurrentBreakpointItem().setConversationScope(scopePattern);
            breakpointModel.getCurrentBreakpointItem().setPayloadScope(payloadPattern);

            breakpointModel.getCurrentBreakpointItem().resetFrameTypeScope();


            breakpointModel.getCurrentBreakpointItem().setFrameTypeScope(getSelectedFrameScopes());

            if (pnlBreakPointsView.jradioBreakInbound.isSelected()) {
                breakpointModel.getCurrentBreakpointItem().setWebsocketDirection(WebsocketDirection.INBOUND);
            }
            if (pnlBreakPointsView.jradioBreakOutbound.isSelected()) {
                breakpointModel.getCurrentBreakpointItem().setWebsocketDirection(WebsocketDirection.OUTBOUND);
            }
            if (pnlBreakPointsView.jradioBreakBoth.isSelected()) {
                breakpointModel.getCurrentBreakpointItem().setWebsocketDirection(WebsocketDirection.BOTH);
            }

            boolean exists = false;
            for ( BreakPointItem item : breakpointModel.getBreakpointItems() ) {
                if ( item.getId().equals(breakpointModel.getCurrentBreakpointItem().getId())) {
                    System.out.println("EXISTS");
                    exists = true;
                    item.setName(breakpointModel.getCurrentBreakpointItem().getName());
                    item.setConversationScope(breakpointModel.getCurrentBreakpointItem().getConversationScope());
                    item.setPayloadScope(breakpointModel.getCurrentBreakpointItem().getPayloadScope());
                    item.setFrameTypeScope(breakpointModel.getCurrentBreakpointItem().getFrameTypeScope());
                    item.setWebsocketDirection(breakpointModel.getCurrentBreakpointItem().getWebsocketDirection());
                }

            }
            if ( !exists ) {
                System.out.println("NOT EXISTS");
                breakpointModel.getBreakpointItems().add(breakpointModel.getCurrentBreakpointItem());
            }
        }
        else {
            System.out.println("Validation failed");
        }
    }


    public void loadSelectedBreakpointItem() {
        if ( pnlBreakPointsView.jtblBreakpointRules.getSelectedRow() >= 0 ) {
            String id = (String) pnlBreakPointsView.jtblBreakpointRules.getValueAt(pnlBreakPointsView.jtblBreakpointRules.getSelectedRow(),0);
            for ( BreakPointItem item : breakpointModel.getBreakpointItems() ) {
                if ( item.getId().equals(id)) {
                    breakpointModel.setCurrentBreakpointItem(item);
                    loadCurrentItem(item);
                }
            }
        }
    }


    // Loads the current breakpoint item into the UI
    public void loadCurrentItem(BreakPointItem item ) {

        pnlBreakPointsView.jtxtName.setEnabled(false);
        pnlBreakPointsView.jtxtConversationScopeRegex.setEnabled(false);
        pnlBreakPointsView.jtxtPayloadScopeRegex.setEnabled(false);
        pnlBreakPointsView.jradioBreakInbound.setEnabled(false);
        pnlBreakPointsView.jradioBreakOutbound.setEnabled(false);
        pnlBreakPointsView.jradioBreakBoth.setEnabled(false);

        pnlBreakPointsView.jtxtConversationScopeRegex.setBackground(Color.WHITE);
        pnlBreakPointsView.jtxtPayloadScopeRegex.setBackground(Color.WHITE);

        pnlBreakPointsView.jchkContinuation.setEnabled(false);
        pnlBreakPointsView.jchkText.setEnabled(false);
        pnlBreakPointsView.jchkBin.setEnabled(false);
        pnlBreakPointsView.jchkRes1.setEnabled(false);
        pnlBreakPointsView.jchkRes2.setEnabled(false);
        pnlBreakPointsView.jchkRes3.setEnabled(false);
        pnlBreakPointsView.jchkRes4.setEnabled(false);
        pnlBreakPointsView.jchkRes5.setEnabled(false);
        pnlBreakPointsView.jchkClose.setEnabled(false);
        pnlBreakPointsView.jchkPing.setEnabled(false);
        pnlBreakPointsView.jchkPong.setEnabled(false);

        pnlBreakPointsView.jtxtName.setText("");
        pnlBreakPointsView.jtxtConversationScopeRegex.setText("");
        pnlBreakPointsView.jtxtPayloadScopeRegex.setText("");

        pnlBreakPointsView.btnSave.setEnabled(false);
        pnlBreakPointsView.btnDelete.setEnabled(false);

        if ( item != null ) {

            pnlBreakPointsView.jtxtName.setEnabled(true);
            pnlBreakPointsView.jtxtConversationScopeRegex.setEnabled(true);
            pnlBreakPointsView.jtxtPayloadScopeRegex.setEnabled(true);
            pnlBreakPointsView.jradioBreakInbound.setEnabled(true);
            pnlBreakPointsView.jradioBreakOutbound.setEnabled(true);
            pnlBreakPointsView.jradioBreakBoth.setEnabled(true);


            pnlBreakPointsView.btnSave.setEnabled(true);
            pnlBreakPointsView.btnDelete.setEnabled(true);

            pnlBreakPointsView.jchkContinuation.setEnabled(true);
            pnlBreakPointsView.jchkText.setEnabled(true);
            pnlBreakPointsView.jchkBin.setEnabled(true);
            pnlBreakPointsView.jchkRes1.setEnabled(true);
            pnlBreakPointsView.jchkRes2.setEnabled(true);
            pnlBreakPointsView.jchkRes3.setEnabled(true);
            pnlBreakPointsView.jchkRes4.setEnabled(true);
            pnlBreakPointsView.jchkRes5.setEnabled(true);
            pnlBreakPointsView.jchkClose.setEnabled(true);
            pnlBreakPointsView.jchkPing.setEnabled(true);
            pnlBreakPointsView.jchkPong.setEnabled(true);

            pnlBreakPointsView.jchkContinuation.setSelected(false);
            pnlBreakPointsView.jchkText.setSelected(false);
            pnlBreakPointsView.jchkBin.setSelected(false);
            pnlBreakPointsView.jchkRes1.setSelected(false);
            pnlBreakPointsView.jchkRes2.setSelected(false);
            pnlBreakPointsView.jchkRes3.setSelected(false);
            pnlBreakPointsView.jchkRes4.setSelected(false);
            pnlBreakPointsView.jchkRes5.setSelected(false);
            pnlBreakPointsView.jchkClose.setSelected(false);
            pnlBreakPointsView.jchkPing.setSelected(false);
            pnlBreakPointsView.jchkPong.setSelected(false);


            pnlBreakPointsView.jtxtName.setText(item.getName());
            pnlBreakPointsView.jtxtConversationScopeRegex.setText(item.getConversationScope().pattern());
            pnlBreakPointsView.jtxtPayloadScopeRegex.setText(item.getPayloadScope().pattern());

            switch ( item.getWebsocketDirection()) {
                case INBOUND:
                    pnlBreakPointsView.jradioBreakInbound.setSelected(true);
                    break;

                case OUTBOUND:
                    pnlBreakPointsView.jradioBreakOutbound.setSelected(true);
                    break;

                case BOTH:
                    pnlBreakPointsView.jradioBreakBoth.setSelected(true);
                    break;
            }

            for ( WebsocketFrameType websocketFrameType : item.getFrameTypeScope() ) {
                switch ( websocketFrameType ) {
                    case CONTINUATION:
                        pnlBreakPointsView.jchkContinuation.setSelected(true);
                        break;
                    case TEXT:
                        pnlBreakPointsView.jchkText.setSelected(true);
                        break;
                    case BINARY:
                        pnlBreakPointsView.jchkBin.setSelected(true);
                        break;
                    case RESERVED1:
                        pnlBreakPointsView.jchkRes1.setSelected(true);
                        break;
                    case RESERVED2:
                        pnlBreakPointsView.jchkRes2.setSelected(true);
                        break;
                    case RESERVED3:
                        pnlBreakPointsView.jchkRes3.setSelected(true);
                        break;
                    case RESERVED4:
                        pnlBreakPointsView.jchkRes4.setSelected(true);
                        break;
                    case RESERVED5:
                        pnlBreakPointsView.jchkRes5.setSelected(true);
                        break;
                    case CLOSE:
                        pnlBreakPointsView.jchkClose.setSelected(true);
                        break;
                    case PING:
                        pnlBreakPointsView.jchkPing.setSelected(true);
                        break;
                    case PONG:
                        pnlBreakPointsView.jchkPong.setSelected(true);
                        break;
                }
            }
        }
    }

    public void syncTrappedFrames() {
        // Current frames in the table
        ArrayList<String> currentTableFrameIds = new ArrayList<String>();
        ArrayList<String> currentFrameIds = new ArrayList<String>();
        for ( int i = 0; i < breakpointModel.getBreakpointQueueTableModel().getRowCount(); i++ ) {
            currentTableFrameIds.add((String) breakpointModel.getBreakpointQueueTableModel().getValueAt(i,0));
        }
        for ( WebsocketFrame frame : breakpointModel.getTrappedFrames() ) {
            currentFrameIds.add(frame.getMessageUUID());
        }

        // remove
        ArrayList<String> removeList = new ArrayList<String>();
        for ( int i = 0; i < breakpointModel.getBreakpointQueueTableModel().getRowCount(); i++ ) {
            String curId = (String) breakpointModel.getBreakpointQueueTableModel().getValueAt(i,0);
            if ( !currentFrameIds.contains(curId)) {
                removeList.add(curId);
            }
        }

        for ( String curId : removeList ) {
            for ( int i = 0; i < breakpointModel.getBreakpointQueueTableModel().getRowCount(); i++ ) {
                String curRowId = (String) breakpointModel.getBreakpointQueueTableModel().getValueAt(i,0);
                if ( curRowId.equals(curId)) {
                    breakpointModel.getBreakpointQueueTableModel().removeRow(i);
                    break;
                }
            }
        }

        // Add
        for ( WebsocketFrame frame : breakpointModel.getTrappedFrames() ) {
            if ( !currentTableFrameIds.contains(frame.getMessageUUID())) {
                try {
                    String host = new URL(frame.getUpgradeUrl()).getHost();
                    breakpointModel.getBreakpointQueueTableModel().addRow(new Object[] {
                            frame.getMessageUUID(),
                            frame.getDirection() == WebsocketDirection.INBOUND ? "←" : "→",
                            host,
                            frame.getPayloadString()
                    });
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

        if ( pnlBreakPointsView.jtblBreakpointQueue.getSelectedRow() == -1 ) {
            GuiUtils.tableSelectFirst(pnlBreakPointsView.jtblBreakpointQueue);
        }
        toggleActionsPanel();
    }

    public void toggleActionsPanel() {

        for ( Component c : pnlBreakPointsView.pnlActions.getComponents() ) {
            c.setEnabled(pnlBreakPointsView.jtblBreakpointQueue.getRowCount() == 0 ? false : true);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if ( "BreakpointModel.trappedFrame".equals(propertyChangeEvent.getPropertyName())) {
            syncTrappedFrames();
        }

        if ( "BreakpointModel.releasedFrame".equals(propertyChangeEvent.getPropertyName())) {
            syncTrappedFrames();
        }

        if ( "BreakpointModel.currentBreakpointItem".equals(propertyChangeEvent.getPropertyName())) {
            syncTrappedFrames();
        }

        if ( "BreakpointModel.droppedFrameRemoved".equals(propertyChangeEvent.getPropertyName())) {
            syncTrappedFrames();
        }

        if ( "BreakpointModel.droppedFrame".equals(propertyChangeEvent.getPropertyName())) {
            syncTrappedFrames();
        }


        if ( "BreakpointModel.currentBreakpointItem".equals(propertyChangeEvent.getPropertyName())) {

            if ( propertyChangeEvent.getNewValue() == null ) {
                togglePanelComponents(false);
                pnlBreakPointsView.pnlWebsocketFrameView.jtxtFramePayload.setEnabled(false);
            }
            else {
                togglePanelComponents(true);
                pnlBreakPointsView.pnlWebsocketFrameView.jtxtFramePayload.setEnabled(true);
                loadCurrentItem((BreakPointItem) propertyChangeEvent.getNewValue());
            }
        }
    }
}
