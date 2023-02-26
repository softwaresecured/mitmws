package com.wsproxy;
import com.wsproxy.configuration.ApplicationConfig;
import com.wsproxy.environment.Environment;
import com.wsproxy.httpproxy.HttpProxy;
import com.wsproxy.httpproxy.trafficlogger.TrafficLogger;
import com.wsproxy.logging.AppLog;
import com.wsproxy.mvc.WsProxyGui;
import com.wsproxy.mvc.model.BreakpointModel;
import com.wsproxy.mvc.thread.TrafficLogQueueProcessorThread;
import com.wsproxy.mvc.view.WsProxyHeadless;
import com.wsproxy.pki.RSACrypto;
import com.wsproxy.projects.ProjectDataService;
import com.wsproxy.projects.ProjectDataServiceException;
import com.wsproxy.updates.UpdateManager;
import com.wsproxy.util.FileUtils;
import com.wsproxy.util.ManifestUtils;
import com.wsproxy.version.VERSION;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Logger;
public class Main {
    private static ApplicationConfig applicationConfig = new ApplicationConfig();
    private static Logger LOGGER = AppLog.getLogger(Main.class.getName());
    public static void generateKeyPair( String keyPairFileName ) {
        try {
            if ( keyPairFileName == null ) {
                Scanner stdin = new Scanner(System.in);
                System.out.println("Keypair name: ");
                keyPairFileName = stdin.nextLine();
            }

            if ( keyPairFileName != null ) {
                System.out.println("Generating key pair");
                KeyPair kp = RSACrypto.generateKeyPair();
                String privb64 = new String(Base64.getEncoder().encode(kp.getPrivate().getEncoded()));
                String pubb64 = new String(Base64.getEncoder().encode(kp.getPublic().getEncoded()));
                FileUtils.putFileContent(String.format("%s.public", keyPairFileName),pubb64.getBytes(StandardCharsets.UTF_8));
                FileUtils.putFileContent(String.format("%s.private", keyPairFileName),privb64.getBytes(StandardCharsets.UTF_8));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateUpdateManifest(String privateKeyFile, String localPath ) {
        PrivateKey privateKey;
        String privateKeyb64;


        try {
            if (privateKeyFile != null ) {
                privateKeyb64 = new String(FileUtils.getFileContent(privateKeyFile));
                privateKey = RSACrypto.decodePrivateKeyB64(privateKeyb64);
                String manifestFile = ManifestUtils.generateUpdateManifest(privateKey,localPath, ManifestUtils.manifestPaths);
                FileUtils.putFileContent(String.format("%s/manifest.txt", localPath), manifestFile.getBytes(StandardCharsets.UTF_8));
                System.out.println(manifestFile);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public static void checkRemote(String remoteUrl ) {
        try {
            System.out.println(String.format("Checking manifest for server @ %s", remoteUrl));
            PublicKey publicKey = RSACrypto.decodePublicKeyB64(applicationConfig.getProperty("updates.public_key"));
            String updateManifest = ManifestUtils.getRemoteManifest(remoteUrl);
            if ( ManifestUtils.verifyManifest(publicKey,updateManifest) ) {
                System.out.println(updateManifest);
                System.out.println(String.format("VALID: %s", remoteUrl));
            }
        } catch (IOException e) {
            System.out.println(String.format("ERROR: Could connect to remote URL %s", remoteUrl));
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            System.out.println(String.format("ERROR: Manifest verification failed for remote URL %s", remoteUrl));
            e.printStackTrace();
        }
    }

    public static void update() {
        UpdateManager updateManager = new UpdateManager();
        try {
            updateManager.update();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public static void checkUpdate() {
        UpdateManager updateManager = new UpdateManager();
        try {
            HashMap<String,String> updates = updateManager.getApplicableUpdates(ManifestUtils.manifestPaths);
            for ( String path : updates.keySet() ) {
                System.out.println(path);
            }
            System.out.printf("%d updates available\n", updates.size());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public static void startGui() {
        LOGGER.info(String.format("WsProxy %s starting", VERSION.getVersionStr()));
        try {
            WsProxyGui wsProxyGui = new WsProxyGui();
            wsProxyGui.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ProjectDataServiceException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws UnknownHostException, InterruptedException {
        // Clear temp vars from previous run
        Environment environment = new Environment();
        environment.clearTemp();

        if ( args.length > 0 ) {
            if ( args[0].equals("--generate-keypair")) {
                String kpName = null;
                if ( args.length > 1 ) {
                    kpName = args[1];
                }
                generateKeyPair(kpName);
            } else if ( args.length > 2 && args[0].equals("--create-manifest")) {
                String signingKey = signingKey = args[1];
                String localPath = args[2];

                generateUpdateManifest(signingKey,localPath);
            } else if ( args.length > 1 && args[0].equals("--verify-manifest")) {
                checkRemote(args[1]);
            } else if ( args[0].equals("--update")) {
                update();
            } else if ( args[0].equals("--check")) {
                checkUpdate();
            }
            else if ( args[0].equals("--headless")) {
                String projectFile = null;
                try {
                    projectFile = File.createTempFile("wsproxy",".wspdb").getPath();
                    if ( args.length == 2 ) {
                        projectFile = args[1];
                    }
                    WsProxyHeadless wsProxyHeadless = new WsProxyHeadless(projectFile);
                    wsProxyHeadless.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
            else {
                displayStartupError("Unknown argument", -1);
            }
        }
        else {
            startGui();
        }
        System.exit(0);
    }

    public static void displayStartupError(String message, int status) {
        System.out.println(message);
        System.exit(status);
    }
}

