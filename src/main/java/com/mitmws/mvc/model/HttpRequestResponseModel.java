package com.mitmws.mvc.model;

import com.mitmws.httpproxy.HttpMessage;
import com.mitmws.httpproxy.HttpMessageParseException;
import com.mitmws.util.TestUtil;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;

public class HttpRequestResponseModel {
    private String url = TestUtil.DEFAULT_TARGET_URL;
    private String testRunBaseName = TestUtil.DEFAULT_TEST_NAME;
    private String method = "GET";
    private String headers = TestUtil.DEFAULT_HEADERS;
    private String body = null;
    private String response = null;
    private boolean useUpgradeScript = false;
    private String upgradeScriptName = null;
    private HttpMessage savedRequest = null;
    private String[] methods = new String[]{ "GET","POST","PUT","PATCH","HEAD","DELETE","OPTIONS"};
    private boolean editable = true;
    private String requestResponse = null;

    private SwingPropertyChangeSupport eventEmitter;
    public HttpRequestResponseModel() {
        eventEmitter = new SwingPropertyChangeSupport(this);
    }

    public String[] getMethods() {
        return methods;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        eventEmitter.firePropertyChange("HttpRequestResponseModel.url", null, this.url);
    }

    public void setUrlNoEvent(String url) {
        this.url = url;
    }

    public String getTestRunBaseName() {
        return testRunBaseName;
    }

    public void setTestRunBaseName(String testRunBaseName) {
        this.testRunBaseName = testRunBaseName;
        eventEmitter.firePropertyChange("HttpRequestResponseModel.testRunBaseName", null, this.testRunBaseName);
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
        eventEmitter.firePropertyChange("HttpRequestResponseModel.method", null, this.method);
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
        eventEmitter.firePropertyChange("HttpRequestResponseModel.headers", null, this.headers);
    }

    public void setHeadersNoEvent(String headers) {
        this.headers = headers;
    }
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
        eventEmitter.firePropertyChange("HttpRequestResponseModel.body", null, this.body);
    }

    public String getRequestResponse() {
        return requestResponse;
    }

    public void setRequestResponse(String requestResponse) {
        this.requestResponse = requestResponse;
        eventEmitter.firePropertyChange("HttpRequestResponseModel.requestResponse", null, this.requestResponse);
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        eventEmitter.firePropertyChange("HttpRequestResponseModel.editable", null, this.editable);
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
        eventEmitter.firePropertyChange("HttpRequestResponseModel.response", null, this.response);
    }

    public boolean isUseUpgradeScript() {
        return useUpgradeScript;
    }

    public void setUseUpgradeScript(boolean useUpgradeScript) {
        this.useUpgradeScript = useUpgradeScript;
        eventEmitter.firePropertyChange("HttpRequestResponseModel.useUpgradeScript", null, this.useUpgradeScript);
    }

    public String getUpgradeScriptName() {
        return upgradeScriptName;
    }

    public void setUpgradeScriptName(String upgradeScriptName) {
        this.upgradeScriptName = upgradeScriptName;
        eventEmitter.firePropertyChange("HttpRequestResponseModel.upgradeScriptName", null, this.upgradeScriptName);
    }

    public HttpMessage getSavedRequest() {
        return savedRequest;
    }
    /*
        Saves the request when toggling scripted upgrade.
     */
    public void setSavedRequest() {
        this.savedRequest = buildHttpMessage();
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }

    public HttpMessage buildHttpMessage() {
        HttpMessage msg = null;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%s %s HTTP/1.1", method,url));
            sb.append("\r\n");
            for ( String curHeader : headers.split("\n")) {
                String cleanHeader = curHeader.trim();
                if ( cleanHeader.length() > 0 ) {
                    sb.append(cleanHeader);
                    sb.append("\r\n");
                }
            }
            sb.append("\r\n");
            if ( body != null ) {
                sb.append(body);
            }
            String httpMessage = sb.toString();
            msg = new HttpMessage();
            msg.fromBytes(httpMessage.getBytes());
            if ( url.toLowerCase().startsWith("https")) {
                msg.setSslEnabled(true);
            }
        } catch (HttpMessageParseException e) {
            e.printStackTrace();
        }
        return msg;
    }
}
