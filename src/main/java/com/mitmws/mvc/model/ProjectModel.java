package com.mitmws.mvc.model;
import com.mitmws.anomalydetection.DetectedAnomaly;
import com.mitmws.projects.ProjectDataService;
import com.mitmws.projects.ProjectDataServiceException;
import com.mitmws.tester.AutomatedTestRun;
import com.mitmws.tester.ManualTestRun;
import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class ProjectModel {
    private String name;
    private String description;
    private String version;
    private ArrayList<AutomatedTestRun> automatedTestRuns;
    private ArrayList<ManualTestRun> manualTestRuns;
    private ArrayList<DetectedAnomaly> detectedAnomalies;
    private ArrayBlockingQueue passiveAnomalyScanQueue;
    private ArrayBlockingQueue activeAnomalyScanQueue;
    private ProjectDataService projectDataService;
    private SwingPropertyChangeSupport eventEmitter;
    public ProjectModel() throws ProjectDataServiceException {
        eventEmitter = new SwingPropertyChangeSupport(this);
        reset();
    }

    public void reset() throws ProjectDataServiceException {
        name = null;
        description = null;
        version = null;
        automatedTestRuns = new ArrayList<AutomatedTestRun>();
        manualTestRuns = new ArrayList<ManualTestRun>();
        detectedAnomalies = new ArrayList<DetectedAnomaly>();
        passiveAnomalyScanQueue = new ArrayBlockingQueue(8192*2);
        activeAnomalyScanQueue = new ArrayBlockingQueue(8192);
    }

    private String getDefaultDb() throws IOException {
        File tmpSessionFile = File.createTempFile("mitmws",".mwsdb");
        return tmpSessionFile.getPath();
    }

    public void createDefaultProject() throws IOException, ProjectDataServiceException {
        load(getDefaultDb());
    }

    public void load ( String mitmwsDbFile ) throws ProjectDataServiceException {
        reset();
        projectDataService = new ProjectDataService(mitmwsDbFile);
        setName(projectDataService.getProjectName());
        setDescription(projectDataService.getProjectDescription());
        setVersion(projectDataService.getProjectVersion());
        setManualTestRuns(projectDataService.getManualTestRuns());
        setAutomatedTestRuns(projectDataService.getAutomatedTestRuns());
        setDetectedAnomalies(projectDataService.getDetectedAnomalies());
        eventEmitter.firePropertyChange("ProjectModel.mitmwsDbFile", null, mitmwsDbFile);

    }

    public void unload() throws ProjectDataServiceException {
        getProjectDataService().disconnect();
        eventEmitter.firePropertyChange("ProjectModel.mitmwsDbFile", null, null);
    }

    public void save() {
        if ( projectDataService != null ) {
            try {
                projectDataService.setProjectName(getName());
                projectDataService.setProjectDescription(getDescription());
                projectDataService.setProjectVersion(getVersion());
                projectDataService.saveAutomatedTestRuns(getAutomatedTestRuns());
                projectDataService.saveManualTestRuns(getManualTestRuns());
                projectDataService.saveDetectedAnomalies(getDetectedAnomalies());
            } catch (ProjectDataServiceException e) {
                e.printStackTrace();
            }
        }
    }

    public ManualTestRun getManualTestRunByName( String name ) {
        ManualTestRun testRun = null;
        for ( ManualTestRun manualTestRun : manualTestRuns ) {
            if ( manualTestRun.getTestName().equals(name)) {
                testRun = manualTestRun;
                break;
            }
        }
        return testRun;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws ProjectDataServiceException {
        String old = this.name;
        if ( projectDataService != null && projectDataService.isConnected()) {
            projectDataService.setProjectName(name);
        }
        this.name = name;
        eventEmitter.firePropertyChange("ProjectModel.name", old, name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) throws ProjectDataServiceException {
        String old = this.description;
        if ( projectDataService != null ) {
            projectDataService.setProjectDescription(description);
        }
        this.description = description;
        eventEmitter.firePropertyChange("ProjectModel.description", old, description);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) throws ProjectDataServiceException {
        String old = this.version;
        if ( projectDataService != null ) {
            projectDataService.setProjectVersion(version);
        }
        this.version = version;
        eventEmitter.firePropertyChange("ProjectModel.version", old, version);
    }

    public ArrayList<AutomatedTestRun> getAutomatedTestRuns() {
        return automatedTestRuns;
    }

    public void setAutomatedTestRuns(ArrayList<AutomatedTestRun> automatedTestRuns) throws ProjectDataServiceException {
        this.automatedTestRuns = automatedTestRuns;
        eventEmitter.firePropertyChange("ProjectModel.automatedTestRuns", null, automatedTestRuns);
    }

    public ArrayList<ManualTestRun> getManualTestRuns() {
        return manualTestRuns;
    }

    public void setManualTestRuns(ArrayList<ManualTestRun> manualTestRuns) throws ProjectDataServiceException {
        this.manualTestRuns = manualTestRuns;
        eventEmitter.firePropertyChange("ProjectModel.manualTestRuns", null, manualTestRuns);
    }

    public ArrayList<DetectedAnomaly> getDetectedAnomalies() {
        return detectedAnomalies;
    }

    public void setDetectedAnomalies(ArrayList<DetectedAnomaly> detectedAnomalies) throws ProjectDataServiceException {
        this.detectedAnomalies = detectedAnomalies;
        eventEmitter.firePropertyChange("ProjectModel.detectedAnomalies", null, detectedAnomalies);
    }

    public ArrayBlockingQueue getPassiveAnomalyScanQueue() {
        return passiveAnomalyScanQueue;
    }

    public ArrayBlockingQueue getActiveAnomalyScanQueue() {
        return activeAnomalyScanQueue;
    }

    public void setActiveAnomalyScanQueue(ArrayBlockingQueue activeAnomalyScanQueue) {
        this.activeAnomalyScanQueue = activeAnomalyScanQueue;
    }

    public ProjectDataService getProjectDataService() {
        return projectDataService;
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
