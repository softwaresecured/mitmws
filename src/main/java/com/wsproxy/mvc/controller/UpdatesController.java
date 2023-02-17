package com.wsproxy.mvc.controller;

import com.wsproxy.configuration.ApplicationConfig;
import com.wsproxy.mvc.model.UpdatesModel;
import com.wsproxy.mvc.view.panels.updates.PnlUpdatesView;
import com.wsproxy.updates.UpdateManager;
import com.wsproxy.util.FileUtils;
import com.wsproxy.util.GuiUtils;
import com.wsproxy.util.ManifestUtils;
import com.wsproxy.util.NetUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;

public class UpdatesController implements PropertyChangeListener {
    private UpdatesModel updatesModel;
    private PnlUpdatesView pnlUpdatesView;
    private ApplicationConfig applicationConfig = new ApplicationConfig();
    private UpdateManager updateManager = new UpdateManager();
    private JFrame parent;
    public UpdatesController(UpdatesModel updatesModel, PnlUpdatesView pnlUpdatesView) {
        this.updatesModel = updatesModel;
        this.pnlUpdatesView = pnlUpdatesView;
        this.updatesModel.addListener(this);
        initEventListeners();
        loadLocal();
        loadRemote();
    }

    public void loadLocal() {
        String paths[] = { "payloads","scripts/httpserver","scripts/rules/active","scripts/rules/passive","scripts/upgrade","scripts/variables" };
        for ( String path : paths ) {
            String filePath = String.format("%s/%s", applicationConfig.getConfigDirPath(),path);
            File f = new File(filePath);
            for ( String fileName : f.list()) {
                String updateItem = String.format("%s/%s", filePath,fileName).substring(applicationConfig.getConfigDirPath().length()+1);
                updatesModel.addRepoItem(GuiUtils.uppercaseFirst(updateItem.split("[\\/]")[0]),"Local",updateItem,false);
            }
        }
        GuiUtils.tableSelectLast(pnlUpdatesView.pnlUpdatesTableViewer.jtblUpdates);
    }

    public void loadRemote() {
        try {
            HashMap<String, String> availableUpdates = updateManager.getApplicableUpdates(ManifestUtils.manifestPaths);
            for ( String updateItem : availableUpdates.keySet()) {
                updatesModel.addRepoItem(updateItem.split("[\\/]")[0],"Update",updateItem,true);
                pnlUpdatesView.pnlUpdatesTableViewer.pnlUpdatesToolbar.lblUpdateServerUrl.setText(String.format("%s",applicationConfig.getProperty("updates.url")));
            }
            pnlUpdatesView.pnlUpdatesTableViewer.pnlUpdatesToolbar.btnInstallUpdates.setEnabled( availableUpdates.size() > 0 ? true : false );
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException | InvalidKeyException e) {
            pnlUpdatesView.pnlUpdatesTableViewer.pnlUpdatesToolbar.lblUpdateServerUrl.setText(String.format("%s (error)",applicationConfig.getProperty("updates.url")));
        }
        GuiUtils.tableSelectLast(pnlUpdatesView.pnlUpdatesTableViewer.jtblUpdates);
    }

    public void checkUpdates() {
        updatesModel.getUpdatesTableModel().setRowCount(0);
        loadLocal();
        loadRemote();
    }

    public void initEventListeners() {
        // Check for updates
        pnlUpdatesView.pnlUpdatesTableViewer.pnlUpdatesToolbar.btnCheckUpdates.addActionListener( actionEvent -> {
            checkUpdates();
        });

        // Install updates
        pnlUpdatesView.pnlUpdatesTableViewer.pnlUpdatesToolbar.btnInstallUpdates.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    updateManager.applyUpdates(updateManager.getApplicableUpdates(ManifestUtils.manifestPaths));
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (NoSuchAlgorithmException ex) {
                    ex.printStackTrace();
                } catch (InvalidKeySpecException ex) {
                    ex.printStackTrace();
                } catch (SignatureException ex) {
                    ex.printStackTrace();
                } catch (InvalidKeyException ex) {
                    ex.printStackTrace();
                }
                checkUpdates();
            }
        });
        pnlUpdatesView.pnlUpdatesTableViewer.jtblUpdates.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selectedRow = pnlUpdatesView.pnlUpdatesTableViewer.jtblUpdates.getSelectedRow();
                pnlUpdatesView.pnlUpdateContentViewer.jtxtContent.setText("");
                if ( selectedRow >= 0 ) {
                    String source = (String) pnlUpdatesView.pnlUpdatesTableViewer.jtblUpdates.getValueAt(selectedRow,2);
                    String path = (String) pnlUpdatesView.pnlUpdatesTableViewer.jtblUpdates.getValueAt(selectedRow,3);
                    String content = "";
                    if ( source.toLowerCase().equals("local")) {
                        String localPath = String.format("%s/%s", applicationConfig.getConfigDirPath(),path);
                        try {
                            byte fileContent[] = FileUtils.getFileContent(localPath);
                            if ( fileContent != null ) {
                                content = new String(fileContent);
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    if ( source.toLowerCase().equals("update")) {
                        String remotePath = String.format("%s/%s", applicationConfig.getProperty("updates.url"),path);
                        try {
                            content = NetUtils.getRemoteUrl(remotePath);
                        } catch (IOException ex) {
                            pnlUpdatesView.pnlUpdatesTableViewer.pnlUpdatesToolbar.lblUpdateServerUrl.setText(String.format("%s (error)",applicationConfig.getProperty("updates.url")));
                        }
                    }
                    pnlUpdatesView.pnlUpdateContentViewer.jtxtContent.setText(content);
                }
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

    }
}
