package com.wsproxy.mvc.view.panels.search;
import com.wsproxy.util.GuiUtils;
import com.wsproxy.mvc.model.TrafficSearchModel;
import javax.swing.*;
import java.awt.*;
import java.util.EventObject;

public class PnlTrafficSearchView extends JPanel {
    public JTextField jtxtSearch = new JTextField();
    public JCheckBox jchkRegex = new JCheckBox("Regex");
    public JCheckBox jchkProxySource = new JCheckBox("Proxy");
    public JCheckBox jchkManualTesterSource = new JCheckBox("Manual tester");
    public JCheckBox jchkAutomatedTesterSource = new JCheckBox("Automated tester");
    public JCheckBox jchkImmediateSource = new JCheckBox("Immediate");
    public JSpinner jspnMaxResults;
    public JButton btnSearch = new JButton("Search");

    public JTextArea jtxtTextResult = new JTextArea();
    public JTable tblSearchResults = null;
    private TrafficSearchModel trafficSearchModel;
    public PnlTrafficSearchView ( TrafficSearchModel trafficSearchModel) {
        this.trafficSearchModel = trafficSearchModel;
        jspnMaxResults = new JSpinner(new SpinnerNumberModel(25,1,this.trafficSearchModel.getMaxResults(),25));
        initLayout();
    }

    public void initLayout() {


        JPanel pnlSearchToolbar = new JPanel();
        jchkProxySource.setSelected(true);
        jchkManualTesterSource.setSelected(true);
        jchkAutomatedTesterSource.setSelected(true);
        jchkImmediateSource.setSelected(true);

        //jspnMaxResults.setPreferredSize(new Dimension(50,(int)jtxtSearch.getPreferredSize().getHeight()));
        //btnSearch.setPreferredSize(new Dimension((int) btnSearch.getPreferredSize().getWidth(), (int) new JTextField().getPreferredSize().getHeight()));
        //btnClear.setPreferredSize(new Dimension((int) btnClear.getPreferredSize().getWidth(), (int) new JTextField().getPreferredSize().getHeight()));
        pnlSearchToolbar.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        pnlSearchToolbar.add(new JLabel("Search"),gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 0;
        pnlSearchToolbar.add(jtxtSearch,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        pnlSearchToolbar.add(jchkRegex,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        pnlSearchToolbar.add(jchkProxySource,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        pnlSearchToolbar.add(jchkManualTesterSource,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        pnlSearchToolbar.add(jchkAutomatedTesterSource,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 0;
        pnlSearchToolbar.add(jchkImmediateSource,gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 7;
        gbc.gridy = 0;
        pnlSearchToolbar.add(new JLabel(),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 0;
        pnlSearchToolbar.add(btnSearch,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 0;
        pnlSearchToolbar.add(new JLabel("Max results"),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 0;
        pnlSearchToolbar.add(jspnMaxResults,gbc);

        tblSearchResults = new JTable(trafficSearchModel.getResultsModel()) {
            public boolean editCellAt(int row, int column, EventObject e) {
                return false;
            }
        };
        tblSearchResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblSearchResults.setCellSelectionEnabled(false);
        tblSearchResults.setRowSelectionAllowed(true);

        int[] httpTrafficColWidths = { -1,-1,140};
        for ( int i = 0; i < httpTrafficColWidths.length; i++ ) {
            tblSearchResults.getColumnModel().getColumn(i).setMinWidth(httpTrafficColWidths[i]);
            tblSearchResults.getColumnModel().getColumn(i).setMaxWidth(httpTrafficColWidths[i]);
            tblSearchResults.getColumnModel().getColumn(i).setPreferredWidth(httpTrafficColWidths[i]);
        }


        JScrollPane scrollTblResult = new JScrollPane(tblSearchResults);
        scrollTblResult.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTblResult.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JScrollPane scrollTextResult = new JScrollPane(jtxtTextResult);
        scrollTextResult.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollTextResult.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(pnlSearchToolbar,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(scrollTblResult,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(GuiUtils.frameWrapComponent(scrollTextResult,"Text"),gbc);
    }
}