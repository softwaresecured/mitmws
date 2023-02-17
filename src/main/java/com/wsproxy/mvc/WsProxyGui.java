package com.wsproxy.mvc;

import com.wsproxy.mvc.controller.MainController;
import com.wsproxy.mvc.model.MainModel;
import com.wsproxy.mvc.view.frames.FrmMainView;
import com.wsproxy.projects.ProjectDataServiceException;
import com.wsproxy.util.ThemeUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class WsProxyGui {
    private FrmMainView frmMain;
    private MainModel mainModel;
    private MainController mainController;
    private static Object lock = new Object();
    public WsProxyGui() throws IOException, ProjectDataServiceException {
        mainModel = new MainModel();
        frmMain = new FrmMainView(mainModel);
        mainController = new MainController(mainModel,frmMain);
        initEventListeners();
        ThemeUtils.setLookAndFeel(frmMain);
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
