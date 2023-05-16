package com.mitmws.mvc.view.panels.automatedtester;
import com.mitmws.mvc.controller.WebsocketFrameController;
import com.mitmws.mvc.model.WebsocketFrameModel;
import com.mitmws.mvc.view.panels.PnlTestLog;
import com.mitmws.mvc.view.panels.trafficpanel.PnlWebsocketFrameView;
import com.mitmws.util.GuiUtils;
import com.mitmws.mvc.model.AutomatedTesterModel;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.EventObject;

public class PnlAutomatedTesterTestOutput extends JPanel {
    public JTable jtblTestRuns = null;
    public JTable jtblTestTraffic = null;
    public PnlTestLog pnlTestLog;
    public PnlWebsocketFrameView pnlFrameViewer;
    public WebsocketFrameModel websocketFrameModel;
    public WebsocketFrameController websocketFrameController;
    private JScrollPane scrollTestRuns = null;
    private JScrollPane scrollTraffic = null;
    private AutomatedTesterModel automatedTesterModel;
    public PnlAutomatedTesterTestOutput(AutomatedTesterModel automatedTesterModel) {
        this.automatedTesterModel = automatedTesterModel;
        initLayout();
    }

    public void resetUi() {
        GuiUtils.clearTable(jtblTestTraffic);
        GuiUtils.clearTable(jtblTestRuns);
        pnlFrameViewer.resetUi();
        this.automatedTesterModel = automatedTesterModel;}

    public void initLayout() {
        pnlTestLog = new PnlTestLog(automatedTesterModel.getTestLogTableModel());
        websocketFrameModel = new WebsocketFrameModel(null);
        pnlFrameViewer = new PnlWebsocketFrameView(websocketFrameModel);
        websocketFrameController = new WebsocketFrameController(websocketFrameModel,pnlFrameViewer);
        jtblTestRuns = new JTable(automatedTesterModel.getRunsTableModel()) {
            public boolean editCellAt(int row, int column, EventObject e) {
                return false;
            }
        };
        jtblTestTraffic = new JTable(automatedTesterModel.getTestTrafficModel()){
            public boolean editCellAt(int row, int column, EventObject e) {
                return false;
            }

            @Override
            public Class getColumnClass(int column) {
                if (column == 1) {
                    return Color.class;
                }
                return String.class;
            }
        };


        int[] trcolWidths = { -1,160,140,80,50};
        for ( int i = 0; i < trcolWidths.length; i++ ) {
            jtblTestRuns.getColumnModel().getColumn(i).setMinWidth(trcolWidths[i]);
            jtblTestRuns.getColumnModel().getColumn(i).setMaxWidth(trcolWidths[i]);
            jtblTestRuns.getColumnModel().getColumn(i).setPreferredWidth(trcolWidths[i]);
        }
        jtblTestRuns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jtblTestRuns.setCellSelectionEnabled(false);
        jtblTestRuns.setRowSelectionAllowed(true);


        jtblTestTraffic.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jtblTestTraffic.setCellSelectionEnabled(false);
        jtblTestTraffic.setRowSelectionAllowed(true);
        jtblTestTraffic.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
        {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(Color.BLACK);
                if ( !isSelected ) {
                    c.setBackground(Color.WHITE);
                    Color color = (Color)table.getValueAt(row,1);
                    c.setBackground(color);
                    c.setForeground(Color.BLACK);
                }
                else {
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        });

        int[] wsConvoColWidths = { -1,-1,160,140,35,90,35};
        for ( int i = 0; i < wsConvoColWidths.length; i++ ) {
            jtblTestTraffic.getColumnModel().getColumn(i).setMinWidth(wsConvoColWidths[i]);
            jtblTestTraffic.getColumnModel().getColumn(i).setMaxWidth(wsConvoColWidths[i]);
            jtblTestTraffic.getColumnModel().getColumn(i).setPreferredWidth(wsConvoColWidths[i]);
        }

        scrollTestRuns = new JScrollPane(jtblTestRuns);
        scrollTestRuns.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTestRuns.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        scrollTraffic = new JScrollPane(jtblTestTraffic);
        scrollTraffic.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTraffic.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);


        JSplitPane spltBottomRight = new JSplitPane(JSplitPane.VERTICAL_SPLIT,pnlFrameViewer, pnlTestLog);
        JSplitPane spltTraffic = new JSplitPane(JSplitPane.VERTICAL_SPLIT,scrollTraffic, spltBottomRight);

        JSplitPane scrollMain = new JSplitPane();
        scrollMain = new JSplitPane(JSplitPane.VERTICAL_SPLIT,scrollTestRuns, spltTraffic);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(scrollMain,gbc);



        int height = (int)getHeight()/2;
        scrollTraffic.setPreferredSize(new Dimension(getWidth(),height));
        spltBottomRight.setPreferredSize(new Dimension(getWidth(),height));
        spltTraffic.setResizeWeight(0.5);


    }
    public WebsocketFrameController getWebsocketFrameController() {
        return websocketFrameController;
    }
}
