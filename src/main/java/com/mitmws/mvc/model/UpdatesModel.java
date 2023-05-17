package com.mitmws.mvc.model;

import com.mitmws.updates.UpdateManager;

import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.table.DefaultTableModel;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

public class UpdatesModel {
    private UpdateManager updateManager = new UpdateManager();
    private HashMap<String, String> availableUpdates;
    private HashMap<String, String> installedUpdates;
    private DefaultTableModel updatesTableModel;
    private ArrayList<String> recentlyInstalledUpdates = new ArrayList<String>();
    private SwingPropertyChangeSupport eventEmitter;
    public UpdatesModel() {
        availableUpdates = new HashMap<String, String>();
        eventEmitter = new SwingPropertyChangeSupport(this);
        updatesTableModel = new DefaultTableModel();
        for ( String col: new String[] { "updateAvailable", "Type","Source","Path" }) {
            updatesTableModel.addColumn(col);
        }
    }

    public void addRepoItem( String type, String source, String path, boolean updateAvailable ) {
        updatesTableModel.addRow(new Object[] {
                updateAvailable,
                type,
                source,
                path
        });
    }

    public DefaultTableModel getUpdatesTableModel() {
        return updatesTableModel;
    }

    public void setUpdatesTableModel(DefaultTableModel updatesTableModel) {
        this.updatesTableModel = updatesTableModel;
    }

    public UpdateManager getUpdateManager() {
        return updateManager;
    }

    public HashMap<String, String> getInstalledUpdates() {
        return installedUpdates;
    }

    public void setInstalledUpdates(HashMap<String, String> installedUpdates) {
        this.installedUpdates = installedUpdates;
        eventEmitter.firePropertyChange("UpdatesModel.installedUpdates", null, this.installedUpdates);
    }

    public HashMap<String, String> getAvailableUpdates() {
        return availableUpdates;
    }

    public void setAvailableUpdates(HashMap<String, String> availableUpdates) {
        this.availableUpdates = availableUpdates;
        eventEmitter.firePropertyChange("UpdatesModel.availableUpdates", null, this.availableUpdates);
    }

    public ArrayList<String> getRecentlyInstalledUpdates() {
        return recentlyInstalledUpdates;
    }

    public void setRecentlyInstalledUpdates(ArrayList<String> recentlyInstalledUpdates) {
        this.recentlyInstalledUpdates = recentlyInstalledUpdates;
        eventEmitter.firePropertyChange("UpdatesModel.recentlyInstalledUpdates", null, this.availableUpdates);
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
