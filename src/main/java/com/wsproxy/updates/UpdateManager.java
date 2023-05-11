package com.wsproxy.updates;

import com.wsproxy.configuration.ApplicationConfig;
import com.wsproxy.logging.AppLog;
import com.wsproxy.pki.RSACrypto;
import com.wsproxy.util.FileUtils;
import com.wsproxy.util.HashUtils;
import com.wsproxy.util.ManifestUtils;
import com.wsproxy.util.NetUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.logging.Logger;

public class UpdateManager {
    private static Logger LOGGER = AppLog.getLogger(UpdateManager.class.getName());
    private ApplicationConfig applicationConfig = new ApplicationConfig();
    public UpdateManager() {

    }


    /*
        The update base URL can either be from github or a selfhosted URL
        If github:
            - we replace github with raw.githubusercontent.com
            - we add /main/ for the main branch
     */
    public String getUpdateBaseUrl() {
        String url = applicationConfig.getProperty("updates.url");
        if ( url.contains("github.com")) {
            url = url.replaceFirst("github.com","raw.githubusercontent.com");
            url += "/main/";
        }
        return url;
    }

    public void update() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        applyUpdates(getApplicableUpdates(ManifestUtils.manifestPaths));
    }

    public void applyUpdates( HashMap<String,String> updates ) throws IOException, NoSuchAlgorithmException {
        for ( String path : updates.keySet()) {
            String updateUrl = String.format("%s/%s", getUpdateBaseUrl(), path);
            String updateContent = NetUtils.getRemoteUrl(updateUrl);
            String remoteHash = HashUtils.sha256sum(updateContent.getBytes(StandardCharsets.UTF_8));
            if ( updates.get(path).equals(remoteHash)) {
                FileUtils.putFileContent(String.format("%s/%s", applicationConfig.getConfigDirPath(),path),updateContent.getBytes(StandardCharsets.UTF_8));
                LOGGER.info(String.format("Updating %s -> %s", updateUrl, path, updates.get(path)));
            }
            else {
                LOGGER.severe(String.format("Hash failure %s, %s, %s != %s", updateUrl,path,remoteHash,updates.get(path)));
            }
        }
    }

    public HashMap<String,String> getApplicableUpdates(String[] manifestPaths) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        HashMap<String,String> applicableUpdates = new HashMap<>();
        String localManifest = ManifestUtils.generateManifest(applicationConfig.getConfigDirPath(),manifestPaths);
        String updateManifest = ManifestUtils.getRemoteManifest(getUpdateBaseUrl());
        PublicKey publicKey = RSACrypto.decodePublicKeyB64(applicationConfig.getProperty("updates.public_key"));
        if ( ManifestUtils.verifyManifest(publicKey, updateManifest)) {
            HashMap<String,String> updateMap = ManifestUtils.manifestToMap(updateManifest);
            HashMap<String,String> localMap = ManifestUtils.manifestToMap(localManifest);
            for ( String path : updateMap.keySet()) {
                // if the file is not present
                if ( localMap.get(path) == null ) {
                    applicableUpdates.put(path,updateMap.get(path));
                }
                else {
                    // if the file is different
                    if ( !localMap.get(path).equals(updateMap.get(path))) {
                        applicableUpdates.put(path,updateMap.get(path));
                    }
                }
            }
        }
        else {
            LOGGER.severe("Invalid signature on manifest - update aborted.");
        }
        return applicableUpdates;
    }

    /*
        Gets remote content from the repo ( files / manifest etc )
     */
    public String getRemoteContent( String path) throws IOException {
        String remotePath = String.format("%s/%s", getUpdateBaseUrl(),path);
        String content = null;
        content = NetUtils.getRemoteUrl(remotePath);
        return content;
    }

}
