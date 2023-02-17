package com.wsproxy.mvc.view.panels.anomalies;
import com.wsproxy.mvc.controller.WebsocketFrameController;
import com.wsproxy.mvc.model.WebsocketFrameModel;
import com.wsproxy.mvc.view.panels.trafficpanel.PnlWebsocketFrameView;
import com.wsproxy.util.GuiUtils;
import com.wsproxy.mvc.model.AnomaliesModel;
import javax.swing.*;
import java.awt.*;
import java.util.EventObject;

public class PnlDetectedAnomalies extends JPanel {
    public JTable jtblDetectedAnomalies;
    public JTable jtblWebsocketConversation;

    public JLabel lblTitle = new JLabel("");
    public JTextArea jtxtAnomalyDescription = new JTextArea();
    public JTextArea jtxtRawFrame = new JTextArea();
    public PnlWebsocketFrameView pnlFrameViewer;
    public WebsocketFrameModel websocketFrameModel;
    public WebsocketFrameController websocketFrameController;

    public JPanel pnlRawFrameViewer = new JPanel();

    private PnlDetectedAnomaliesToolBar pnlDetectedAnomaliesToolBar = null;
    public JSplitPane splitPane = new JSplitPane();
    private AnomaliesModel anomaliesModel;
    public PnlDetectedAnomalies(AnomaliesModel anomaliesModel) {
        this.anomaliesModel = anomaliesModel;
        initLayout();
        //updateAnomalyTitle(null,null);
    }
    public void updateAnomalyTitle( String credibility, String cwe, String title ) {
        lblTitle.setText("");
        if ( cwe != null && title != null ) {
            lblTitle.setText(String.format("%s/%s - %s", credibility, cwe, title));
        }
    }
    public void initLayout() {
        pnlRawFrameViewer.setVisible(false);
        websocketFrameModel = new WebsocketFrameModel(null);
        pnlFrameViewer = new PnlWebsocketFrameView(websocketFrameModel);
        websocketFrameController = new WebsocketFrameController(websocketFrameModel,pnlFrameViewer);
        // Anomaly side bar
        lblTitle.setFont(new Font(this.getFont().getName(), Font.BOLD, 24));
        jtblDetectedAnomalies = new JTable(anomaliesModel.getAnomaliesTableModel()) {
            public boolean editCellAt(int row, int column, EventObject e) {
                return false;
            }
        };
        jtblDetectedAnomalies.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jtblDetectedAnomalies.setCellSelectionEnabled(false);
        jtblDetectedAnomalies.setRowSelectionAllowed(true);

        int[] anomalyColWidths = { -1,100,100,100, 120, 80 };
        for ( int i = 0; i < anomalyColWidths.length; i++ ) {
            jtblDetectedAnomalies.getColumnModel().getColumn(i).setMinWidth(anomalyColWidths[i]);
            jtblDetectedAnomalies.getColumnModel().getColumn(i).setMaxWidth(anomalyColWidths[i]);
            jtblDetectedAnomalies.getColumnModel().getColumn(i).setPreferredWidth(anomalyColWidths[i]);
        }
        jtblDetectedAnomalies.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        JScrollPane scrollAnomalies = new JScrollPane(jtblDetectedAnomalies);
        scrollAnomalies.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollAnomalies.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JScrollPane scroll = new JScrollPane(jtxtRawFrame);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);


        pnlRawFrameViewer.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        pnlRawFrameViewer.add(new JLabel("Raw frame"), gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        pnlRawFrameViewer.add(scroll, gbc);

        pnlDetectedAnomaliesToolBar = new PnlDetectedAnomaliesToolBar();

        JPanel pnlSideBar = new JPanel();
        pnlSideBar.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        pnlSideBar.add(pnlDetectedAnomaliesToolBar, gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        pnlSideBar.add(scrollAnomalies, gbc);

        // Anomaly detail viewer
        jtblWebsocketConversation = new JTable(anomaliesModel.getConversationTableModel()) {
            public boolean editCellAt(int row, int column, EventObject e) {
                return false;
            }
        };
        jtblWebsocketConversation.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jtblWebsocketConversation.setCellSelectionEnabled(false);
        jtblWebsocketConversation.setRowSelectionAllowed(true);
        int[] wsConvoColWidths = { -1,160,35,90,35};
        for ( int i = 0; i < wsConvoColWidths.length; i++ ) {
            jtblWebsocketConversation.getColumnModel().getColumn(i).setMinWidth(wsConvoColWidths[i]);
            jtblWebsocketConversation.getColumnModel().getColumn(i).setMaxWidth(wsConvoColWidths[i]);
            jtblWebsocketConversation.getColumnModel().getColumn(i).setPreferredWidth(wsConvoColWidths[i]);
        }
        jtblDetectedAnomalies.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        JScrollPane scrollConversation = new JScrollPane(jtblWebsocketConversation);
        scrollConversation.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollConversation.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JScrollPane scrollAnomalyDescription = new JScrollPane(jtxtAnomalyDescription);
        scrollAnomalyDescription.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollAnomalyDescription.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel pnlAnomalyDetails = new JPanel();
        pnlAnomalyDetails.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        pnlAnomalyDetails.add(lblTitle, gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        pnlAnomalyDetails.add(GuiUtils.frameWrapComponent(scrollAnomalyDescription, "Description"), gbc);





        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 2;
        pnlAnomalyDetails.add(pnlRawFrameViewer, gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 3;
        pnlAnomalyDetails.add(scrollConversation, gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 4;
        pnlAnomalyDetails.add(pnlFrameViewer, gbc);

        splitPane = new JSplitPane();
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,pnlSideBar, pnlAnomalyDetails);

        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(splitPane,gbc);
        pnlFrameViewer.jtxtFramePayload.setRows(5);
        jtxtAnomalyDescription.setRows(5);
        int parentWidth = (int)this.getSize().getWidth();
        pnlSideBar.setPreferredSize(new Dimension(parentWidth/2,(int)this.getPreferredSize().getHeight()));
        pnlAnomalyDetails.setPreferredSize(new Dimension(parentWidth/2,(int)this.getPreferredSize().getHeight()));
        splitPane.setResizeWeight(0.5);
    }

    public WebsocketFrameController getWebsocketFrameController() {
        return websocketFrameController;
    }
}
