package com.wsproxy.mvc.view.panels;

import com.wsproxy.mvc.model.HttpRequestResponseModel;
import com.wsproxy.mvc.view.panels.manualtester.PnlUpgradeRequestScriptConfig;
import com.wsproxy.util.TestUtil;

import javax.swing.*;
import java.awt.*;

public class PnlHttpRequestResponse extends JPanel {

    public JLabel lblConnectionStatus = new JLabel("[Not connected]");
    public JTextField jtxtWsUrl = null;
    public JTextField jtxtTestRunName = null;
    public JComboBox jcmbHttpUpgradeMessageMethod = null;
    public JTextArea jtxtHttpUpgradeMessageHeaders = null;
    public JTextArea jtxtHttpUpgradeMessageBody = null;
    public JTextArea jtxtHttpUpgradeMessageResponse = null;
    private JPanel pnlHttpUpgradeMessageResponse = null;
    private JPanel pnlHttpUpgradeMessageRequest = null;
    public JButton jbtnRunTest = null;
    private JPanel pnlTarget = null;
    public JCheckBox jchkMessageBody = null;
    public PnlUpgradeRequestScriptConfig pnlUpgradeRequestScriptConfig;
    public JScrollPane scrollHttpMsgBodyViewer = null;
    private HttpRequestResponseModel httpRequestResponseModel;
    public PnlHttpRequestResponse( HttpRequestResponseModel httpRequestResponseModel ) {
        this.httpRequestResponseModel = httpRequestResponseModel;
        initLayout();
    }

    public void initLayout() {
        pnlUpgradeRequestScriptConfig = new PnlUpgradeRequestScriptConfig();
        pnlHttpUpgradeMessageRequest = new JPanel();
        pnlHttpUpgradeMessageResponse = new JPanel();
        jtxtTestRunName = new JTextField(TestUtil.DEFAULT_TEST_NAME);
        jtxtTestRunName.setFont(new Font(jtxtTestRunName.getText(), Font.BOLD,12));
        jtxtTestRunName.setMinimumSize(new Dimension(100,27));
        jtxtTestRunName.setMaximumSize(new Dimension(100,27));
        jtxtTestRunName.setPreferredSize(new Dimension(100,27));

        jtxtWsUrl = new JTextField(TestUtil.DEFAULT_TARGET_URL);
        jcmbHttpUpgradeMessageMethod = new JComboBox(httpRequestResponseModel.getMethods());
        jtxtHttpUpgradeMessageHeaders = new JTextArea(TestUtil.DEFAULT_HEADERS);
        jtxtHttpUpgradeMessageBody = new JTextArea();
        jtxtHttpUpgradeMessageResponse = new JTextArea();
        jtxtHttpUpgradeMessageResponse.setEditable(false);
        jbtnRunTest = new JButton("Run");

        jchkMessageBody = new JCheckBox("Request body");
        jchkMessageBody.setSelected(false);


        jtxtHttpUpgradeMessageHeaders.setLineWrap(true);

        scrollHttpMsgBodyViewer = new JScrollPane(jtxtHttpUpgradeMessageBody);
        scrollHttpMsgBodyViewer.setVisible(false);
        scrollHttpMsgBodyViewer.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollHttpMsgBodyViewer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        /*
            Build the layout
         */
        pnlHttpUpgradeMessageRequest.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        pnlHttpUpgradeMessageRequest.add(pnlUpgradeRequestScriptConfig,gbc);

        gbc = new GridBagConstraints();
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 0;
        pnlHttpUpgradeMessageRequest.add(new JLabel("Request headers"),gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        pnlHttpUpgradeMessageRequest.add(jtxtHttpUpgradeMessageHeaders,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1;
        gbc.weighty = 0;
        pnlHttpUpgradeMessageRequest.add(jchkMessageBody,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1;
        gbc.weighty = 1;
        pnlHttpUpgradeMessageRequest.add(scrollHttpMsgBodyViewer,gbc);

        JScrollPane scrollHttpResponse = new JScrollPane(jtxtHttpUpgradeMessageResponse);
        scrollHttpResponse.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollHttpResponse.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        pnlHttpUpgradeMessageResponse.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        pnlHttpUpgradeMessageResponse.add(scrollHttpResponse,gbc);
        // Upgrade message panel
        // Target panel
        pnlTarget = new JPanel();
        pnlTarget.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlTarget.add(jcmbHttpUpgradeMessageMethod,gbc);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        pnlTarget.add(jtxtWsUrl,gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlTarget.add(new JLabel("Test name"),gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlTarget.add(jtxtTestRunName,gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlTarget.add(lblConnectionStatus,gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        pnlTarget.add(jbtnRunTest,gbc);

        JTabbedPane upgradeHttpMsgTabs = new JTabbedPane();
        upgradeHttpMsgTabs.addTab("Request", null, pnlHttpUpgradeMessageRequest);
        upgradeHttpMsgTabs.addTab("Response", null, pnlHttpUpgradeMessageResponse);


        setLayout(new GridBagLayout());
        //setBorder(BorderFactory.createTitledBorder("Upgrade request"));

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(pnlTarget,gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(upgradeHttpMsgTabs,gbc);
    }
}
