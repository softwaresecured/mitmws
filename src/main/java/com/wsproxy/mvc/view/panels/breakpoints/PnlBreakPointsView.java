package com.wsproxy.mvc.view.panels.breakpoints;

import com.wsproxy.mvc.controller.WebsocketFrameController;
import com.wsproxy.mvc.model.BreakpointModel;
import com.wsproxy.mvc.model.WebsocketFrameModel;
import com.wsproxy.mvc.view.panels.trafficpanel.PnlWebsocketFrameView;

import javax.swing.*;
import java.awt.*;
/*



    Layout:
    - Break point editor
        Breakpoint controls:
        - Conversation scope
        - Payload scope
        - Break inbound / outbound
        - PING/PONG/CLOSE/TEXT
        - Save/Delete
    - Table showing current break points
    - Table showing break queue
    - Editor
    - Button panel for drop/release
 */



public class PnlBreakPointsView extends JPanel {

    public JPanel pnlCurrentBreakpointSettings = new JPanel();
    public JTextField jtxtName = new JTextField("Untitled");
    public JTextField jtxtConversationScopeRegex = new JTextField(".*");
    public JTextField jtxtPayloadScopeRegex = new JTextField(".*");
    public JRadioButton jradioBreakInbound = new JRadioButton("Inbound");
    public JRadioButton jradioBreakOutbound = new JRadioButton("Outbound");
    public JRadioButton jradioBreakBoth = new JRadioButton("Both");
    public JLabel lblWarnings = new JLabel("");

    public JButton btnNew = new JButton("New");
    public JButton btnSave = new JButton("Save");
    public JButton btnDelete = new JButton("Delete");

    public JPanel pnlSettingsHeader = new JPanel();
    public JPanel pnlFrameTypes = new JPanel();
    public JPanel pnlSettingsFooter = new JPanel();


    public JCheckBox jchkContinuation  = new JCheckBox("CONTINUATION");
    public JCheckBox jchkText  = new JCheckBox("TEXT");
    public JCheckBox jchkBin  = new JCheckBox("BINARY");
    public JCheckBox jchkRes1  = new JCheckBox("RES1");
    public JCheckBox jchkRes2  = new JCheckBox("RES2");
    public JCheckBox jchkRes3  = new JCheckBox("RES3");
    public JCheckBox jchkRes4  = new JCheckBox("RES4");
    public JCheckBox jchkRes5  = new JCheckBox("RES5");
    public JCheckBox jchkClose  = new JCheckBox("CLOSE");
    public JCheckBox jchkPing  = new JCheckBox("PING");
    public JCheckBox jchkPong  = new JCheckBox("PONG");

    public JTable jtblBreakpointRules;
    public JTable jtblBreakpointQueue;
    public WebsocketFrameModel websocketFrameModel;
    public PnlWebsocketFrameView pnlWebsocketFrameView;

    public JPanel pnlActions = new JPanel();
    public JButton btnDrop = new JButton("Drop");
    public JButton btnContinue = new JButton("Continue");

    private BreakpointModel breakpointModel;


    public PnlBreakPointsView( BreakpointModel breakpointModel ) {
        this.breakpointModel = breakpointModel;
        initLayout();
    }

