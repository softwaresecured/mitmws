package com.mitmws.mvc.controller;

import com.mitmws.configuration.ApplicationConfigException;
import com.mitmws.httpproxy.websocket.WebsocketFrame;
import com.mitmws.httpproxy.trafficlogger.HttpTrafficRecord;
import com.mitmws.httpproxy.trafficlogger.WebsocketDirection;
import com.mitmws.httpproxy.trafficlogger.WebsocketTrafficRecord;
import com.mitmws.httpproxy.websocket.WebsocketFrameType;
import com.mitmws.projects.ProjectDataServiceException;
import com.mitmws.tester.TestSequenceItem;
import com.mitmws.tester.TestSequenceItemType;
import com.mitmws.util.FileUtils;
import com.mitmws.util.GuiUtils;
import com.mitmws.mvc.model.MainModel;
import com.mitmws.mvc.thread.*;
import com.mitmws.mvc.view.frames.FrmMainView;
import com.mitmws.pki.BouncyCastleSSLProvider;
import com.mitmws.pki.CertificateKeyBundle;
import com.mitmws.pki.PKIProviderException;
import com.mitmws.pki.PKIUtils;
import com.mitmws.util.TestUtil;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

public class MainController implements PropertyChangeListener {

    private MainModel mainModel;
    private FrmMainView frmMainView;
    // Sub controllers
    private TrafficController trafficController;
    private TrafficSearchController trafficSearchController;
    private MainStatusBarController mainStatusBarController;
    private EnvironmentController environmentController;
    private ManualTesterController manualTesterController;
    private AutomatedTesterController automatedTesterController;
    private AnomaliesController anomaliesController;
    private LogsController logsController;
    private UpdatesController updatesController;
    private SettingsController settingsController;
    private ImmediateController immediateController;
    private PayloadsController payloadsController;
    private RulesController rulesController;
    private ProtocolTesterController protocolTesterController;
    private ScriptConsoleController scriptConsoleController;
    private InteractshController interactshController;
    private BreakpointController breakpointController;
    private EncoderDecoderToolController encoderDecoderToolController;
    private HttpRequestController httpRequestController;
    private ProjectDataExplorerController projectDataExplorerController;

    // Threads
    private LogTailerThread logTailerThread;
    private TrafficLogQueueProcessorThread logQueueProcessorThread;
    private PassiveAnomalyDetectorThread passiveAnomalyDetectorThread;
    private ActiveAnomalyDetectorThread activeAnomalyDetectorThread;
    private InteractshMonitorThread interactshMonitorThread;

    public MainController(MainModel model, FrmMainView view) throws IOException {
        // Views
        this.mainModel = model;
        this.frmMainView = view;


        attachListeners();
        // Controllers
        trafficController = new TrafficController(mainModel.getTrafficModel(),mainModel.getProjectModel(),frmMainView.pnlTrafficView);
        anomaliesController = new AnomaliesController(mainModel.getAnomaliesModel(),mainModel.getProjectModel(),frmMainView.pnlAnomaliesView);
        immediateController = new ImmediateController(mainModel,model.getImmediateModel(),mainModel.getProjectModel(),mainModel.getProxy().getLogger(),frmMainView.pnlImmediateView);
        // Create the project
        try {
            mainModel.getProjectModel().createDefaultProject();
            updateMainTitle();
        } catch (ProjectDataServiceException e) {
            e.printStackTrace(); // TODO big error
        }


        updatesController = new UpdatesController(mainModel.getUpdatesModel(),frmMainView.frmUpdatesView);
        mainStatusBarController = new MainStatusBarController(mainModel.getMainStatusBarModel(),frmMainView.pnlMainStatusBarView);
        trafficSearchController = new TrafficSearchController(mainModel,frmMainView.pnlTrafficSearchView);
        environmentController = new EnvironmentController(mainModel,frmMainView.frmEnvironmentView);
        manualTesterController = new ManualTesterController(mainModel,mainModel.getProxy().getLogger(),frmMainView.pnlManualTesterView);
        automatedTesterController = new AutomatedTesterController(model,mainModel.getProxy().getLogger(),frmMainView.pnlAutomatedTesterView);
        logsController = new LogsController(mainModel.getAppLogModel(),frmMainView.frmLogsView);
        settingsController = new SettingsController(mainModel,frmMainView.frmSettingsView);
        payloadsController = new PayloadsController(mainModel.getPayloadsModel(),frmMainView.frmPayloadsView);
        interactshController = new InteractshController(mainModel.getInteractshModel(),mainModel,frmMainView.pnlInteractsh);
        rulesController = new RulesController(mainModel,frmMainView.frmRulesView);
        protocolTesterController = new ProtocolTesterController(mainModel,frmMainView.pnlProtocolTesterView);
        scriptConsoleController = new ScriptConsoleController(mainModel.getScriptConsoleModel(),frmMainView.frmScriptConsole);
        encoderDecoderToolController = new EncoderDecoderToolController(mainModel.getEncoderDecoderToolModel(),frmMainView.frmEncoderDecoderToolView);
        httpRequestController = new HttpRequestController(mainModel.getHttpRequestTesterModel(),frmMainView.frmHttpRequestTester);
        projectDataExplorerController = new ProjectDataExplorerController(mainModel,frmMainView.frmProjectDataExplorer);

        initEventListeners();



        // Start threads
        startLogTailerThread();
        startAnomalyDetectorThread();
        startMaintenanceThread();
        startInteractshMonitorThread();
        // Init some settings
        if ( mainModel.getSettingsModel().getApplicationConfig().getProperty("http.exclude_url_regex") != null ) {
            view.pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.pnlHttpTrafficToolbar.jchkExclude.setSelected(true);
            mainModel.getTrafficModel().setExcludeRegex(mainModel.getSettingsModel().getApplicationConfig().getProperty("http.exclude_url_regex"));
            view.pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.pnlHttpTrafficToolbar.jbtnApply.doClick();
        }
        frmMainView.pack();
        frmMainView.mnuItemImportFramesFromFile.setEnabled(false);
        frmMainView.mnuItemImportHttpFromFile.setEnabled(false);
        frmMainView.mnuConversations.setEnabled(false);
    }

