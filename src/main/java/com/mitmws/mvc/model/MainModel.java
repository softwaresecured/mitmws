package com.mitmws.mvc.model;

import com.mitmws.httpproxy.HttpProxy;
import com.mitmws.httpserver.HttpServer;
import com.mitmws.mvc.thread.*;
import com.mitmws.projects.ProjectDataServiceException;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;

public class MainModel {

    private HttpProxy proxy;
    private HttpServer httpServer;

    private ProjectModel projectModel;
    private TrafficModel trafficModel;
    private TrafficSearchModel trafficSearchModel;
    private ManualTesterModel manualTesterModel;
    private AutomatedTesterModel automatedTesterModel;
    private LogModel logModel;
    private MainStatusBarModel mainStatusBarModel;
    private AnomaliesModel anomaliesModel;
    private EnvironmentModel environmentModel;
    private UpdatesModel updatesModel;
    private SettingsModel settingsModel;
    private ImmediateModel immediateModel;
    private PayloadsModel payloadsModel;
    private RulesModel rulesModel;
    private AnalyzerModel analyzerModel;
    private InteractshModel interactshModel;
    private ProtocolTesterModel protocolTesterModel;
    private ScriptConsoleModel scriptConsoleModel;
    private BreakpointModel breakpointModel;
    private EncoderDecoderToolModel encoderDecoderToolModel;
    private HttpRequestTesterModel httpRequestTesterModel;
    private ProjectDataExplorerModel projectDataExplorerModel;

    // Threads
    private MaintenanceThread maintenanceThread;
    private TrafficLoaderThread trafficLoaderThread;
    private TrafficSearchThread trafficSearchThread = null;
    private ImmediateThread immediateThread = null;
    private ProtocolTesterThread protocolTesterThread = null;
    private InteractshMonitorThread interactshMonitorThread = null;
    private RawTesterThread rawTesterThread = null;

    private SwingPropertyChangeSupport eventEmitter;

    public MainModel() throws IOException, ProjectDataServiceException {
        projectModel = new ProjectModel();
        mainStatusBarModel = new MainStatusBarModel();
        trafficSearchModel = new TrafficSearchModel(projectModel);
        manualTesterModel = new ManualTesterModel(projectModel);
        automatedTesterModel = new AutomatedTesterModel();
        updatesModel = new UpdatesModel();
        anomaliesModel = new AnomaliesModel();
        logModel = new LogModel();
        trafficModel = new TrafficModel(projectModel);
        environmentModel = new EnvironmentModel(projectModel);
        settingsModel = new SettingsModel();
        immediateModel = new ImmediateModel();
        payloadsModel = new PayloadsModel();
        eventEmitter = new SwingPropertyChangeSupport(this);
        httpServer = new HttpServer();
        analyzerModel = new AnalyzerModel();
        interactshModel = new InteractshModel();
        rulesModel = new RulesModel(); // needs to be after interactsh-model
        protocolTesterModel = new ProtocolTesterModel();
        scriptConsoleModel = new ScriptConsoleModel();
        breakpointModel = new BreakpointModel();
        encoderDecoderToolModel = new EncoderDecoderToolModel();
        httpRequestTesterModel = new HttpRequestTesterModel();
        projectDataExplorerModel = new ProjectDataExplorerModel();


        proxy = new HttpProxy(breakpointModel);
    }

    public ProjectDataExplorerModel getProjectDataExplorerModel() {
        return projectDataExplorerModel;
    }

    public HttpRequestTesterModel getHttpRequestTesterModel() {
        return httpRequestTesterModel;
    }

    public EncoderDecoderToolModel getEncoderDecoderToolModel() {
        return encoderDecoderToolModel;
    }

    public BreakpointModel getBreakpointModel() {
        return breakpointModel;
    }

    public ScriptConsoleModel getScriptConsoleModel() {
        return scriptConsoleModel;
    }

    public ProtocolTesterModel getProtocolTesterModel() {
        return protocolTesterModel;
    }

    public InteractshModel getInteractshModel() {
        return interactshModel;
    }

    public AnalyzerModel getAnalyzerModel() {
        return analyzerModel;
    }

    public void setAnalyzerModel(AnalyzerModel analyzerModel) {
        this.analyzerModel = analyzerModel;
    }

    public RulesModel getRulesModel() {
        return rulesModel;
    }

    public PayloadsModel getPayloadsModel() {
        return payloadsModel;
    }

