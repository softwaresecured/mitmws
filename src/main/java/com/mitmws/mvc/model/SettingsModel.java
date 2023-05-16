package com.mitmws.mvc.model;

import com.mitmws.configuration.ApplicationConfig;

import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.table.DefaultTableModel;
import java.beans.PropertyChangeListener;

public class SettingsModel {
    private String regexProperties[] = {"http.exclude_urls"};
    private String stringProperties[] = {"pki.cert_subject_c","pki.cert_subject_st","pki.cert_subject_l","pki.cert_subject_o","pki.cert_subject_cn","scripts.pythonpath","updates.url","updates.public_key"};
    private String booleanProperties[] = {"http.tls_verify","http.remove_hsts","http.remove_websocket_extensions","outbound_proxy.enabled"};
    private String integerProperties[] = {"http.read_timeout_sec","http.expect_timeout_sec","http.read_buff_len_b","http.max_header_size"};
    private String portProperties[] = {"outbound_proxy.port","inbound_proxy.default_listen_port" };
    private String localInterfaceProperties[] = {"outbound_proxy.address"};
    private String remoteIpAddressProperties[] = {"outbound_proxy.address"};

    private DefaultTableModel settingsTableModel;
    private ApplicationConfig applicationConfig;
    private SwingPropertyChangeSupport eventEmitter;
    public SettingsModel() {
        applicationConfig = new ApplicationConfig();
        settingsTableModel = new DefaultTableModel(null,new String[] { "valid", "Property", "Value", "Description"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return Boolean.class;
                    default:
                        return   String.class;
                }
            }
        };
        eventEmitter = new SwingPropertyChangeSupport(this);
    }

    public String getPropertyDescription( String propertyName ) {
        String desc = null;
        if ( propertyName != null ) {
            switch ( propertyName ) {
                case "http.read_timeout_sec":
                    desc = "The read timeout for an HTTP connection";
                    break;
                case "http.expect_timeout_sec":
                    desc = "The amount of time to wait for an expect response";
                    break;
                case "http.read_buff_len_b":
                    desc = "The size of the http read buffer";
                    break;
                case "http.max_header_size":
                    desc = "The amount of bytes a header can be";
                    break;
                case "http.tls_verify":
                    desc = "Verify the remote server's TLS certificate";
                    break;
                case "http.remove_hsts":
                    desc = "Strip the hsts header from responses";
                    break;
                case "http.remove_websocket_extensions":
                    desc = "Strip the websocket extensions header from incoming requests";
                    break;
                case "http.exclude_urls":
                    desc = "Exclude urls from the proxy";
                    break;
                case "pki.cert_subject_c":
                    break;
                case "pki.cert_subject_st":
                    break;
                case "pki.cert_subject_l":
                    break;
                case "pki.cert_subject_o":
                    break;
                case "pki.cert_subject_cn":
                    break;
                case "inbound_proxy.default_listen_port":
                    desc = "Proxy listen port";
                    break;
                case "inbound_proxy.default_listen_address":
                    desc = "Proxy listen interface";
                    break;
                case "outbound_proxy.enabled":
                    desc = "Use an upstream proxy";
                    break;
                case "outbound_proxy.port":
                    desc = "The port of the outbound proxy used by the proxy and testers";
                    break;
                case "outbound_proxy.address":
                    desc = "The host or ip of the outbound proxy used by the proxy and testers";
                    break;
                case "scripts.pythonpath":
                    desc = "The location of the python environment to use";
                    break;
                case "updates.url":
                    desc = "The URL of the update repository";
                    break;
                case "updates.public_key":
                    desc = "The public key of the update repository";
                    break;

                case "interactsh.serverurl":
                    desc = "The URL of the interactsh server";
                    break;
                case "interactsh.token":
                    desc = "The auth token for the interactsh server";
                    break;
                case "interactsh.pollinterval":
                    desc = "The amount of time in seconds between interactsh poll requests";
                    break;
                case "interactsh.runonstartup":
                    desc = "Start the interactsh detector on startup";
                    break;
            }
        }
        return desc;

    }


    public String[] getRegexProperties() {
        return regexProperties;
    }

    public String[] getStringProperties() {
        return stringProperties;
    }

    public String[] getBooleanProperties() {
        return booleanProperties;
    }

    public String[] getIntegerProperties() {
        return integerProperties;
    }

    public String[] getPortProperties() {
        return portProperties;
    }

    public String[] getLocalInterfaceProperties() {
        return localInterfaceProperties;
    }

    public String[] getRemoteIpAddressProperties() {
        return remoteIpAddressProperties;
    }

    public DefaultTableModel getSettingsTableModel() {
        return settingsTableModel;
    }

    public ApplicationConfig getApplicationConfig() {
        return applicationConfig;
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
