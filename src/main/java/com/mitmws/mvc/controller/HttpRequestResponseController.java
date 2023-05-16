package com.mitmws.mvc.controller;

import com.mitmws.httpproxy.HttpMessage;
import com.mitmws.httpproxy.HttpMessageParseException;
import com.mitmws.integrations.python.Script;
import com.mitmws.integrations.python.ScriptManager;
import com.mitmws.mvc.model.HttpRequestResponseModel;
import com.mitmws.mvc.view.panels.PnlHttpRequestResponse;
import com.mitmws.util.GuiUtils;

import javax.script.ScriptException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    The viewer/editor for HTTP requests
 */
public class HttpRequestResponseController implements PropertyChangeListener {

    private HttpRequestResponseModel httpRequestResponseModel;
    private PnlHttpRequestResponse pnlHttpRequestResponse;
    private ScriptManager scriptManager = new ScriptManager();
    public HttpRequestResponseController(HttpRequestResponseModel httpRequestResponseModel, PnlHttpRequestResponse pnlHttpRequestResponse) {
        this.httpRequestResponseModel = httpRequestResponseModel;
        this.httpRequestResponseModel.addListener(this);
        this.pnlHttpRequestResponse = pnlHttpRequestResponse;
        initEventListeners();
    }

    /*
        Loads the model request into the UI
     */
    public void loadRequest( HttpMessage request ) {


        GuiUtils.setComboBoxItem(pnlHttpRequestResponse.jcmbHttpUpgradeMessageMethod,"GET");
        pnlHttpRequestResponse.jtxtWsUrl.setText("");
        pnlHttpRequestResponse.jtxtHttpUpgradeMessageHeaders.setText("");
        pnlHttpRequestResponse.jchkMessageBody.setSelected(false);
        pnlHttpRequestResponse.jtxtHttpUpgradeMessageBody.setText("");
        pnlHttpRequestResponse.jchkMessageBody.setSelected(false);
        pnlHttpRequestResponse.jtxtHttpUpgradeMessageBody.setText("");
        if ( request != null ) {
            httpRequestResponseModel.setMethod(request.getHttpMethod());
            httpRequestResponseModel.setUrl(request.getUrl());
            httpRequestResponseModel.setHeaders(String.join("\n",request.getHeaders()));
            httpRequestResponseModel.setBody(null);
            if ( request.getBodyBytes() != null ) {
                httpRequestResponseModel.setBody(new String(request.getBodyBytes()));
            }
        }


    }