    public ImmediateModel getImmediateModel() {
        return immediateModel;
    }

    public void setImmediateModel(ImmediateModel immediateModel) {
        this.immediateModel = immediateModel;
    }

    public InteractshMonitorThread getInteractshMonitorThread() {
        return interactshMonitorThread;
    }

    public void setInteractshMonitorThread(InteractshMonitorThread interactshMonitorThread) {
        this.interactshMonitorThread = interactshMonitorThread;
    }

    public RawTesterThread getRawTesterThread() {
        return rawTesterThread;
    }

    public void setRawTesterThread(RawTesterThread rawTesterThread) {
        this.rawTesterThread = rawTesterThread;
        eventEmitter.firePropertyChange("MainModel.rawTesterThread", null, this.rawTesterThread);
    }

    public ProtocolTesterThread getProtocolTesterThread() {
        return protocolTesterThread;
    }

    public void setProtocolTesterThread(ProtocolTesterThread protocolTesterThread) {
        this.protocolTesterThread = protocolTesterThread;
        eventEmitter.firePropertyChange("MainModel.protocolTesterThread", null, this.protocolTesterThread);
    }


    public ImmediateThread getImmediateThread() {
        return immediateThread;
    }

    public void setImmediateThread(ImmediateThread immediateThread) {
        this.immediateThread = immediateThread;
        eventEmitter.firePropertyChange("MainModel.immediateThread", null, this.immediateThread);
    }

    public TrafficLoaderThread getTrafficLoaderThread() {
        return trafficLoaderThread;
    }

    public void setTrafficLoaderThread(TrafficLoaderThread trafficLoaderThread) {
        this.trafficLoaderThread = trafficLoaderThread;
        eventEmitter.firePropertyChange("MainModel.trafficLoaderThread", null, this.trafficLoaderThread);
    }

    public MaintenanceThread getMaintenanceThread() {
        return maintenanceThread;
    }

    public void setMaintenanceThread(MaintenanceThread maintenanceThread) {
        this.maintenanceThread = maintenanceThread;
    }

    public TrafficSearchThread getTrafficSearchThread() {
        return trafficSearchThread;
    }

    public void setTrafficSearchThread(TrafficSearchThread trafficSearchThread) {
        this.trafficSearchThread = trafficSearchThread;
        eventEmitter.firePropertyChange("MainModel.trafficSearchThread", null, this.trafficSearchThread);
    }

    // Syncs active connections in HttpProxy with connections table in traffic model
    public void syncActiveConnections() {

        String[] activeConnections = proxy.getActiveWebsocketConnections();
        if ( trafficModel.getWebsocketConnectionsModel().getRowCount() > 1 ) {
            for ( int i = 1; i < trafficModel.getWebsocketConnectionsModel().getRowCount(); i++ ) {
                if ( Arrays.stream(activeConnections).anyMatch(trafficModel.getWebsocketConnectionsModel().getValueAt(i,0)::equals)) {
                    trafficModel.getWebsocketConnectionsModel().setValueAt("OPEN",i,2);
                }
                else {
                    trafficModel.getWebsocketConnectionsModel().setValueAt("CLOSED",i,2);
                }
            }
        }
    }

    public SettingsModel getSettingsModel() {
        return settingsModel;
    }


    public UpdatesModel getUpdatesModel() {
        return updatesModel;
    }

    public void setUpdatesModel(UpdatesModel updatesModel) {
        this.updatesModel = updatesModel;
    }

    public TrafficSearchModel getTrafficSearchModel() {
        return trafficSearchModel;
    }

    public AnomaliesModel getAnomaliesModel() {
        return anomaliesModel;
    }

    public LogModel getAppLogModel() {
        return logModel;
    }

    public ManualTesterModel getManualTesterModel() {
        return manualTesterModel;
    }

    public AutomatedTesterModel getAutomatedTesterModel() {
        return automatedTesterModel;
    }

    public HttpProxy getProxy() {
        return proxy;
    }

    public TrafficModel getTrafficModel() {
        return trafficModel;
    }

    public ProjectModel getProjectModel() {
        return projectModel;
    }

    public MainStatusBarModel getMainStatusBarModel() {
        return mainStatusBarModel;
    }

    public EnvironmentModel getEnvironmentModel() {
        return environmentModel;
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }

    public HttpServer getHttpServer() {
        return httpServer;
    }

    public void setHttpServer(HttpServer httpServer) {
        this.httpServer = httpServer;
    }
}
