package com.wsproxy.mvc.model;

import com.wsproxy.util.GuiUtils;

import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.table.DefaultTableModel;
import java.beans.PropertyChangeListener;
import java.util.Date;

public class TrafficSearchModel {
    private SwingPropertyChangeSupport eventEmitter;
    private String searchText = "";
    private String searchResultText = "";
    private boolean searchRegex = false;
    private boolean searchProxy = true;
    private boolean searchManualTester = true;
    private boolean searchAutomatedTester = true;
    private boolean searchImmediate = true;
    private int maxResults = 100;
    private DefaultTableModel resultsModel = null;
    public TrafficSearchModel(ProjectModel projectModel) {
        eventEmitter = new SwingPropertyChangeSupport(this);
        resultsModel = new DefaultTableModel();
        for ( String col: new String[] { "id","Time", "Source", "Text sample"}) {
            resultsModel.addColumn(col);
        }
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        String oldVal = this.searchText;
        this.searchText = searchText;
        eventEmitter.firePropertyChange("TrafficSearchModel.searchText", oldVal, this.searchText);
    }

    public String getSearchResultText() {
        return searchResultText;
    }

    public void setSearchResultText(String searchResultText) {
        String oldVal = this.searchResultText;
        this.searchResultText = searchResultText;
        eventEmitter.firePropertyChange("TrafficSearchModel.searchResultText", oldVal, this.searchResultText);
    }

    public boolean isSearchRegex() {
        return searchRegex;
    }

    public void setSearchRegex(boolean searchRegex) {
        boolean oldVal = this.searchRegex;
        this.searchRegex = searchRegex;
        eventEmitter.firePropertyChange("TrafficSearchModel.searchRegex", oldVal, this.searchRegex);
    }

    public boolean isSearchProxy() {
        return searchProxy;
    }

    public void setSearchProxy(boolean searchProxy) {
        boolean oldVal = this.searchProxy;
        this.searchProxy = searchProxy;
        eventEmitter.firePropertyChange("TrafficSearchModel.searchProxy", oldVal, this.searchProxy);
    }

    public boolean isSearchManualTester() {
        return searchManualTester;
    }

    public void setSearchManualTester(boolean searchManualTester) {
        boolean oldVal = this.searchManualTester;
        this.searchManualTester = searchManualTester;
        eventEmitter.firePropertyChange("TrafficSearchModel.searchManualTester", oldVal, this.searchManualTester);
    }

    public boolean isSearchAutomatedTester() {
        return searchAutomatedTester;
    }

    public void setSearchAutomatedTester(boolean searchAutomatedTester) {
        boolean oldVal = this.searchAutomatedTester;
        this.searchAutomatedTester = searchAutomatedTester;
        eventEmitter.firePropertyChange("TrafficSearchModel.searchAutomatedTester", oldVal, this.searchAutomatedTester);
    }

    public boolean isSearchImmediate() {
        return searchImmediate;
    }

    public void setSearchImmediate(boolean searchImmediate) {
        boolean oldVal = this.searchImmediate;
        this.searchImmediate = searchImmediate;
        eventEmitter.firePropertyChange("TrafficSearchModel.searchImmediate", oldVal, this.searchImmediate);
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        int oldVal = this.maxResults;
        this.maxResults = maxResults;
        eventEmitter.firePropertyChange("TrafficSearchModel.maxResults", oldVal, this.maxResults);
    }

    public void addSearchResult( String id, long reqTime, String source, String text ) {
        int oldVal = resultsModel.getRowCount();
        resultsModel.addRow(new Object[] {
                id,
                GuiUtils.trafficTimeFmt.format(new Date(reqTime)),
                source,
                GuiUtils.getSnippet(text,500)
        });
        eventEmitter.firePropertyChange("TrafficSearchModel.searchResultCount", oldVal, resultsModel.getRowCount());
    }

    public DefaultTableModel getResultsModel() {
        return resultsModel;
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
