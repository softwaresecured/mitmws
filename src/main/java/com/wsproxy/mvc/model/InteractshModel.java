package com.wsproxy.mvc.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wsproxy.configuration.ApplicationConfig;
import com.wsproxy.jsonobjects.PollModel;
import com.wsproxy.jsonobjects.TldDataModel;
import com.wsproxy.pki.RSACrypto;
import com.wsproxy.util.GuiUtils;
import com.wsproxy.util.NetUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.table.DefaultTableModel;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.MGF1ParameterSpec;
import java.util.*;

public class InteractshModel {
    private DefaultTableModel interactionsTableModel;
    private String intshSecret = null;
    private KeyPair intshKeypair = null;
    private String intshUrl = null;
    private String intshToken = null;
    private String correlationId = null;
    private int pollIntervalMs = 5000;
    private String host = null;
    private ArrayList<TldDataModel> pollData = new ArrayList<TldDataModel>();
    private HashMap<String, String> payloadConversationMap = new HashMap<>();
    ApplicationConfig applicationConfig = new ApplicationConfig();
    private SwingPropertyChangeSupport eventEmitter;
    private boolean testMode = false;

    public InteractshModel() {
        interactionsTableModel = new DefaultTableModel();
        for ( String col: new String[] { "id","Unique ID", "Timestamp","Remote ip", "Protocol","Request"}) {
            interactionsTableModel.addColumn(col);
        }
        int configPollinterval = Integer.parseInt(applicationConfig.getProperty("interactsh.pollinterval"));
        if ( configPollinterval > 0 ) {
            pollIntervalMs = configPollinterval * 1000;
        }
        eventEmitter = new SwingPropertyChangeSupport(this);
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    private void regenerateSecret() {
        intshSecret = UUID.randomUUID().toString();
    }

    private void regenerateKeypair() throws NoSuchAlgorithmException {
        intshKeypair = RSACrypto.generateKeyPair();
    }

    public void reloadConfig() {
        applicationConfig = new ApplicationConfig();
        intshUrl = applicationConfig.getProperty("interactsh.serverurl");
        intshToken = applicationConfig.getProperty("interactsh.token");
        try {
            URL hostUrl = new URL(applicationConfig.getProperty("interactsh.serverurl"));
            host = hostUrl.getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, String> getPayloadConversationMap() {
        return payloadConversationMap;
    }

    public void setPayloadConversationMap(HashMap<String, String> payloadConversationMap) {
        this.payloadConversationMap = payloadConversationMap;
    }

    // Generates a random string of n length for the correlation and payload ids
    // TODO: make compliant with the format interactsh uses https://github.com/rs/xid
    private String getRandomId( int n ) {
        StringBuilder sb = new StringBuilder();
        String idChars = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        for ( int i = 0; i < n; i++ ) {
            sb.append(idChars.charAt(random.nextInt(idChars.length()-1)));
        }
        return sb.toString();
    }

    public String getPayload() {
        if ( testMode ) {
            return String.format("%s%s.%s", "a", "b", "c.d");
        }
        if ( correlationId != null ) {
            return String.format("%s%s.%s", correlationId, getRandomId(13), host);
        }
        return null;
    }

    public void associatePayload ( String payload, String conversationId ) {
        payloadConversationMap.put(payload,conversationId);
    }

    public String getHostPayload() {
        if ( correlationId != null ) {
            return String.format(
                    "%s%s.%s",
                    correlationId,
                    getRandomId(13),
                    applicationConfig.getProperty("interactsh.serverurl").replaceAll("https?://",""));
        }
        return null;
    }
    // register an endpoint
    public void register() throws IOException {
        try {
            regenerateSecret();
            regenerateKeypair();
            String newCorrelation = getRandomId(20);

            String pemFormat = String.format("-----BEGIN RSA PUBLIC KEY-----\n%s\n-----END RSA PUBLIC KEY-----",
                    Base64.getEncoder().encodeToString(intshKeypair.getPublic().getEncoded())
            );
            String requestBody = String.format(
                    "{\"public-key\": \"%s\",\"secret-key\": \"%s\",\"correlation-id\": \"%s\"}",
                    Base64.getEncoder().encodeToString(pemFormat.getBytes(StandardCharsets.UTF_8)),
                    intshSecret,
                    newCorrelation
                    );
            String apiResponse = NetUtils.apiRequest(
                    "POST",
                    intshUrl,
                    "register",
                    null,
                    requestBody,
                    intshToken);
            if ( apiResponse.contains("registration successful")) {
                setCorrelationId(newCorrelation);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    // deregister an endpoint
    public void deregister() throws IOException {
        String requestBody = String.format(
                "{\"correlation-id\": \"%s\", \"secret-key\": \"%s\"}",
                correlationId,
                intshSecret
        );
        String apiResponse = NetUtils.apiRequest(
                "POST",
                intshUrl,
                "deregister",
                null,
                requestBody,
                intshToken);
        if ( apiResponse.contains("deregistration successful")) {
            setCorrelationId(null);
        }
    }

    // decrypt the poll results
    private byte[] decrypt( String aesKey, String message ) {
        byte[] decrypted = null;
        try {
            byte buff[] = Base64.getDecoder().decode(message);
            Cipher cipher = null;
            cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING");
            OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), PSource.PSpecified.DEFAULT);
            cipher.init(Cipher.DECRYPT_MODE, intshKeypair.getPrivate(), oaepParams);
            byte decryptedKey[] = cipher.doFinal(Base64.getDecoder().decode(aesKey));
            if ( decryptedKey != null ) {
                cipher = Cipher.getInstance("AES/CFB/NoPadding");
                byte ivBytes[] = new byte[cipher.getBlockSize()];
                byte encData[] = new byte[buff.length-cipher.getBlockSize()];
                System.arraycopy(buff,0,ivBytes,0,cipher.getBlockSize());
                System.arraycopy(buff,cipher.getBlockSize(),encData,0,encData.length);
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decryptedKey, "AES"), new IvParameterSpec(ivBytes));
                return cipher.doFinal(encData);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return decrypted;
    }

    public void poll() throws IOException {
        String apiResponse = NetUtils.apiRequest(
                "GET",
                intshUrl,
                "poll",
                String.format("id=%s&secret=%s", correlationId,intshSecret,intshToken),
                null,
                intshToken);
        ObjectMapper mapper = new ObjectMapper();
        PollModel pollModel = mapper.readValue(apiResponse,PollModel.class);
        if ( pollModel.getData() != null ) {
            for ( String data : pollModel.getData() ) {
                byte decdata[] = decrypt(pollModel.getAesKey(),data);
                if ( decdata != null ) {
                    TldDataModel tldDataModel = mapper.readValue(new String(decdata),TldDataModel.class);
                    addInteraction(tldDataModel);
                }
            }
        }
        return;
    }

    public void addInteraction(TldDataModel tldDataModel) {
        interactionsTableModel.addRow(new Object[] {
                tldDataModel.getId(),
                tldDataModel.getUniqueId(),
                tldDataModel.getTimeStamp(),
                tldDataModel.getRemoteAddress(),
                tldDataModel.getProtocol(),
                GuiUtils.getSnippet(GuiUtils.getBinPreviewStr(tldDataModel.getRawRequest().getBytes(StandardCharsets.UTF_8)),500)
        });
        pollData.add(tldDataModel);
        eventEmitter.firePropertyChange("InteractshModel.interaction", null, tldDataModel.getUniqueId());
    }

    public String getIntshUrl() {
        return intshUrl;
    }

    public void setIntshUrl(String intshUrl) {
        this.intshUrl = intshUrl;
        eventEmitter.firePropertyChange("InteractshModel.intshUrl", null, intshUrl);
    }

    public String getIntshToken() {
        return intshToken;
    }

    public void setIntshToken(String intshToken) {
        this.intshToken = intshToken;
        eventEmitter.firePropertyChange("InteractshModel.intshToken", null, intshToken);
    }

    public int getPollIntervalMs() {
        return pollIntervalMs;
    }

    public void setPollIntervalMs(int pollIntervalMs) {
        this.pollIntervalMs = pollIntervalMs;
        eventEmitter.firePropertyChange("InteractshModel.correlationId", null, correlationId);
    }

    public DefaultTableModel getInteractionsTableModel() {
        return interactionsTableModel;
    }

    public void setInteractionsTableModel(DefaultTableModel interactionsTableModel) {
        this.interactionsTableModel = interactionsTableModel;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        eventEmitter.firePropertyChange("InteractshModel.correlationId", null, correlationId);
    }

    public ArrayList<TldDataModel> getPollData() {
        return pollData;
    }

    public void setPollData(ArrayList<TldDataModel> pollData) {
        this.pollData = pollData;
    }

    public void addListener(PropertyChangeListener listener ) {
        eventEmitter.addPropertyChangeListener(listener);
    }
}
