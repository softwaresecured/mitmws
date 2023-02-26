package com.wsproxy.configuration;

import com.wsproxy.pki.PKIUtils;
import com.wsproxy.pki.RSACrypto;
import com.wsproxy.util.GuiUtils;
import com.wsproxy.util.HashUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ApplicationConfig {

    private String confDirName = null;
    private String userHome = null;
    private String configFileName = null;
    private String configDirPath = null;
    private String configFilePath = null;
    private Properties appConfig = null;
    private final String[] configDirectories = new String[] {
            "payloads",
            "pki",
            "scripts",
            "scripts/events",
            "scripts/upgrade",
            "scripts/httpserver",
            "scripts/variables",
            "scripts/rules",
            "scripts/rules/active",
            "scripts/rules/passive"};
    public HashMap<String,ConfigPropertyTypes> configPropertyTypeMap = new HashMap<String,ConfigPropertyTypes>();
    //TODO: Make this static
    public ApplicationConfig() {
        confDirName = ".wsproxy";
        configFileName = "app.properties";
        userHome = System.getProperty("user.home");
        configDirPath = String.format("%s/%s", userHome, confDirName);
        configFilePath = String.format("%s/%s", configDirPath, configFileName);
        initConfigTypeMap();
        createConfDirs();
        loadConfig();
    }

    public Properties getProperties() {
        return appConfig;
    }

    /*
        Creates directories used by the config
     */
    public void createConfDirs() {
        // the root config dir
        File confDir = new File(configDirPath);
        if (!confDir.exists()) {
            confDir.mkdirs();
        }
        // sub directories
        for ( String dir : configDirectories ) {
            File d = new File(String.format("%s/%s", configDirPath,dir));
            if (!d.exists()) {
                d.mkdirs();
            }
        }
    }

    public void setProperty(String key, String value ) throws ApplicationConfigException {
        validateProperty(key,value);
        appConfig.setProperty(key,value);
    }

    public String getProperty(String key ) {
        loadConfig();
        return appConfig.getProperty(key);
    }

    public void loadDefaultConfig() throws IOException {
        setDefaultConfig(appConfig);
        saveAndReload();
    }

    public void validateProperty( String propertyName, String value ) throws ApplicationConfigException {
        if ( configPropertyTypeMap.get(propertyName) == null ) {
            throw new ApplicationConfigException("Invalid property name");
        }
        if ( configPropertyTypeMap.get(propertyName) == null ) {
            throw new ApplicationConfigException("Invalid property name");
        }
        switch ( configPropertyTypeMap.get(propertyName) ) {
            case URL:
                try {
                    URL url = new URL(value);
                } catch (MalformedURLException e) {
                    throw new ApplicationConfigException("Invalid url");
                }

                break;
            case PORT:
            case TIME_MSEC:
            case TIME_SEC:
            case SIZE:
                if ( !value.matches("\\d+")) {
                    throw new ApplicationConfigException("Invalid number");
                }
                break;
            case PUBLIC_KEY:
                if ( value != null && value.length() > 0 ) {
                    try {
                        PublicKey publicKey = RSACrypto.decodePublicKeyB64(value);
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                        throw new ApplicationConfigException("Invalid public key");
                    }
                }
                break;
            case IPV4:
                try {
                    InetAddress.getByName(value);
                } catch (UnknownHostException e) {
                    throw new ApplicationConfigException("Invalid IP address");
                }
                break;
            case BOOLEAN:
                if ( !value.matches("(true|false)")) {
                    throw new ApplicationConfigException("Invalid boolean");
                }
                break;
            case TEST_NAME:
                if ( value.length() > 250 ) {
                    throw new ApplicationConfigException("Test name too long");
                }
                break;
            case REGEX:
                try {
                    Pattern p = Pattern.compile(value);
                }
                catch ( PatternSyntaxException e ) {
                    throw new ApplicationConfigException("Invalid regex");
                }
                break;
            case FSPATH:
                try {
                    Path path = Paths.get(value);
                }
                catch ( InvalidPathException e ) {
                    throw new ApplicationConfigException("Invalid path");
                }
                break;
            case INTERACTSH_TOKEN:
                if ( !value.matches("^(?i)[a-z0-9]+$") && value != null ) {
                    throw new ApplicationConfigException("Invalid interactsh token");
                }
                break;
            case API_KEY:
                if ( !value.matches("(?i)^[a-f0-9]+$")) {
                    throw new ApplicationConfigException("Invalid API key");
                }
                break;
            case NUMBER_CSV:
                if ( !value.matches("^[0-9,\\s]+$") && value != null && value.length() > 0) {
                    throw new ApplicationConfigException("Invalid array of csv numbers");
                }
                break;
        }
    }

    public HashMap<String, ConfigPropertyTypes> getConfigPropertyTypeMap() {
        return configPropertyTypeMap;
    }

    public void initConfigTypeMap() {

        configPropertyTypeMap.put("api.api-key", ConfigPropertyTypes.API_KEY);
        configPropertyTypeMap.put("tests.default_step_delay", ConfigPropertyTypes.TIME_MSEC);
        configPropertyTypeMap.put("tests.default_name", ConfigPropertyTypes.TEST_NAME);
        configPropertyTypeMap.put("tests.autotarget_regex", ConfigPropertyTypes.REGEX);
        configPropertyTypeMap.put("tests.autotarget_regex_xml", ConfigPropertyTypes.REGEX);
        configPropertyTypeMap.put("tests.autotarget_regex_json", ConfigPropertyTypes.REGEX);
        configPropertyTypeMap.put("tests.autotarget_regex_kvp", ConfigPropertyTypes.REGEX);
        configPropertyTypeMap.put("tests.autotarget_regex_urlenckvp", ConfigPropertyTypes.REGEX);
        configPropertyTypeMap.put("http.read_timeout_sec", ConfigPropertyTypes.TIME_SEC);
        configPropertyTypeMap.put("http.expect_timeout_sec", ConfigPropertyTypes.TIME_SEC);
        configPropertyTypeMap.put("http.read_buff_len_b", ConfigPropertyTypes.SIZE);
        configPropertyTypeMap.put("http.max_header_size", ConfigPropertyTypes.SIZE);
        configPropertyTypeMap.put("http.tls_verify", ConfigPropertyTypes.BOOLEAN);
        configPropertyTypeMap.put("http.remove_hsts", ConfigPropertyTypes.BOOLEAN);
        configPropertyTypeMap.put("http.remove_websocket_extensions", ConfigPropertyTypes.BOOLEAN);
        configPropertyTypeMap.put("http.exclude_url_regex", ConfigPropertyTypes.REGEX);
        configPropertyTypeMap.put("inbound_proxy.default_listen_port", ConfigPropertyTypes.PORT);
        configPropertyTypeMap.put("inbound_proxy.default_listen_address", ConfigPropertyTypes.IPV4);
        configPropertyTypeMap.put("util.httpserver.listen_port", ConfigPropertyTypes.PORT);
        configPropertyTypeMap.put("util.httpserver.listen_address", ConfigPropertyTypes.IPV4);
        configPropertyTypeMap.put("outbound_proxy.enabled", ConfigPropertyTypes.BOOLEAN);
        configPropertyTypeMap.put("outbound_proxy.port", ConfigPropertyTypes.PORT);
        configPropertyTypeMap.put("outbound_proxy.address", ConfigPropertyTypes.IPV4);
        configPropertyTypeMap.put("scripts.pythonpath", ConfigPropertyTypes.FSPATH);
        configPropertyTypeMap.put("updates.checkonstartup", ConfigPropertyTypes.BOOLEAN);
        configPropertyTypeMap.put("updates.url", ConfigPropertyTypes.URL);
        configPropertyTypeMap.put("updates.public_key", ConfigPropertyTypes.PUBLIC_KEY);
        configPropertyTypeMap.put("betafeatures.enable-protocoltester", ConfigPropertyTypes.BOOLEAN);
        configPropertyTypeMap.put("betafeatures.enable-analyzer", ConfigPropertyTypes.BOOLEAN);
        configPropertyTypeMap.put("interactsh.serverurl", ConfigPropertyTypes.URL);
        configPropertyTypeMap.put("interactsh.token", ConfigPropertyTypes.INTERACTSH_TOKEN);
        configPropertyTypeMap.put("interactsh.pollinterval", ConfigPropertyTypes.TIME_SEC);
        configPropertyTypeMap.put("interactsh.runonstartup", ConfigPropertyTypes.BOOLEAN);

        configPropertyTypeMap.put("rules.enabled_active", ConfigPropertyTypes.NUMBER_CSV);
        configPropertyTypeMap.put("rules.enabled_passive", ConfigPropertyTypes.NUMBER_CSV);

    }

    public void setDefaultConfig(Properties prop) {
        prop.setProperty("api.api-key", generateAPIKey());
        prop.setProperty("interactsh.serverurl", "");
        prop.setProperty("interactsh.token", "");
        prop.setProperty("interactsh.pollinterval", "10");
        prop.setProperty("interactsh.runonstartup", "true");

        prop.setProperty("logging.perf", "false");

        prop.setProperty("tests.default_step_delay", "100");
        prop.setProperty("tests.default_name", "UNTITLED");
        prop.setProperty("tests.autotarget_regex", "\\w+");


        prop.setProperty("tests.autotarget_regex_xml", "<\\s?([^<>\\s/]{1,255})[^>]*>\\s*([^<]*)</");
        prop.setProperty("tests.autotarget_regex_json", "([^\"{}:,]{1,255})\"?\\s?:\\s?\"?([^\"{}:,]+)");
        prop.setProperty("tests.autotarget_regex_kvp", "([^&=]{1,255})(?:=([^&=]*))?");
        prop.setProperty("tests.autotarget_regex_urlenckvp", "(?:%2C)?([\\w\\._\\-]+)%3A[%2-7A-D\"]*([^%]+)");

        prop.setProperty("http.read_timeout_sec", "2");
        prop.setProperty("http.expect_timeout_sec", "2");
        prop.setProperty("http.read_buff_len_b", "8000");
        prop.setProperty("http.max_header_size", "16000");
        prop.setProperty("http.tls_verify", "false");
        prop.setProperty("http.remove_hsts", "true");
        prop.setProperty("http.remove_websocket_extensions", "true");
        prop.setProperty("http.exclude_url_regex", "(?i)https?://.*\\.(css|js|woff2|jpg|jpeg|png|gif|less)$");

        prop.setProperty("pki.cert_subject_c", "CA");
        prop.setProperty("pki.cert_subject_st", "wsproxy");
        prop.setProperty("pki.cert_subject_l", "wsproxy");
        prop.setProperty("pki.cert_subject_o", "wsproxy");
        prop.setProperty("pki.cert_subject_cn", "wsproxy");

        prop.setProperty("inbound_proxy.default_listen_port", "8989");
        prop.setProperty("inbound_proxy.default_listen_address", "127.0.0.1");
        prop.setProperty("util.httpserver.listen_port", "7878");
        prop.setProperty("util.httpserver.listen_address", "127.0.0.1");

        prop.setProperty("outbound_proxy.enabled", "false");
        prop.setProperty("outbound_proxy.port", "9999");
        prop.setProperty("outbound_proxy.address", "127.0.0.1");
        prop.setProperty("scripts.pythonpath", "/usr/bin/python3");

        prop.setProperty("updates.checkonstartup", "false");
        prop.setProperty("updates.url", "http://localhost:8000/");
        prop.setProperty("updates.public_key", "");

        prop.setProperty("betafeatures.enable-protocoltester", "false");
        prop.setProperty("betafeatures.enable-analyzer", "false");

        prop.setProperty("rules.enabled_active", "");
        prop.setProperty("rules.enabled_passive", "");

    }


    private void loadConfig() {
        try {
            createConfig();
            InputStream input = new FileInputStream(configFilePath);
            appConfig = new Properties();
            appConfig.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void createConfig() throws IOException {
        File f = new File(configFilePath);
        if (!f.exists()) {
            OutputStream output = new FileOutputStream(configFilePath);
            Properties prop = new Properties();
            setDefaultConfig(prop);
            prop.store(output, null);

        }
    }
    /*
            Checks the config to see if there is a valid upstream proxy config
         */
    public boolean upstreamProxyAvailable() {
        if ( appConfig.getProperty("outbound_proxy.enabled") != null ) {
            if ( appConfig.getProperty("outbound_proxy.enabled").equals("true")) {
                if ( appConfig.getProperty("outbound_proxy.port") != null && appConfig.getProperty("outbound_proxy.address") != null ) {
                    return true;
                }
            }
        }
        return false;
    }
    public void saveConfig() throws IOException {
        File f = new File(configFilePath);
        OutputStream output = new FileOutputStream(configFilePath);
        appConfig.store(output, null);
    }

    public void saveAndReload() throws IOException {
        saveConfig();
        loadConfig();
    }

    public String getUserHome() {
        return userHome;
    }

    public String getConfigDirPath() {
        return configDirPath;
    }

    public String getConfigFilePath() {
        return configFilePath;
    }

    public String generateAPIKey() {
        String key = null;
        byte[] keyBytes = new byte[32];
        Random r = new Random();
        r.nextBytes(keyBytes);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            key = GuiUtils.binToHexStr(digest.digest(keyBytes));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return key;
    }

}

