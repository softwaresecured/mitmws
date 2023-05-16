package com.mitmws.mvc.controller;

import com.mitmws.configuration.ApplicationConfig;
import com.mitmws.mvc.model.UpdatesModel;
import com.mitmws.mvc.view.frames.FrmUpdatesView;
import com.mitmws.updates.UpdateManager;
import com.mitmws.util.FileUtils;
import com.mitmws.util.GuiUtils;
import com.mitmws.util.ManifestUtils;

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
    private FrmUpdatesView frmUpdatesView;
    private ApplicationConfig applicationConfig = new ApplicationConfig();
    private UpdateManager updateManager = new UpdateManager();
    private JFrame parent;
    public UpdatesController(UpdatesModel updatesModel, FrmUpdatesView frmUpdatesView) {
        this.updatesModel = updatesModel;
        this.frmUpdatesView = frmUpdatesView;
        this.updatesModel.addListener(this);
        initEventListeners();
        frmUpdatesView.pnlUpdatesTableViewer.pnlUpdatesToolbar.lblUpdateServerUrl.setText(String.format("%s",applicationConfig.getProperty("updates.url")));
        loadLocal();
        loadRemote();
    }

    /*
        Loads local content from the .mitmws home directory
     */
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
        GuiUtils.tableSelectLast(frmUpdatesView.pnlUpdatesTableViewer.jtblUpdates);
    }

    /*
        Load remote content
     */
    public void loadRemote() {
        try {
            HashMap<String, String> availableUpdates = updateManager.getApplicableUpdates(ManifestUtils.manifestPaths);
            for ( String updateItem : availableUpdates.keySet()) {
                updatesModel.addRepoItem(updateItem.split("[\\/]")[0],"Update",updateItem,true);
            }
            frmUpdatesView.pnlUpdatesTableViewer.pnlUpdatesToolbar.btnInstallUpdates.setEnabled( availableUpdates.size() > 0 ? true : false );
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException | InvalidKeyException e) {
            frmUpdatesView.pnlUpdatesTableViewer.pnlUpdatesToolbar.lblUpdateServerUrl.setText(String.format("%s (error)",applicationConfig.getProperty("updates.url")));
        }
        GuiUtils.tableSelectLast(frmUpdatesView.pnlUpdatesTableViewer.jtblUpdates);
    }

    public void checkUpdates() {
        updatesModel.getUpdatesTableModel().setRowCount(0);
        loadLocal();
        loadRemote();
    }

    public void initEventListeners() {
        // Check for updates
        frmUpdatesView.pnlUpdatesTableViewer.pnlUpdatesToolbar.btnCheckUpdates.addActionListener(actionEvent -> {
            checkUpdates();
        });

        // Install updates
        frmUpdatesView.pnlUpdatesTableViewer.pnlUpdatesToolbar.btnInstallUpdates.addActionListener(new ActionListener() {
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
        frmUpdatesView.pnlUpdatesTableViewer.jtblUpdates.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selectedRow = frmUpdatesView.pnlUpdatesTableViewer.jtblUpdates.getSelectedRow();
                frmUpdatesView.pnlUpdateContentViewer.jtxtContent.setText("");
                if ( selectedRow >= 0 ) {
                    String source = (String) frmUpdatesView.pnlUpdatesTableViewer.jtblUpdates.getValueAt(selectedRow,2);
                    String path = (String) frmUpdatesView.pnlUpdatesTableViewer.jtblUpdates.getValueAt(selectedRow,3);
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
                        try {
                            content = updateManager.getRemoteContent(path);
                        } catch (IOException ex) {
                            frmUpdatesView.pnlUpdatesTableViewer.pnlUpdatesToolbar.lblUpdateServerUrl.setText(String.format("%s (error)",applicationConfig.getProperty("updates.url")));
                        }
                    }
                    frmUpdatesView.pnlUpdateContentViewer.jtxtContent.setText(content);
                }
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

    }
}
