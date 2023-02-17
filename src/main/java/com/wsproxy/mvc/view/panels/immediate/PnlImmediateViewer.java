package com.wsproxy.mvc.view.panels.immediate;

import com.wsproxy.mvc.controller.WebsocketFrameController;
import com.wsproxy.mvc.model.WebsocketFrameModel;
import com.wsproxy.mvc.view.panels.PnlWebsocketTrafficToolbar;
import com.wsproxy.mvc.view.panels.trafficpanel.PnlWebsocketFrameView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.EventObject;

public class PnlImmediateViewer extends JPanel {
    public PnlWebsocketTrafficToolbar pnlWebsocketTrafficToolbar;
    public JTable tblWebsocketConversation;
    public PnlWebsocketFrameView pnlFrameViewer;
    public WebsocketFrameModel websocketFrameModel;
    public WebsocketFrameController websocketFrameController;
    private DefaultTableModel trafficTableModel;
    private JSplitPane splt;
    public PnlImmediateViewer(DefaultTableModel trafficTableModel) {
        this.trafficTableModel = trafficTableModel;
        initLayout();
    }

    public void initLayout() {

        pnlWebsocketTrafficToolbar = new PnlWebsocketTrafficToolbar();
        websocketFrameModel = new WebsocketFrameModel(null);
        pnlFrameViewer = new PnlWebsocketFrameView(websocketFrameModel);
        websocketFrameController = new WebsocketFrameController(websocketFrameModel,pnlFrameViewer);
        tblWebsocketConversation = new JTable(trafficTableModel) {
            public boolean editCellAt(int row, int column, EventObject e) {
                return false;
            }
        };
        tblWebsocketConversation.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] wsConvoColWidths = { -1,160,140,35,90,35};
        for ( int i = 0; i < wsConvoColWidths.length; i++ ) {
            tblWebsocketConversation.getColumnModel().getColumn(i).setMinWidth(wsConvoColWidths[i]);
            tblWebsocketConversation.getColumnModel().getColumn(i).setMaxWidth(wsConvoColWidths[i]);
            tblWebsocketConversation.getColumnModel().getColumn(i).setPreferredWidth(wsConvoColWidths[i]);
        }

        JScrollPane scrollConversationHistoryViewer = new JScrollPane(tblWebsocketConversation);
        scrollConversationHistoryViewer.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollConversationHistoryViewer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        /*
            Top panel
         */

        JPanel pnlTop = new JPanel();
        pnlTop.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        pnlTop.add(pnlWebsocketTrafficToolbar,gbc);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.weightx = 1;
        pnlTop.add(scrollConversationHistoryViewer,gbc);

        /*
            Main layout
         */
        splt = new JSplitPane(JSplitPane.VERTICAL_SPLIT,pnlTop,pnlFrameViewer);
        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(splt,gbc);
        int parentHeight = (int)getHeight()/2;
        pnlTop.setPreferredSize(new Dimension(getWidth(),parentHeight));
        pnlFrameViewer.setPreferredSize(new Dimension(getWidth(),parentHeight));
        splt.setResizeWeight(0.50);
    }
}