    public void initEventListeners() {
        pnlHttpRequestResponse.jtxtWsUrl.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                httpRequestResponseModel.setUrlNoEvent(pnlHttpRequestResponse.jtxtWsUrl.getText());
                // update the host header
                String hostHeaderRegex = "^host:\\s(.*?)$";
                Pattern hostHeaderRegexPattern = Pattern.compile(hostHeaderRegex, Pattern.MULTILINE|Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
                String headers = pnlHttpRequestResponse.jtxtHttpUpgradeMessageHeaders.getText();
                String wsUrlStr = pnlHttpRequestResponse.jtxtWsUrl.getText();
                if ( wsUrlStr != null && wsUrlStr.matches("^(?i)https?://.*")) {
                    try {
                        URL wsUrl = new URL(wsUrlStr);
                        String hostStr = wsUrl.getHost();
                        if ( wsUrl.getPort() > 0 ) {
                            hostStr = String.format("%s:%d", wsUrl.getHost(),wsUrl.getPort());
                        }
                        headers = hostHeaderRegexPattern.matcher(headers).replaceAll(String.format("Host: %s", hostStr));
                        httpRequestResponseModel.setHeaders(headers);
                    } catch (MalformedURLException exception) {
                        // TODO highlight error in UI
                        //exception.printStackTrace();
                    }
                }
            }
        });

        pnlHttpRequestResponse.jtxtHttpUpgradeMessageHeaders.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                String hostHeaderRegex = "^host:\\s(.*?)$";
                Pattern hostHeaderRegexPattern = Pattern.compile(hostHeaderRegex, Pattern.MULTILINE|Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
                Matcher m = hostHeaderRegexPattern.matcher(pnlHttpRequestResponse.jtxtHttpUpgradeMessageHeaders.getText());
                if ( m.find() ) {
                    String wsUrlStr = pnlHttpRequestResponse.jtxtWsUrl.getText();
                    httpRequestResponseModel.setUrl(wsUrlStr);
                    String host = m.group(1);
                    if ( wsUrlStr != null && wsUrlStr.matches("^(?i)https?://.*")) {
                        try {
                            URL wsUrl = new URL(wsUrlStr);
                            StringBuilder sb = new StringBuilder();
                            sb.append(String.format("%s://", wsUrl.getProtocol()));
                            sb.append(host);
                            if ( wsUrl.getPath() != null ) {
                                sb.append(wsUrl.getPath());
                            }
                            if ( wsUrl.getQuery() != null ) {
                                sb.append(String.format("?%s", wsUrl.getQuery()));
                            }
                            httpRequestResponseModel.setHeadersNoEvent(pnlHttpRequestResponse.jtxtHttpUpgradeMessageHeaders.getText());
                            httpRequestResponseModel.setUrl(sb.toString());
                        } catch (MalformedURLException exception) {
                            // TODO highlight error in UI
                            //exception.printStackTrace();
                        }
                    }
                }
            }
        });


        // Toggle build from script
        pnlHttpRequestResponse.pnlUpgradeRequestScriptConfig.jchkUpgrade.addActionListener( actionEvent -> {
            // load an output of the upgrade script
            if ( pnlHttpRequestResponse.pnlUpgradeRequestScriptConfig.jchkUpgrade.isSelected() ) {
                httpRequestResponseModel.setSavedRequest();
                httpRequestResponseModel.setUseUpgradeScript(pnlHttpRequestResponse.pnlUpgradeRequestScriptConfig.jchkUpgrade.isSelected());
                httpRequestResponseModel.setUpgradeScriptName(pnlHttpRequestResponse.pnlUpgradeRequestScriptConfig.jcmbUpgradeScripts.getSelectedItem().toString());


            }
            else {
                httpRequestResponseModel.setUseUpgradeScript(false);
                loadRequest(httpRequestResponseModel.getSavedRequest());
            }
        });

        // Select script
        pnlHttpRequestResponse.pnlUpgradeRequestScriptConfig.jcmbUpgradeScripts.addActionListener(actionEvent -> {
            if ( httpRequestResponseModel.isUseUpgradeScript() ) {
                httpRequestResponseModel.setUpgradeScriptName(pnlHttpRequestResponse.pnlUpgradeRequestScriptConfig.jcmbUpgradeScripts.getSelectedItem().toString());

            }
        });

        // Toggle body
        pnlHttpRequestResponse.jchkMessageBody.addActionListener(actionEvent -> {
            pnlHttpRequestResponse.scrollHttpMsgBodyViewer.setVisible(pnlHttpRequestResponse.jchkMessageBody.isSelected());
            pnlHttpRequestResponse.revalidate();
        });
        // Select method
        pnlHttpRequestResponse.jcmbHttpUpgradeMessageMethod.addActionListener( actionEvent -> {
            httpRequestResponseModel.setMethod(pnlHttpRequestResponse.jcmbHttpUpgradeMessageMethod.getSelectedItem().toString());
        });

        pnlHttpRequestResponse.jbtnReloadScripts.addActionListener( actionEvent -> {
            reloadScripts();
        });
    }

    public void reloadScripts() {
        pnlHttpRequestResponse.pnlUpgradeRequestScriptConfig.reloadScripts();
        if ( httpRequestResponseModel.getUpgradeScriptName() != null ) {
            GuiUtils.setComboBoxItem(pnlHttpRequestResponse.pnlUpgradeRequestScriptConfig.jcmbUpgradeScripts,httpRequestResponseModel.getUpgradeScriptName());
        }
    }

    public HttpRequestResponseModel getHttpRequestResponseModel() {
        return httpRequestResponseModel;
    }

    public HttpMessage processUpgradeScript( String selectedScript ) {
        try {
            Script upgradeScript = scriptManager.getScript("upgrade", selectedScript);
            String upgradeRequestStr = (String) upgradeScript.executeFunction("execute");
            if ( upgradeRequestStr != null ) {
                HttpMessage upgradeRequest = new HttpMessage();
                upgradeRequest.fromBytes(upgradeRequestStr.getBytes());
                if ( upgradeRequest != null ) {
                    if ( upgradeRequest.getUrl() != null ) {
                        return upgradeRequest;
                    }
                }
            }
        } catch (IllegalArgumentException | HttpMessageParseException e) {
            e.printStackTrace();
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

        if ( "HttpRequestResponseModel.upgradeScriptName".equals(propertyChangeEvent.getPropertyName())) {
            if (propertyChangeEvent.getNewValue() != null) {
                String selectedScript = (String) pnlHttpRequestResponse.pnlUpgradeRequestScriptConfig.jcmbUpgradeScripts.getSelectedItem();
                HttpMessage upgradeRequest = processUpgradeScript(selectedScript);
                if ( upgradeRequest != null ) {
                    loadRequest(upgradeRequest);
                }
            }
        }
        if ( "HttpRequestResponseModel.url".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() != null ) {
                pnlHttpRequestResponse.jtxtWsUrl.setText((String) propertyChangeEvent.getNewValue());
            }
            else {
                pnlHttpRequestResponse.jtxtWsUrl.setText("");
            }
        }
        if ( "HttpRequestResponseModel.method".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() != null ) {
                GuiUtils.setComboBoxItem(pnlHttpRequestResponse.jcmbHttpUpgradeMessageMethod,(String) propertyChangeEvent.getNewValue());
            }
        }
        if ( "HttpRequestResponseModel.testRunBaseName".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() != null ) {
                pnlHttpRequestResponse.jtxtTestRunName.setText((String) propertyChangeEvent.getNewValue());
            }
            else {
                pnlHttpRequestResponse.jtxtTestRunName.setText("");
            }
        }
        if ( "HttpRequestResponseModel.headers".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() != null ) {
                pnlHttpRequestResponse.jtxtHttpUpgradeMessageHeaders.setText((String) propertyChangeEvent.getNewValue());
            }
            else {
                pnlHttpRequestResponse.jtxtHttpUpgradeMessageHeaders.setText("");
            }
        }
        if ( "HttpRequestResponseModel.body".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() != null ) {
                pnlHttpRequestResponse.jtxtHttpUpgradeMessageBody.setText((String) propertyChangeEvent.getNewValue());
            }
            else {
                pnlHttpRequestResponse.jtxtHttpUpgradeMessageBody.setText("");
            }
        }
        if ( "HttpRequestResponseModel.response".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() != null ) {
                pnlHttpRequestResponse.jtxtHttpUpgradeMessageResponse.setText((String) propertyChangeEvent.getNewValue());
            }
            else {
                pnlHttpRequestResponse.jtxtHttpUpgradeMessageResponse.setText("");
            }
        }
        if ( "HttpRequestResponseModel.requestResponse".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() != null ) {
                pnlHttpRequestResponse.jtxtHttpUpgradeMessageResponse.setText((String) propertyChangeEvent.getNewValue());
            }
            else {
                pnlHttpRequestResponse.jtxtHttpUpgradeMessageResponse.setText("");
            }
        }
        if ( "HttpRequestResponseModel.useUpgradeScript".equals(propertyChangeEvent.getPropertyName())) {
            pnlHttpRequestResponse.pnlUpgradeRequestScriptConfig.jchkUpgrade.setSelected((boolean)propertyChangeEvent.getNewValue());
        }

        if ( "HttpRequestResponseModel.upgradeScriptName".equals(propertyChangeEvent.getPropertyName())) {
            if ( propertyChangeEvent.getNewValue() != null ) {
                GuiUtils.setComboBoxItem(pnlHttpRequestResponse.pnlUpgradeRequestScriptConfig.jcmbUpgradeScripts,(String) propertyChangeEvent.getNewValue());
            }
        }
        if ( "HttpRequestResponseModel.editable".equals(propertyChangeEvent.getPropertyName())) {
            boolean editable = (boolean) propertyChangeEvent.getNewValue();
            pnlHttpRequestResponse.jtxtHttpUpgradeMessageHeaders.setEditable(editable);
            pnlHttpRequestResponse.jtxtHttpUpgradeMessageBody.setEditable(editable);
            pnlHttpRequestResponse.jtxtWsUrl.setEditable(editable);
            pnlHttpRequestResponse.jcmbHttpUpgradeMessageMethod.setEditable(editable);
            pnlHttpRequestResponse.jchkMessageBody.setEnabled(editable);
        }
    }
}
