package com.wsproxy.mvc.view.frames;
import com.wsproxy.configuration.ApplicationConfig;
import com.wsproxy.mvc.model.MainModel;
import com.wsproxy.mvc.view.panels.breakpoints.PnlBreakPointsView;
import com.wsproxy.mvc.view.panels.immediate.PnlImmediateView;
import com.wsproxy.mvc.view.panels.interactsh.PnlInteractsh;
import com.wsproxy.mvc.view.panels.payloads.PnlPayloadsView;
import com.wsproxy.mvc.view.panels.protocoltester.PnlProtocolTesterView;
import com.wsproxy.mvc.view.panels.rules.PnlRulesView;
import com.wsproxy.mvc.view.panels.scriptconsole.PnlScriptConsole;
import com.wsproxy.mvc.view.panels.search.PnlTrafficSearchView;
import com.wsproxy.mvc.view.panels.anomalies.PnlAnomaliesView;
import com.wsproxy.mvc.view.panels.automatedtester.PnlAutomatedTesterView;
import com.wsproxy.mvc.view.panels.environment.PnlEnvironmentView;
import com.wsproxy.mvc.view.panels.logs.PnlLogs;
import com.wsproxy.mvc.view.panels.mainform.PnlMainStatusBarView;
import com.wsproxy.mvc.view.panels.manualtester.PnlManualTesterView;
import com.wsproxy.mvc.view.panels.settings.PnlSettingsView;
import com.wsproxy.mvc.view.panels.trafficpanel.PnlTrafficView;
import com.wsproxy.mvc.view.panels.updates.PnlUpdatesView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class FrmMainView extends JFrame {

    /*
    Main menu
    */
    public JMenuBar mnuBarMainMenu = new JMenuBar();
    public JMenu mnuFile = null;
    public JMenu mnuEdit = null;
    public JMenu mnuProxy = null;
    public JMenu mnuTestplan = null;
    public JMenu mnuTools = null;
    public JMenu mnuHelp = null;
    /*
    Main menu items
    */
    public JMenuItem mnuItemClose = null;
    public JMenuItem mnuItemNewProject = null;
    public JMenuItem mnuItemOpen = null;
    public JMenuItem mnuItemImportHttpFromFile = null;
    public JMenuItem mnuItemImportFramesFromFile = null;
    public JMenuItem mnuItemSaveAs = null;
    public JMenuItem mnuItemSave = null;
    public JMenuItem mnuProxyRestart = new JMenuItem("Restart");
    public JMenuItem mnuProxyStart = new JMenuItem("Start");
    public JMenuItem mnuProxyStop = new JMenuItem("Stop");
    public JMenuItem mnuExportCa = new JMenuItem("Export CA Certificate");
    public JMenuItem mnuUpdate = new JMenuItem("Update rules/payloads");

    public JMenuItem mnuEncodeDecode = new JMenuItem("Encoder/Decoder");


    // Popup menu for traffic tables
    public JPopupMenu httpTrafficTableMenu;
    public JPopupMenu wsTrafficTableMenu;
    public JPopupMenu wsConnectionTableMenu;
    public JMenuItem mnuHttpRequestAddToManualTest;
    public JMenuItem mnuWsFrameAddToManualTest;
    public JMenuItem mnuHttpRequestAddToImmediate;
    public JMenuItem mnuWsFrameAddToImmediate;

    public JMenuItem mnuHighlightWsTrafficRows;
    public JMenuItem mnuHighlightHttpTrafficRows;

    public JMenuItem mnuExportSelectedHTTPToFile;
    public JMenuItem mnuExportSelectedWsToFile;

    public JMenuItem mnuExportConversationToFile;
    public JMenuItem mnuConnectionsAddToManualTest;
    // Main form components
    public JTabbedPane jtabMain;

    // Sub views
    public PnlMainStatusBarView pnlMainStatusBarView;
    public PnlTrafficView pnlTrafficView;
    public PnlTrafficSearchView pnlTrafficSearchView;
    public PnlEnvironmentView pnlEnvironmentView;
    public PnlManualTesterView pnlManualTesterView;
    public PnlAutomatedTesterView pnlAutomatedTesterView;
    public PnlAnomaliesView pnlAnomaliesView;
    public PnlSettingsView pnlSettingsView;
    public PnlImmediateView pnlImmediateView;
    public PnlUpdatesView pnlUpdatesView;
    public PnlPayloadsView pnlPayloadsView;
    public PnlRulesView pnlRulesView;
    public PnlScriptConsole pnlScriptConsole;
    public PnlInteractsh pnlInteractsh;
    public PnlBreakPointsView pnlBreakPointsView;
    public FrmEncoderDecoderToolView frmEncoderDecoderToolView;


    public PnlLogs pnlLogs;
    public PnlProtocolTesterView pnlProtocolTesterView;
    private MainModel mainModel;
    private ApplicationConfig applicationConfig = new ApplicationConfig();
    public FrmMainView(MainModel mainModel) {
        this.mainModel = mainModel;
        initLayout();
        initSubLayout();
        initMainMenu();
        initPopupMenus();
    }
    public void initSubLayout() {
        pnlTrafficView = new PnlTrafficView(mainModel);
        pnlTrafficSearchView = new PnlTrafficSearchView(mainModel.getTrafficSearchModel());
        pnlEnvironmentView = new PnlEnvironmentView(mainModel.getEnvironmentModel());
        pnlManualTesterView = new PnlManualTesterView(mainModel.getManualTesterModel());
        pnlAutomatedTesterView = new PnlAutomatedTesterView(mainModel.getAutomatedTesterModel());
        pnlAnomaliesView = new PnlAnomaliesView(mainModel.getAnomaliesModel(),mainModel.getProjectModel());
        pnlSettingsView = new PnlSettingsView(mainModel.getSettingsModel());
        pnlImmediateView = new PnlImmediateView(mainModel.getImmediateModel());
        pnlUpdatesView = new PnlUpdatesView(mainModel.getUpdatesModel());
        pnlPayloadsView = new PnlPayloadsView(mainModel.getPayloadsModel());
        pnlRulesView = new PnlRulesView(mainModel.getRulesModel());
        pnlLogs = new PnlLogs();
        pnlInteractsh = new PnlInteractsh(mainModel.getInteractshModel());
        pnlScriptConsole = new PnlScriptConsole();
        pnlProtocolTesterView = new PnlProtocolTesterView(mainModel.getProtocolTesterModel());
        frmEncoderDecoderToolView = new FrmEncoderDecoderToolView(mainModel.getEncoderDecoderToolModel());

        jtabMain.addTab("Traffic", null, pnlTrafficView,"HTTP/Websocket traffic");
        jtabMain.addTab("Immediate", null, pnlImmediateView,"Immediate");
        jtabMain.addTab("Manual tester", null, pnlManualTesterView,"Create and edit websocket conversations");
        jtabMain.addTab("Automated tester", null, pnlAutomatedTesterView,"Replay conversations with targets and a list of payloads");

        if ( applicationConfig.getProperty("betafeatures.enable-protocoltester").equals("true")) {
            jtabMain.addTab("Protocol tester", null, pnlProtocolTesterView, "Test the presentation layer.");
        }

        jtabMain.addTab("Search", null, pnlTrafficSearchView,"Search HTTP/Websocket traffic");
        jtabMain.addTab("Anomalies", null, pnlAnomaliesView,"Detection events from passive and active scanner rules");
        jtabMain.addTab("Payloads", null, pnlPayloadsView,"Payloads used by the scanners / fuzzers");
        jtabMain.addTab("Rules", null, pnlRulesView,"Rules used by the scanners / fuzzers");
        jtabMain.addTab("Environment", null, pnlEnvironmentView,"Environment variables");
        jtabMain.addTab("Settings", null, pnlSettingsView,"Settings");
        jtabMain.addTab("Library", null, pnlUpdatesView,"Library for payloads, scripts, and rules");
        jtabMain.addTab("Logs", null, pnlLogs,"Application log");
        jtabMain.addTab("Script console", null, pnlScriptConsole,"Run jython scripts");
        jtabMain.addTab("OOB", null, pnlInteractsh,"Out of band detections");
    }

    private void initLayout() {
        setLayout(new GridBagLayout());
        jtabMain = new JTabbedPane();
        pnlMainStatusBarView = new PnlMainStatusBarView();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        add(jtabMain,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weighty = 0;
        gbc.weightx = 1;
        add(pnlMainStatusBarView,gbc);
    }

    private void initMainMenu() {
        mnuFile = new JMenu("File");
        mnuFile.setMnemonic(KeyEvent.VK_F);

        mnuEdit = new JMenu("Edit");
        mnuEdit.setMnemonic(KeyEvent.VK_E);

        mnuProxy = new JMenu("Proxy");
        mnuProxy.setMnemonic(KeyEvent.VK_P);

        mnuTestplan = new JMenu("Tests");
        mnuTestplan.setMnemonic(KeyEvent.VK_T);

        mnuTools = new JMenu("Tools");

        mnuHelp = new JMenu("Help");
        mnuHelp.setMnemonic(KeyEvent.VK_H);

        mnuProxy.add(mnuProxyStart);
        mnuProxy.add(mnuProxyStop);
        mnuProxy.add(mnuProxyRestart);
        mnuProxy.add(mnuExportCa);
        mnuHelp.add(mnuUpdate);

        mnuTools.add(mnuEncodeDecode);

        mnuItemNewProject = new JMenuItem("New", KeyEvent.VK_C);
        mnuFile.add(mnuItemNewProject);
        mnuItemOpen = new JMenuItem("Open", KeyEvent.VK_O);
        mnuFile.add(mnuItemOpen);

        JMenu mnuImport = new JMenu("Import");
        mnuItemImportHttpFromFile = new JMenuItem("HTTP request from file");
        mnuItemImportFramesFromFile = new JMenuItem("Frames from file");
        mnuImport.add(mnuItemImportHttpFromFile);
        mnuImport.add(mnuItemImportFramesFromFile);
        mnuFile.add(mnuImport);

        mnuItemSave = new JMenuItem("Save", KeyEvent.VK_S);
        mnuFile.add(mnuItemSave);

        mnuItemSaveAs = new JMenuItem("Save As");
        mnuFile.add(mnuItemSaveAs);
        mnuItemClose = new JMenuItem("Close", KeyEvent.VK_C);
        mnuFile.add(mnuItemClose);
        mnuProxy.addSeparator();

        mnuBarMainMenu.add(mnuFile);
        mnuBarMainMenu.add(mnuEdit);
        mnuBarMainMenu.add(mnuProxy);
        mnuBarMainMenu.add(mnuTestplan);
        mnuBarMainMenu.add(mnuTools);
        mnuBarMainMenu.add(Box.createHorizontalGlue());
        mnuBarMainMenu.add(mnuHelp);
        this.setJMenuBar(mnuBarMainMenu);
    }

    public void initPopupMenus() {
        httpTrafficTableMenu = new JPopupMenu();
        wsTrafficTableMenu = new JPopupMenu();
        wsConnectionTableMenu = new JPopupMenu();
        JMenu mnuHttpRequestAddTo = new JMenu("Add to");
        JMenu mnuWsFrameAddTo = new JMenu("Add to");
        JMenu mnuConnAddTo = new JMenu("Add to");
        mnuConnectionsAddToManualTest = new JMenuItem("Add to manual test");
        mnuExportConversationToFile = new JMenuItem("Export conversation to file");
        mnuConnAddTo.add(mnuConnectionsAddToManualTest);
        wsConnectionTableMenu.add(mnuConnAddTo);
        wsConnectionTableMenu.add(mnuExportConversationToFile);

        mnuHttpRequestAddToManualTest = new JMenuItem("Manual test");
        mnuWsFrameAddToManualTest = new JMenuItem("Manual test");
        mnuHttpRequestAddToImmediate = new JMenuItem("Immediate");
        mnuWsFrameAddToImmediate = new JMenuItem("Immediate");

        mnuHighlightWsTrafficRows = new JMenuItem("Highlight selected");
        mnuHighlightHttpTrafficRows = new JMenuItem("Highlight selected");
        mnuExportSelectedHTTPToFile = new JMenuItem("Export selected request to CSV");
        mnuExportSelectedWsToFile = new JMenuItem("Export selected frames to CSV");
        httpTrafficTableMenu.add(mnuHttpRequestAddTo);
        mnuHttpRequestAddTo.add(mnuHttpRequestAddToManualTest);
        mnuHttpRequestAddTo.add(mnuHttpRequestAddToImmediate);
        httpTrafficTableMenu.add(mnuExportSelectedHTTPToFile);

        wsTrafficTableMenu.add(mnuWsFrameAddTo);
        httpTrafficTableMenu.add(mnuHighlightHttpTrafficRows);
        wsTrafficTableMenu.add(mnuHighlightWsTrafficRows);
        mnuWsFrameAddTo.add(mnuWsFrameAddToManualTest);
        mnuWsFrameAddTo.add(mnuWsFrameAddToImmediate);
        wsTrafficTableMenu.add(mnuExportSelectedWsToFile);

        pnlTrafficView.pnlWebsocketTraffic.pnlConnectionsViewer.tblWebsocketConnections.setComponentPopupMenu(wsConnectionTableMenu);
        pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.tblWebsocketTraffic.setComponentPopupMenu(wsTrafficTableMenu);
        pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.tblHttpTraffic.setComponentPopupMenu(httpTrafficTableMenu);
    }
}
