package com.mitmws.util;

import com.mitmws.configuration.ApplicationConfig;
import com.mitmws.pki.RSACrypto;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;

public final class ManifestUtils {
    public static String[] manifestPaths = new String[]{ "scripts/rules/active", "scripts/rules/passive", "payloads" };
    public static ArrayList<String> getApplicableUpdates( String manifestFile ) {
        return new ArrayList<>();
    }

    public static HashMap<String,String> manifestToMap(String manifest ) {
        HashMap<String,String> map = new HashMap<>();
        String[] lines = manifest.split("\n");
        if ( lines.length > 1 ) {
            for ( int i = 1; i < lines.length; i++ ) {
                String[] lineParts = lines[i].split(",");
                if ( lineParts.length == 2 ) {
                    map.put(lineParts[0],lineParts[1]);
                }
            }
        }
        return map;
    }

    public static String generateManifest(String basePath, String[] manifestPaths) throws IOException, NoSuchAlgorithmException {
        StringBuilder sb = new StringBuilder();
        ApplicationConfig applicationConfig = new ApplicationConfig();
        for ( String manifestPath : manifestPaths ) {
            String manifestFiles = String.format("%s/%s",basePath, manifestPath );
            File[] files = new File(manifestFiles).listFiles();
            if ( files != null ) {
                for (File file : files) {
                    if (file.isFile()) {
                        String filePath = String.format("%s/%s", manifestPath, file.getName());
                        String hashb64 = HashUtils.sha256sumFile(String.format("%s/%s", basePath, filePath));
                        sb.append(String.format("%s,%s\n", filePath, hashb64));
                    }
                }
            }
        }
        return String.format("# MitmWs Update Manifest\n%s",sb);
    }

    public static String generateUpdateManifest(PrivateKey privateKey, String basePath, String[] manifestPaths) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, IOException {
        String localManifest = generateManifest(basePath, manifestPaths);
        String updateSig = RSACrypto.sign(privateKey,localManifest.getBytes(StandardCharsets.UTF_8));
        return String.format("%s\n%s", updateSig,localManifest);

    }

    public static boolean verifyManifest(PublicKey publicKey, String updateManifest) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        String[] lines = updateManifest.split("\n");
        if ( lines.length > 1 ) {
            String sig = lines[0];
            StringBuilder sb = new StringBuilder();
            for ( int i = 1; i < lines.length; i++ ) {
                sb.append(String.format("%s\n", lines[i]));
            }
            return RSACrypto.verify(publicKey,sig,sb.toString().getBytes(StandardCharsets.UTF_8));
        }
        return false;
    }

    public static String getRemoteManifest( String url ) throws IOException {
        String manifestUrl = String.format("%s/manifest.txt", url);
        return NetUtils.getRemoteUrl(manifestUrl);
    }
}