    public void attachListeners() {
        mainModel.getProjectModel().addListener(trafficController);
        mainModel.getProjectModel().addListener(this);
        mainModel.getAnomaliesModel().addListener(this);
        mainModel.getTrafficSearchModel().addListener(this);
        mainModel.getUpdatesModel().addListener(this);
        mainModel.getSettingsModel().addListener(this);
        mainModel.getInteractshModel().addListener(this);
        mainModel.getProxy().addCleanupThreadEventListener(this);
    }
    public String exportSelectedFramesByUpgradeUUID( String messageIds ) {
        String exportRec = null;
        StringBuilder sb = new StringBuilder();
        try {
            for ( WebsocketTrafficRecord rec : mainModel.getProjectModel().getProjectDataService().getWebsocketTrafficRecordsByUpgradeMessageUUID(messageIds)) {
                if ( rec != null ) {
                    sb.append(String.format("websocket:%s\n", rec.getFrame().toCsv()));
                }
            }
            exportRec = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ProjectDataServiceException e) {
            e.printStackTrace();
        }
        return exportRec;
    }

    public String exportSelectedFramesByMessageId(String messageIds[] ) {
        String exportRec = null;
        if ( messageIds != null && messageIds.length > 0 ) {
            StringBuilder sb = new StringBuilder();
            for ( int i = 0 ; i < messageIds.length; i++ ) {
                try {
                    WebsocketTrafficRecord rec = null;
                    rec = mainModel.getProjectModel().getProjectDataService().getWebsocketTrafficRecordByUUID(messageIds[i]);
                    if ( rec != null ) {
                        sb.append(String.format("websocket:%s\n", rec.getFrame().toCsv()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ProjectDataServiceException e) {
                    e.printStackTrace();
                }
            }
            exportRec = sb.toString();
        }
        return exportRec;
    }

    public String exportSelectedHttpRequest( String messageId ) {
        String exportRec = null;
        try {
            HttpTrafficRecord rec = mainModel.getProjectModel().getProjectDataService().getHttpTrafficRecordByUUID(messageId);
            if ( rec != null ) {
                exportRec = String.format(
                        "http:%s",
                        Base64.getEncoder().encodeToString(rec.getRequest().getBytes())
                );
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ProjectDataServiceException e) {
            e.printStackTrace();
        }
        return exportRec;
    }


    public void initEventListeners() {

        /*
            Add sample frames
         */

        frmMainView.mnuConversationHelloWorld.addActionListener( actionEvent -> {

            ArrayList<TestSequenceItem> steps = new ArrayList<TestSequenceItem>();

            // Text frame
            WebsocketFrame frame = new WebsocketFrame();
            frame.setFin(1);
            frame.setDirection(WebsocketDirection.OUTBOUND);
            frame.setOpcode(WebsocketFrameType.TEXT);
            frame.setMasked(1);
            frame.setMaskKey(frame.generateMaskBytes());
            frame.setPayloadUnmasked(TestUtil.DEFAULT_TEST_WS_MESSAGE.getBytes());

            TestSequenceItem item = new TestSequenceItem();
            item.setDelayMsec(0);
            item.setTestSequenceItemType(TestSequenceItemType.FRAME);
            item.setFrame(frame);
            steps.add(item);

            // A ping
            frame = new WebsocketFrame();
            frame.setFin(1);
            frame.setDirection(WebsocketDirection.OUTBOUND);
            frame.setOpcode(WebsocketFrameType.PING);
            frame.setMasked(1);
            frame.setMaskKey(frame.generateMaskBytes());
            frame.setPayloadUnmasked("ABCDEFG".getBytes(StandardCharsets.UTF_8));
            item = new TestSequenceItem();
            item.setDelayMsec(0);
            item.setTestSequenceItemType(TestSequenceItemType.FRAME);
            item.setFrame(frame);
            steps.add(item);

            // A close
            frame = new WebsocketFrame();
            frame.setFin(1);
            frame.setDirection(WebsocketDirection.OUTBOUND);
            frame.setOpcode(WebsocketFrameType.CLOSE);
            frame.setMasked(1);
            frame.setMaskKey(frame.generateMaskBytes());
            frame.setPayloadUnmasked(null);
            item = new TestSequenceItem();
            item.setDelayMsec(0);
            item.setTestSequenceItemType(TestSequenceItemType.FRAME);
            item.setFrame(frame);
            steps.add(item);

            // An IOWAIT
            item = new TestSequenceItem();
            item.setDelayMsec(1000);
            item.setTestSequenceItemType(TestSequenceItemType.IOWAIT);
            steps.add(item);

            manualTesterController.addTestSteps(steps);

        });

        /*
            Import export traffic sub menus
         */

        // Export selected websocket frames
        frmMainView.mnuExportSelectedWsToFile.addActionListener( actionEvent -> {
            String[] messageIds = GuiUtils.getSelectedMessageIds(frmMainView.pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.tblWebsocketTraffic);
            if ( messageIds != null && messageIds.length > 0 ) {
                JFileChooser jfcChooser = new JFileChooser();
                jfcChooser.setDialogTitle("Export websocket frames");
                jfcChooser.setSelectedFile(new File("frames.raw"));
                if ( jfcChooser.showSaveDialog(frmMainView) == JFileChooser.APPROVE_OPTION ) {
                    if ( jfcChooser.getSelectedFile() != null ) {
                        System.out.println(String.format("Exporting %d frames", messageIds.length));
                        String export = exportSelectedFramesByMessageId(messageIds);
                        if ( export != null ) {
                            try {
                                FileUtils.putFileContent(jfcChooser.getSelectedFile().getPath(),export.getBytes(StandardCharsets.UTF_8));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            System.out.println("ERROR EXPORT IS NULL");
                        }
                    }
                }
            }
        });

        // Export HTTP request
        frmMainView.mnuExportSelectedHTTPToFile.addActionListener( actionEvent -> {
            String[] messageIds = GuiUtils.getSelectedMessageIds(frmMainView.pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.tblHttpTraffic);
            if ( messageIds != null && messageIds.length > 0 ) {
                JFileChooser jfcChooser = new JFileChooser();
                jfcChooser.setDialogTitle("Export HTTP request");
                jfcChooser.setSelectedFile(new File("httprequest.raw"));
                if ( jfcChooser.showSaveDialog(frmMainView) == JFileChooser.APPROVE_OPTION ) {
                    if ( jfcChooser.getSelectedFile() != null ) {
                        String export = exportSelectedHttpRequest(messageIds[0]);
                        if ( export != null ) {
                            try {
                                FileUtils.putFileContent(
                                        jfcChooser.getSelectedFile().getPath(),
                                        String.format(
                                                "http:%s",
                                                Base64.getEncoder().encodeToString(export.getBytes())
                                        ).getBytes(StandardCharsets.UTF_8)
                                );
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });

        // export conversation
        frmMainView.mnuExportConversationToFile.addActionListener( actionEvent -> {
            String[] selectedHttp = GuiUtils.getSelectedMessageIds(frmMainView.pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.tblHttpTraffic);
            if ( selectedHttp != null && selectedHttp.length > 0 ) {
                String httpExport = exportSelectedHttpRequest(selectedHttp[0]);
                String frameExport = exportSelectedFramesByUpgradeUUID(selectedHttp[0]);
                JFileChooser jfcChooser = new JFileChooser();
                jfcChooser.setDialogTitle("Export conversation");
                jfcChooser.setSelectedFile(new File("conversation.raw"));
                if ( jfcChooser.showSaveDialog(frmMainView) == JFileChooser.APPROVE_OPTION ) {
                    if ( jfcChooser.getSelectedFile() != null ) {
                        if ( httpExport != null && frameExport != null) {
                            try {
                                FileUtils.putFileContent(
                                        jfcChooser.getSelectedFile().getPath(),
                                        String.format("%s\n%s",httpExport,frameExport).getBytes(StandardCharsets.UTF_8)
                                );
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });

        // Import frames from file
        frmMainView.mnuItemImportFramesFromFile.addActionListener( actionEvent -> {
            if ( frmMainView.jtabMain.getSelectedIndex() >= 0 ) {
                if (frmMainView.jtabMain.getTitleAt(frmMainView.jtabMain.getSelectedIndex()).startsWith("Manual tester") | frmMainView.jtabMain.getTitleAt(frmMainView.jtabMain.getSelectedIndex()).startsWith("Immediate")) {
                    String fileName = null;
                    JFileChooser jfcChooser = new JFileChooser();
                    jfcChooser.setDialogTitle("Import traffic");
                    jfcChooser.setFileFilter(new FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            if ( file.isDirectory() ) {
                                return true;
                            }
                            String filename = file.getName().toLowerCase();
                            return filename.endsWith(".raw");
                        }

                        @Override
                        public String getDescription() {
                            return "traffic exports ( *.raw )";
                        }
                    });
                    if ( jfcChooser.showOpenDialog(frmMainView) == JFileChooser.APPROVE_OPTION ) {
                        if ( jfcChooser.getSelectedFile() != null ) {
                            fileName = jfcChooser.getSelectedFile().getPath();
                        }
                    }

                    if ( fileName != null ) {
                        try {
                            byte fileContent[] = FileUtils.getFileContent(fileName);
                            if ( fileContent != null ) {
                                for ( String line : new String(fileContent).split("\n")) {
                                    String recordParts[] = line.split(":");
                                    if ( recordParts.length == 2 ) {
                                        if ( recordParts[0].equals("websocket")) {
                                            String frameRecParts[] = recordParts[1].split(",");
                                            if ( frameRecParts.length > 5 ) {
                                                WebsocketFrame frame = new WebsocketFrame();
                                                frame.setFin(Integer.parseInt(frameRecParts[0]));
                                                frame.setRsv1(Integer.parseInt(frameRecParts[1]));
                                                frame.setRsv2(Integer.parseInt(frameRecParts[2]));
                                                frame.setRsv3(Integer.parseInt(frameRecParts[3]));
                                                frame.setOpcode(mainModel.getProjectModel().getProjectDataService().websocketFrameTypeFromString(frameRecParts[4]));
                                                frame.setDirection(frameRecParts[5].trim().equals("INBOUND") ? WebsocketDirection.INBOUND : WebsocketDirection.OUTBOUND);
                                                if ( frameRecParts.length > 6 ) {
                                                    if ( frameRecParts[6].length() > 0 ) {
                                                        frame.setPayloadUnmasked(Base64.getDecoder().decode(frameRecParts[6]));
                                                    }
                                                }
                                                if (frmMainView.jtabMain.getTitleAt(frmMainView.jtabMain.getSelectedIndex()).startsWith("Manual tester")) {
                                                    manualTesterController.addWebsocketFrame(frame);
                                                } else if (frmMainView.jtabMain.getTitleAt(frmMainView.jtabMain.getSelectedIndex()).startsWith("Immediate")) {
                                                    // Add to immediate
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        // import http request from file
        frmMainView.mnuItemImportHttpFromFile.addActionListener( actionEvent -> {

        });

        frmMainView.jtabMain.addChangeListener( changeEvent -> {
            if ( frmMainView.jtabMain.getSelectedIndex() >= 0 ) {
                if( frmMainView.jtabMain.getTitleAt(frmMainView.jtabMain.getSelectedIndex()).startsWith("Manual tester") || frmMainView.jtabMain.getTitleAt(frmMainView.jtabMain.getSelectedIndex()).startsWith("Immediate") ){
                    frmMainView.mnuItemImportFramesFromFile.setEnabled(true);
                    frmMainView.mnuItemImportHttpFromFile.setEnabled(true);
                    frmMainView.mnuConversations.setEnabled(true);
                }
                else {
                    frmMainView.mnuConversations.setEnabled(false);
                    frmMainView.mnuItemImportFramesFromFile.setEnabled(false);
                    frmMainView.mnuItemImportHttpFromFile.setEnabled(false);
                }
            }
        });
        /*
            Highlight traffic
         */
        frmMainView.mnuHighlightHttpTrafficRows.addActionListener( actionEvent -> {
            for ( int row : frmMainView.pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.tblHttpTraffic.getSelectedRows() ) {
                Color curVal = (Color) mainModel.getTrafficModel().getHttpTrafficModel().getValueAt(row,1);
                mainModel.getTrafficModel().getHttpTrafficModel().setValueAt( curVal.equals(Color.YELLOW) ? Color.WHITE : Color.YELLOW,row,1);
            }
        });
        frmMainView.mnuHighlightWsTrafficRows.addActionListener( actionEvent -> {
            for ( int row : frmMainView.pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.tblWebsocketTraffic.getSelectedRows() ) {
                Color curVal = (Color) mainModel.getTrafficModel().getWebsocketTrafficModel().getValueAt(row,2);
                mainModel.getTrafficModel().getWebsocketTrafficModel().setValueAt(curVal.equals(Color.YELLOW) ? Color.WHITE : Color.YELLOW,row,2);
            }
        });

        /*
            Traffic right click menu
         */
        frmMainView.mnuHttpRequestAddToManualTest.addActionListener(actionEvent -> {
            String[] messageIds = GuiUtils.getSelectedMessageIds(frmMainView.pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.tblHttpTraffic);
            try {
                if ( messageIds != null ) {
                    manualTesterController.addHttpMessageFromHistory( messageIds[0] );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        frmMainView.mnuHttpRequestAddToImmediate.addActionListener(actionEvent -> {
            String[] messageIds = GuiUtils.getSelectedMessageIds(frmMainView.pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.tblHttpTraffic);
            if ( messageIds != null ) {
                immediateController.addHttpMessageFromHistory( messageIds[0] );
            }
        });

        frmMainView.mnuWsFrameAddToManualTest.addActionListener(actionEvent -> {
            String[] messageIds = GuiUtils.getSelectedMessageIds(frmMainView.pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.tblWebsocketTraffic);
            try {
                manualTesterController.addWebsocketFrameFromHistory( messageIds );
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        frmMainView.mnuHttpRequestAddToImmediate.addActionListener(actionEvent -> {
            String[] messageIds = GuiUtils.getSelectedMessageIds(frmMainView.pnlTrafficView.pnlHttpTraffic.pnlHttpTrafficViewer.tblHttpTraffic);
            if ( messageIds != null && messageIds.length > 0 ) {
                immediateController.addHttpMessageFromHistory( messageIds[0] );
            }
        });

        frmMainView.mnuWsFrameAddToImmediate.addActionListener(actionEvent -> {
            String[] messageIds = GuiUtils.getSelectedMessageIds(frmMainView.pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.tblWebsocketTraffic);
            if ( messageIds != null && messageIds.length > 0 ) {
                immediateController.addWebsocketFrameFromHistory(messageIds[0]);
            }
        });

        // connections right click menu
        frmMainView.mnuConnectionsAddToManualTest.addActionListener( actionEvent -> {
            frmMainView.mnuHttpRequestAddToManualTest.doClick();
            ArrayList<String> msgIds = new ArrayList<String>();
            for ( int i = 0; i < frmMainView.pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.tblWebsocketTraffic.getRowCount(); i++) {
                String msgId = (String) frmMainView.pnlTrafficView.pnlWebsocketTraffic.pnlWebsocketTrafficViewer.tblWebsocketTraffic.getValueAt(i,0);
                if ( msgId != null ) {
                    msgIds.add(msgId);
                }
            }
            try {
                manualTesterController.addWebsocketFrameFromHistory(msgIds.stream().toArray(String[]::new));

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        frmMainView.pnlManualTesterView.pnlWsConversationEditor.pnlConversationBuilder.pnlWsConversationToolbar.jbtnAutomate.addActionListener(actionEvent -> {
            manualTesterController.saveTestSequence();
            automatedTesterController.createFromManualTest(
                    mainModel.getManualTesterModel().getCurrentManualTestRun().getTestSequence().getCopy(),
                    frmMainView.pnlManualTesterView.pnlWsConversationEditor.pnlHttpRequestResponse.jtxtTestRunName.getText());
        });

        /*
            File menu
         */
        frmMainView.mnuExportCa.addActionListener(actionEvent -> {
            JFileChooser jfcChooser = new JFileChooser();
            jfcChooser.setDialogTitle("Export Certificate Authority");
            jfcChooser.setSelectedFile(new File("mitmws.crt"));
            if ( jfcChooser.showSaveDialog(frmMainView) == JFileChooser.APPROVE_OPTION ) {
                if ( jfcChooser.getSelectedFile() != null ) {
                    try {
                        BouncyCastleSSLProvider pkiProvider = new BouncyCastleSSLProvider();
                        pkiProvider.init();
                        CertificateKeyBundle ckb = pkiProvider.getKeyStoreEntry("rootca");
                        FileUtils.putFileContent(
                                jfcChooser.getSelectedFile().getPath(),
                                PKIUtils.getCertificatePEM(ckb.getCertificateChain()).getBytes(StandardCharsets.UTF_8));
                    } catch (PKIProviderException e) {
                        //LOGGER.severe(e.getMessage());
                    } catch (FileNotFoundException e) {
                        //LOGGER.severe(e.getMessage());
                    } catch (IOException e) {
                        //LOGGER.severe(e.getMessage());
                    }
                }
            }
        });
        frmMainView.mnuProxyRestart.addActionListener(actionEvent -> {
            stopProxyServices();
            startProxyServices();
        });

        frmMainView.mnuProxyStart.addActionListener(actionEvent -> {
            startProxyServices();
        });

        frmMainView.mnuProxyStop.addActionListener(actionEvent -> {
            stopProxyServices();
        });

        frmMainView.mnuItemClose.addActionListener(actionEvent -> {
            stopProxyServices();
            logTailerThread.shutdown();
            passiveAnomalyDetectorThread.shutdown();
            if ( mainModel.getAnalyzerModel().getAnalyzerWorkerThread() != null ) {
                mainModel.getAnalyzerModel().getAnalyzerWorkerThread().shutdown();
            }
            try {
                logTailerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                passiveAnomalyDetectorThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                if ( mainModel.getAnalyzerModel().getAnalyzerWorkerThread() != null ) {
                    mainModel.getAnalyzerModel().getAnalyzerWorkerThread().join();
                }
            }
            catch ( InterruptedException e ) {
                e.printStackTrace();
            }
            frmMainView.dispose();
        });
        frmMainView.mnuItemNewProject.addActionListener(actionEvent -> {
            JFileChooser jfcChooser = new JFileChooser();
            jfcChooser.setDialogTitle("New project");
            String defaultNewProjectName = String.format("new_project_%d.mwsdb", System.currentTimeMillis()/1000);
            jfcChooser.setSelectedFile(new File(defaultNewProjectName));
            if ( jfcChooser.showOpenDialog(frmMainView) == JFileChooser.APPROVE_OPTION ) {
                if ( jfcChooser.getSelectedFile() != null ) {
                    stopProxyServices();
                    try {
                        mainModel.getProjectModel().load(jfcChooser.getSelectedFile().getPath());

                        startProxyServices();
                    } catch (ProjectDataServiceException e) {
                        e.printStackTrace();
                    }
                    finally {
                        frmMainView.setTitle(String.format("MitmWs - %s", mainModel.getProjectModel().getProjectDataService().getDbFilePath()));
                    }
                }
            }
        });



        frmMainView.mnuItemImportHttpFromFile.addActionListener( actionEvent -> {

        });
        frmMainView.mnuItemImportFramesFromFile.addActionListener( actionEvent -> {

        });

        frmMainView.mnuItemSave.addActionListener(actionEvent -> mainModel.getProjectModel().save());

        frmMainView.mnuItemSaveAs.addActionListener(actionEvent -> {
            JFileChooser jfcChooser = new JFileChooser();
            jfcChooser.setDialogTitle("Save as");
            String defaultNewProjectName = String.format("new_project_%d.mwsdb", System.currentTimeMillis()/1000);
            jfcChooser.setSelectedFile(new File(defaultNewProjectName));
            if ( jfcChooser.showSaveDialog(frmMainView) == JFileChooser.APPROVE_OPTION ) {
                if ( jfcChooser.getSelectedFile() != null ) {
                    stopProxyServices();
                    try {
                        mainModel.getProjectModel().save();
                        File curDb = new File(mainModel.getProjectModel().getProjectDataService().getDbFilePath());
                        mainModel.getProjectModel().getProjectDataService().disconnect();
                        curDb.renameTo(new File(jfcChooser.getSelectedFile().getPath()));
                        mainModel.getProjectModel().load(jfcChooser.getSelectedFile().getPath());
                        startProxyServices();
                    } catch (ProjectDataServiceException e) {
                        e.printStackTrace();
                    }
                    finally {
                        frmMainView.setTitle(String.format("MitmWs - %s", mainModel.getProjectModel().getProjectDataService().getDbFilePath()));
                    }
                }
            }
        });

        frmMainView.mnuUpdate.addActionListener(actionEvent -> {



        });
        frmMainView.mnuItemOpen.addActionListener(actionEvent -> {
            JFileChooser jfcChooser = new JFileChooser();
            jfcChooser.setDialogTitle("Open project");
            jfcChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if ( file.isDirectory() ) {
                        return true;
                    }
                    String filename = file.getName().toLowerCase();
                    return filename.endsWith(".mwsdb");
                }

                @Override
                public String getDescription() {
                    return "Websocket Project Sqlite DB ( *.mwsdb )";
                }
            });
            if ( jfcChooser.showOpenDialog(frmMainView) == JFileChooser.APPROVE_OPTION ) {
                if ( jfcChooser.getSelectedFile() != null ) {
                    stopProxyServices();
                    try {
                        mainModel.getProjectModel().unload();
                        mainModel.getProjectModel().load(jfcChooser.getSelectedFile().getPath());
                        startProxyServices();
                        loadTraffic();
                    } catch (ProjectDataServiceException | IOException e) {
                        e.printStackTrace();
                    }
                    finally {
                        frmMainView.setTitle(String.format("MitmWs - %s", mainModel.getProjectModel().getProjectDataService().getDbFilePath()));
                    }
                }
            }
        });
        frmMainView.frmSettingsView.btnApply.addActionListener(actionEvent -> {
            if (settingsController.validateSettings() ) {
                stopProxyServices();
                for ( int i = 0; i < mainModel.getSettingsModel().getSettingsTableModel().getRowCount(); i++ ) {
                    String key = (String)mainModel.getSettingsModel().getSettingsTableModel().getValueAt(i,1);
                    String value = (String)mainModel.getSettingsModel().getSettingsTableModel().getValueAt(i,2);
                    try {
                        mainModel.getSettingsModel().getApplicationConfig().setProperty(key,value);
                    } catch (ApplicationConfigException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    mainModel.getSettingsModel().getApplicationConfig().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startProxyServices();

            }
        });
        frmMainView.frmSettingsView.btnDefaults.addActionListener(actionEvent -> {
            stopProxyServices();
            try {
                mainModel.getSettingsModel().getApplicationConfig().loadDefaultConfig();
                mainModel.getSettingsModel().getApplicationConfig().saveConfig();
                settingsController.loadProperties();
            } catch (IOException e) {
                e.printStackTrace();
            }
            startProxyServices();
        });
        frmMainView.pnlTrafficView.pnlWebsocketTraffic.pnlBreakPointsView.jtblBreakpointQueue.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                frmMainView.pnlTrafficView.pnlWebsocketTraffic.trafficTabs.setTitleAt(1,"Breakpoints");
                int rowCount = frmMainView.pnlTrafficView.pnlWebsocketTraffic.pnlBreakPointsView.jtblBreakpointQueue.getModel().getRowCount();
                if ( rowCount > 0 ) {
                    frmMainView.pnlTrafficView.pnlWebsocketTraffic.trafficTabs.setTitleAt(1, String.format("Breakpoints (%d)", rowCount));
                }

            }
        });

        // Encoder / decoder tool
        frmMainView.mnuEncodeDecode.addActionListener( actionEvent -> {
            frmMainView.frmEncoderDecoderToolView.setVisible(true);
        });

        // Payloads
        frmMainView.mnuPayloads.addActionListener( actionEvent -> {
            frmMainView.frmPayloadsView.setVisible(true);
        });

        // Rules
        frmMainView.mnuRules.addActionListener( actionEvent -> {
            frmMainView.frmRulesView.setVisible(true);
        });

        // Updates
        frmMainView.mnuUpdate.addActionListener(actionEvent -> {
            frmMainView.frmUpdatesView.setVisible(true);
        });

        // Settings
        frmMainView.mnuSettings.addActionListener(actionEvent -> {
            frmMainView.frmSettingsView.setVisible(true);
        });

        // Logs
        frmMainView.mnuLogs.addActionListener(actionEvent -> {
            frmMainView.frmLogsView.setVisible(true);
        });

        // Logs
        frmMainView.mnuScriptConsole.addActionListener(actionEvent -> {
            frmMainView.frmScriptConsole.setVisible(true);
        });

        // Environment
        frmMainView.mnuEnvironment.addActionListener(actionEvent -> {
            frmMainView.frmEnvironmentView.setVisible(true);
        });

        frmMainView.mnuHttpRequestTester.addActionListener( actionEvent -> {
            frmMainView.frmHttpRequestTester.setVisible(true);
        });

        frmMainView.mnuProjectDataExplorer.addActionListener( actionEvent -> {
            frmMainView.frmProjectDataExplorer.setVisible(true);
        });
    }

    public void loadTraffic() throws IOException {
        if ( mainModel.getTrafficLoaderThread() == null ) {
            mainModel.setTrafficLoaderThread(new TrafficLoaderThread(mainModel));
            mainModel.getTrafficLoaderThread().start();
        }
    }

    public void startMaintenanceThread() {
        mainModel.setMaintenanceThread(new MaintenanceThread(mainModel));
        mainModel.getMaintenanceThread().start();
    }

    public void stopMaintenanceThread() {
        try {
            mainModel.getMaintenanceThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startAnomalyDetectorThread() {
        passiveAnomalyDetectorThread = new PassiveAnomalyDetectorThread(mainModel);
        passiveAnomalyDetectorThread.start();
        activeAnomalyDetectorThread = new ActiveAnomalyDetectorThread(mainModel);
        activeAnomalyDetectorThread.start();
    }

    public void stopAnomalyDetectorThread() {
        passiveAnomalyDetectorThread.shutdown();
        try {
            passiveAnomalyDetectorThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        activeAnomalyDetectorThread.shutdown();
        try {
            activeAnomalyDetectorThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void startInteractshMonitorThread() {
        interactshMonitorThread = new InteractshMonitorThread(mainModel.getInteractshModel());
        interactshMonitorThread.start();
    }

    public void stopInteractshMonitorThread() {
        interactshMonitorThread.shutdown();
        try {
            interactshMonitorThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startLogTailerThread() {
        logTailerThread = new LogTailerThread(mainModel.getAppLogModel());
        logTailerThread.start();
    }

    public void stopLogTailerThread() {
        logTailerThread.shutdown();
        try {
            logTailerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startProxyServices() {
        logQueueProcessorThread = new TrafficLogQueueProcessorThread(mainModel);
        logQueueProcessorThread.start();
        mainModel.getProxy().startAll();
        mainModel.getHttpServer().start();
        try {
            Thread.sleep(2000);
            mainModel.getMainStatusBarModel().setProxyListenAddr(mainModel.getProxy().getFirstInstanceListenAddress());
            if ( mainModel.getHttpServer().getHttpServerListenerThread() != null ) {
                mainModel.getMainStatusBarModel().setHttpListenAddr(mainModel.getHttpServer().getHttpServerListenerThread().getListenAddr());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopProxyServices() {
        mainModel.getProxy().stopAll();
        logQueueProcessorThread.shutdown();
        mainModel.getHttpServer().stop();
        try {
            logQueueProcessorThread.join();
            mainModel.getMainStatusBarModel().setProxyListenAddr(null);
            mainModel.getMainStatusBarModel().setHttpListenAddr(null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
        Main actions
     */

    public void updateMainTitle() {
        frmMainView.setTitle(String.format("MitmWs - %s", mainModel.getProjectModel().getProjectDataService().getDbFilePath()));
    }



    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

        if ( "ProjectModel.mitmwsDbFile".equals(propertyChangeEvent.getPropertyName())) {
            updateMainTitle();
            if ( mainModel.getProjectModel().getProjectDataService() != null ) {
                mainModel.getAnalyzerModel().init(mainModel.getProjectModel().getProjectDataService());
            }
        }
        if ( "UpdatesModel.installedUpdates".equals(propertyChangeEvent.getPropertyName())) {

        }
        if ( "AnomaliesModel.anomaliesTableModel".equals(propertyChangeEvent.getPropertyName())) {
            GuiUtils.updateTabTitle(frmMainView.jtabMain,"Anomalies",String.format("Anomalies (%d)",mainModel.getAnomaliesModel().getAnomaliesTableModel().getRowCount()));
        }
        if ( "InteractshModel.interaction".equals(propertyChangeEvent.getPropertyName())) {
            GuiUtils.updateTabTitle(frmMainView.jtabMain,"OOB",String.format("OOB (%d)",mainModel.getInteractshModel().getInteractionsTableModel().getRowCount()));
        }

        if ( "HttpProxyCleanupThread.websocketSessionTerminated".equals(propertyChangeEvent.getPropertyName())) {
            // Event that an http client handler with a websocket session was termianted
        }

    }

    public MainModel getMainModel() {
        return mainModel;
    }

    public FrmMainView getMainView() {
        return frmMainView;
    }
}