    public void initLayout() {

        lblWarnings.setVisible(false);
        JPanel pnlNorth = new JPanel();
        JPanel pnlSouth = new JPanel();
        pnlNorth.setLayout(new GridBagLayout());
        pnlSouth.setLayout(new GridBagLayout());

        btnSave.setEnabled(false);
        btnDelete.setEnabled(false);

        pnlSettingsHeader.setLayout(new GridBagLayout());
        pnlSettingsFooter.setLayout(new GridBagLayout());

        jtblBreakpointRules = new JTable(breakpointModel.getBreakpointTableModel());
        jtblBreakpointQueue = new JTable(breakpointModel.getBreakpointQueueTableModel());

        jtblBreakpointRules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jtblBreakpointQueue.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        int[] t1colWidths = { -1,200,250};
        for ( int i = 0; i < t1colWidths.length; i++ ) {
            jtblBreakpointRules.getColumnModel().getColumn(i).setMinWidth(t1colWidths[i]);
            jtblBreakpointRules.getColumnModel().getColumn(i).setMaxWidth(t1colWidths[i]);
            jtblBreakpointRules.getColumnModel().getColumn(i).setPreferredWidth(t1colWidths[i]);
        }

        int[] t2colWidths = { -1,200,25};
        for ( int i = 0; i < t2colWidths.length; i++ ) {
            jtblBreakpointQueue.getColumnModel().getColumn(i).setMinWidth(t2colWidths[i]);
            jtblBreakpointQueue.getColumnModel().getColumn(i).setMaxWidth(t2colWidths[i]);
            jtblBreakpointQueue.getColumnModel().getColumn(i).setPreferredWidth(t2colWidths[i]);
        }

        websocketFrameModel = new WebsocketFrameModel(null);
        websocketFrameModel.setEditable(true);
        pnlWebsocketFrameView = new PnlWebsocketFrameView(websocketFrameModel);
        WebsocketFrameController websocketFrameController = new WebsocketFrameController(websocketFrameModel,pnlWebsocketFrameView);

        JScrollPane scrollBreakpoints = new JScrollPane(jtblBreakpointRules);
        scrollBreakpoints.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollBreakpoints.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JScrollPane scrollBreakpointQueue = new JScrollPane(jtblBreakpointQueue);
        scrollBreakpointQueue.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollBreakpointQueue.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        ButtonGroup breakDirection = new ButtonGroup();
        breakDirection.add(jradioBreakInbound);
        breakDirection.add(jradioBreakOutbound);
        breakDirection.add(jradioBreakBoth);

        pnlFrameTypes.setBorder(BorderFactory.createTitledBorder(""));
        pnlFrameTypes.setLayout(new FlowLayout());
        pnlFrameTypes.add(jchkContinuation);
        pnlFrameTypes.add(jchkText);
        pnlFrameTypes.add(jchkBin);
        pnlFrameTypes.add(jchkRes1);
        pnlFrameTypes.add(jchkRes2);
        pnlFrameTypes.add(jchkRes3);
        pnlFrameTypes.add(jchkRes4);
        pnlFrameTypes.add(jchkRes5);
        pnlFrameTypes.add(jchkClose);
        pnlFrameTypes.add(jchkPing);
        pnlFrameTypes.add(jchkPong);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlSettingsHeader.add(new JLabel("Name:"),gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        pnlSettingsHeader.add(jtxtName,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlSettingsHeader.add(new JLabel("Conversation scope:"),gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        pnlSettingsHeader.add(jtxtConversationScopeRegex,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlSettingsHeader.add(new JLabel("Payload scope:"),gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        pnlSettingsHeader.add(jtxtPayloadScopeRegex,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        pnlSettingsFooter.add(new JPanel(),gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlSettingsFooter.add(jradioBreakInbound,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlSettingsFooter.add(jradioBreakOutbound,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlSettingsFooter.add(jradioBreakBoth,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlSettingsFooter.add(btnNew,gbc);


        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlSettingsFooter.add(btnSave,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlSettingsFooter.add(btnDelete,gbc);


        pnlCurrentBreakpointSettings.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        pnlCurrentBreakpointSettings.add(pnlSettingsHeader,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 0;
        pnlCurrentBreakpointSettings.add(pnlFrameTypes,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.weighty = 0;
        pnlCurrentBreakpointSettings.add(pnlSettingsFooter,gbc);


        pnlActions.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        pnlActions.add(new JPanel(),gbc);


        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlActions.add(btnDrop,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlActions.add(btnContinue,gbc);



        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        pnlNorth.add(pnlCurrentBreakpointSettings,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 0;
        pnlNorth.add(lblWarnings,gbc);



        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        pnlNorth.add(scrollBreakpoints,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1;
        gbc.weighty = 1;
        pnlNorth.add(scrollBreakpointQueue,gbc);


        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1;
        gbc.weighty = 1;
        pnlSouth.add(pnlWebsocketFrameView,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1;
        gbc.weighty = 0;
        pnlSouth.add(pnlActions,gbc);

        JSplitPane spltBreakpoints = new JSplitPane(JSplitPane.VERTICAL_SPLIT,pnlNorth,pnlSouth);
        setLayout( new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(spltBreakpoints,gbc);

        int parentHeight = (int)this.getSize().getHeight();
        pnlNorth.setPreferredSize(new Dimension(getWidth(),(int)parentHeight/2));
        pnlSouth.setPreferredSize(new Dimension(getWidth(),(int)parentHeight/2));
        spltBreakpoints.setResizeWeight(0.5);

        jtxtName.setEnabled(false);
        jtxtConversationScopeRegex.setEnabled(false);
        jtxtPayloadScopeRegex.setEnabled(false);
        jradioBreakInbound.setEnabled(false);
        jradioBreakOutbound.setEnabled(false);
        jradioBreakBoth.setEnabled(false);

        jchkContinuation.setEnabled(false);
        jchkText.setEnabled(false);
        jchkBin.setEnabled(false);
        jchkRes1.setEnabled(false);
        jchkRes2.setEnabled(false);
        jchkRes3.setEnabled(false);
        jchkRes4.setEnabled(false);
        jchkRes5.setEnabled(false);
        jchkClose.setEnabled(false);
        jchkPing.setEnabled(false);
        jchkPong.setEnabled(false);
        btnSave.setEnabled(false);
        btnDelete.setEnabled(false);


    }
}
