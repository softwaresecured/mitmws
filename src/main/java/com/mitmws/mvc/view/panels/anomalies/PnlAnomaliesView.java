package com.mitmws.mvc.view.panels.anomalies;

import com.mitmws.mvc.model.AnomaliesModel;
import com.mitmws.mvc.model.ProjectModel;

import javax.swing.*;
import java.awt.*;


public class PnlAnomaliesView extends JPanel {
    public PnlDetectedAnomalies pnlDetectedAnomalies;
    private AnomaliesModel anomaliesModel;
    private ProjectModel projectModel;
    public PnlAnomaliesView(AnomaliesModel anomaliesModel, ProjectModel projectModel) {
        this.anomaliesModel = anomaliesModel;
        this.projectModel = projectModel;
        initLayout();
    }

    public void initLayout() {
        pnlDetectedAnomalies = new PnlDetectedAnomalies(anomaliesModel);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridy = 1;
        gbc.gridx = 1;
        add(pnlDetectedAnomalies, gbc);
    }
}
