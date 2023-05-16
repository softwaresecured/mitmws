package com.mitmws.mvc;

import com.mitmws.mvc.controller.MainController;
import com.mitmws.mvc.model.MainModel;
import com.mitmws.mvc.view.frames.FrmMainView;
import com.mitmws.projects.ProjectDataServiceException;
import com.mitmws.util.ThemeUtils;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class MitmWsGui {
    private FrmMainView frmMain;
    private MainModel mainModel;
    private MainController mainController;
    private static Object lock = new Object();
    public MitmWsGui() throws IOException, ProjectDataServiceException {
        mainModel = new MainModel();
        frmMain = new FrmMainView(mainModel);
        mainController = new MainController(mainModel,frmMain);
        initEventListeners();
        ThemeUtils.setLookAndFeel(frmMain);
        ThemeUtils.setLookAndFeel(frmMain.frmPayloadsView);
        ThemeUtils.setLookAndFeel(frmMain.frmEncoderDecoderToolView);
        ThemeUtils.setLookAndFeel(frmMain.frmRulesView);
        ThemeUtils.setLookAndFeel(frmMain.frmUpdatesView);
        ThemeUtils.setLookAndFeel(frmMain.frmSettingsView);
        ThemeUtils.setLookAndFeel(frmMain.frmLogsView);
        ThemeUtils.setLookAndFeel(frmMain.frmScriptConsole);
        ThemeUtils.setLookAndFeel(frmMain.frmEnvironmentView);
        ThemeUtils.setLookAndFeel(frmMain.frmHttpRequestTester);
        ThemeUtils.setLookAndFeel(frmMain.frmProjectDataExplorer);

    }

    private void initEventListeners() {
        frmMain.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                synchronized (lock) {
                    mainController.getMainView().setVisible(false);
                    mainController.stopProxyServices();
                    mainController.stopLogTailerThread();
                    mainController.stopAnomalyDetectorThread();
                    lock.notify();
                }
            }
        });
    }

    public void start() throws InterruptedException {
        Thread guiThread = new Thread(() -> {
            synchronized(lock) {
                mainController.getMainView().setVisible(true);
                mainController.startProxyServices();
                while (mainController.getMainView().isVisible()) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        guiThread.start();
        guiThread.join();
    }
}
