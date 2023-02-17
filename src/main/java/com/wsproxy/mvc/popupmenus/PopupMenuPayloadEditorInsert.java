package com.wsproxy.mvc.popupmenus;

import com.wsproxy.configuration.ApplicationConfig;
import com.wsproxy.environment.Environment;
import com.wsproxy.environment.EnvironmentItemType;
import com.wsproxy.environment.EnvironmentVariable;
import com.wsproxy.mvc.model.InteractshModel;
import com.wsproxy.tester.PayloadList;
import com.wsproxy.util.GuiUtils;
import com.wsproxy.util.TestUtil;

import javax.swing.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PopupMenuPayloadEditorInsert extends JPopupMenu {
    private InteractshModel interactshModel;
    public JMenu mnuInsert = null;
    public JMenu mnuEncode = null;
    public JMenu mnuDecode = null;
    public JMenu mnuPayloads = null;
    public JMenu mnuEnvironment = null;

    // insert
    public JMenuItem mnuInsertUUID4 = new JMenuItem("UUID4");
    public JMenuItem mnuInsertRandInt = new JMenuItem("Random string");
    public JMenuItem mnuInsertRandStr = new JMenuItem("Random integer");
    public JMenuItem mnuInsertUnixTimestamp = new JMenuItem("UNIX timestamp");
    public JMenuItem mnuInsertIntshPayload = new JMenuItem("Interactsh payload");

    // encode / decode
    public JMenuItem mnuDecodeBase64 = new JMenuItem("Base64 decode");
    public JMenuItem mnuEncodeBase64 = new JMenuItem("Base64 encode");

    private JTextArea txtTarget;
    private ApplicationConfig applicationConfig = new ApplicationConfig();
    private Environment environment = new Environment();
    public PopupMenuPayloadEditorInsert(JTextArea txtTarget, InteractshModel interactshModel) {
        this.txtTarget = txtTarget;
        this.interactshModel = interactshModel;
        initLayout();
        txtTarget.setComponentPopupMenu(this);
        initEventListeners();
        reload();
    }

    public void reload() {
        environment.loadEnvironment();
        rebuildEnvironmentPopupMenu();
        rebuildPayloadPopupMenu();
    }

    public void initLayout() {
        mnuInsert = new JMenu("Insert");
        mnuEncode = new JMenu("Encode");
        mnuDecode = new JMenu("Decode");
        mnuPayloads = new JMenu("Payloads");
        mnuEnvironment = new JMenu("Environment");
        mnuInsert.add(mnuInsertUUID4);
        mnuInsert.add(mnuInsertRandInt);
        mnuInsert.add(mnuInsertRandStr);
        mnuInsert.add(mnuInsertUnixTimestamp);
        mnuInsert.add(mnuInsertIntshPayload);
        mnuEncode.add(mnuEncodeBase64);
        mnuDecode.add(mnuDecodeBase64);

        add(mnuInsert);
        add(mnuEncode);
        add(mnuDecode);
        add(mnuPayloads);
        add(mnuEnvironment);

    }

    public void initEventListeners() {
        mnuInsertRandInt.addActionListener( actionEvent -> {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
            StringBuilder sb = new StringBuilder();
            for ( int i = 0; i < 10; i++ ) {
                int j = ThreadLocalRandom.current().nextInt(0, chars.length()-1);
                sb.append(chars.charAt(j));
            }
            byte buff[] = sb.toString().getBytes(StandardCharsets.UTF_8);
            GuiUtils.insertIntoTextArea(txtTarget, buff);
        });

        mnuInsertUUID4.addActionListener( actionEvent -> {
            byte buff[] = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
            GuiUtils.insertIntoTextArea(txtTarget,buff);
        });

        mnuInsertUnixTimestamp.addActionListener( actionEvent -> {
            byte buff[] = String.format("%d", System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8);
            GuiUtils.insertIntoTextArea(txtTarget,buff);
        });

        mnuInsertRandStr.addActionListener( actionEvent -> {
            byte buff[] = String.format("%d", ThreadLocalRandom.current().nextInt(100000, 999999)).getBytes(StandardCharsets.UTF_8);
            GuiUtils.insertIntoTextArea(txtTarget,buff);
        });

        mnuInsertIntshPayload.addActionListener( actionEvent -> {
            byte buff[] = String.format("%s", interactshModel.getHostPayload()).getBytes(StandardCharsets.UTF_8);
            GuiUtils.insertIntoTextArea(txtTarget,buff);
        });

        mnuEncodeBase64.addActionListener( actionEvent -> {
            String selectedText = txtTarget.getSelectedText();
            if ( selectedText != null ) {
                byte buff[] = Base64.getEncoder().encode(selectedText.getBytes(StandardCharsets.UTF_8));
                GuiUtils.insertIntoTextArea(txtTarget,buff);
            }
        });

        mnuDecodeBase64.addActionListener( actionEvent -> {
            String selectedText = txtTarget.getSelectedText();
            if ( selectedText != null ) {
                try {
                    byte buff[] = Base64.getDecoder().decode(selectedText.getBytes(StandardCharsets.UTF_8));
                    GuiUtils.insertIntoTextArea(txtTarget, buff);
                } catch ( IllegalArgumentException e ) {

                }
            }
        });
    }
    public void rebuildEnvironmentPopupMenu() {
        environment.loadEnvironment();
        mnuEnvironment.removeAll();
        for (EnvironmentVariable envVar : environment.getEnvironmentVariables() ) {
            if ( !envVar.isEnabled() ) {
                continue;
            }
            if ( envVar.getEnvironmentItemType().equals(EnvironmentItemType.BUILTIN)) {
                JMenuItem mnuEnvVar = new JMenuItem(envVar.getName());
                mnuEnvironment.add(mnuEnvVar);
                mnuEnvVar.addActionListener(actionEvent -> {
                    byte[] payloadBytes = envVar.getName().getBytes();
                    GuiUtils.insertIntoTextArea(
                            txtTarget, payloadBytes);
                });
            }
        }
    }

    public void rebuildPayloadPopupMenu() {
        mnuPayloads.removeAll();
        ArrayList<PayloadList> payloadLibrary = TestUtil.reloadPayloadLibrary( null, applicationConfig.getConfigDirPath() );
        // List the first 10 payloads from all payloads in the payload library
        for ( PayloadList payloadList : payloadLibrary ) {
            JMenu mnuListName = new JMenu(payloadList.getPayloadListName());
            if ( !payloadList.isEnabled() ) {
                continue;
            }
            for ( String payload : payloadList.getPayloadsSample(10)) {
                JMenuItem mnuPayload = new JMenuItem(payload);
                mnuPayload.addActionListener(actionEvent -> {
                    byte[] payloadBytes = environment.process(mnuPayload.getText().getBytes());
                    GuiUtils.insertIntoTextArea(txtTarget, payloadBytes);
                });
                mnuListName.add(mnuPayload);
            }
            mnuPayloads.add(mnuListName);
        }
    }
}
