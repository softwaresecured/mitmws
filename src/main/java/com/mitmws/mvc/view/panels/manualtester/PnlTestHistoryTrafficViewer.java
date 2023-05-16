package com.mitmws.mvc.view.panels.manualtester;

import com.mitmws.mvc.controller.WebsocketFrameController;
import com.mitmws.mvc.model.ManualTesterModel;
import com.mitmws.mvc.model.WebsocketFrameModel;
import com.mitmws.mvc.view.panels.PnlWebsocketTrafficToolbar;
import com.mitmws.mvc.view.panels.trafficpanel.PnlWebsocketFrameView;

import javax.swing.*;
import java.awt.*;
import java.util.EventObject;

public class PnlTestHistoryTrafficViewer extends JPanel {
    public PnlWebsocketTrafficToolbar pnlWebsocketTrafficToolbar;
    public JTable tblWebsocketConversation;
    public PnlWebsocketFrameView pnlFrameViewer;
    public WebsocketFrameModel websocketFrameModel;
    public WebsocketFrameController websocketFrameController;
    private ManualTesterModel manualTesterModel;
    private JSplitPane spltHistoryDetail;
    public PnlTestHistoryTrafficViewer(ManualTesterModel manualTesterModel) {
        this.manualTesterModel = manualTesterModel;
        initLayout();
    }

    public void initLayout() {
        websocketFrameModel = new WebsocketFrameModel(null);
        pnlFrameViewer = new PnlWebsocketFrameView(websocketFrameModel);
        websocketFrameController = new WebsocketFrameController(websocketFrameModel,pnlFrameViewer);
        pnlWebsocketTrafficToolbar = new PnlWebsocketTrafficToolbar();
        tblWebsocketConversation = new JTable(manualTesterModel.getWebsocketConversationHistoryTableModel()) {
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


        JPanel pnlTopContainer = new JPanel();
        pnlTopContainer.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        pnlTopContainer.add(pnlWebsocketTrafficToolbar,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        pnlTopContainer.add(scrollConversationHistoryViewer,gbc);

        spltHistoryDetail = new JSplitPane(JSplitPane.VERTICAL_SPLIT,pnlTopContainer,pnlFrameViewer);

        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(spltHistoryDetail,gbc);

        int parentHeight = (int)getHeight()/2;
        pnlTopContainer.setPreferredSize(new Dimension(getWidth(),parentHeight));
        pnlFrameViewer.setPreferredSize(new Dimension(getWidth(),parentHeight));
        spltHistoryDetail.setResizeWeight(0.80);
    }

    public WebsocketFrameController getWebsocketFrameController() {
        return websocketFrameController;
    }
}
