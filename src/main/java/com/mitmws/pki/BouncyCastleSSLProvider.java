package com.mitmws.pki;

import com.mitmws.configuration.ApplicationConfig;
import com.mitmws.logging.PerfLog;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;


import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class BouncyCastleSSLProvider implements PKIProvider {
    private ApplicationConfig appConfig = new ApplicationConfig();
    private KeyStore keyStore = null;
    private String keystoreFileName = null;
    private String default_c = "CA";
    private String default_st = "";
    private String default_l = "";
    private String default_o = "";
    private String default_cn = "mitmws";
    // TODO: Save this to project? Save to config dir?
    private Map<Object, Object> certificateKeyBundleCache = null;
    private static Logger PERFLOGGER = PerfLog.getLogger(BouncyCastleSSLProvider.class.getName());
    public BouncyCastleSSLProvider() {
        default_c = appConfig.getProperty("pki.cert_subject_c");
        default_st = appConfig.getProperty("pki.cert_subject_st");
        default_l = appConfig.getProperty("pki.cert_subject_l");
        default_o = appConfig.getProperty("pki.cert_subject_o");
        default_cn = appConfig.getProperty("pki.cert_subject_cn");
        keystoreFileName = String.format("%s/pki/certdb.p12", appConfig.getConfigDirPath());
        certificateKeyBundleCache = Collections.synchronizedMap(new HashMap<>());
    }
    /*
        Used to create the alias used in the keystore
     */
    public String getHostAlias( String hostname, int port ) {
        return String.format("%s:%d",hostname,port );
    }


    public void saveCachedCertificateKeyBundle( String alias, CertificateKeyBundle bundle) {
        certificateKeyBundleCache.put(alias, bundle);
    }

    public CertificateKeyBundle getCachedCertificateBundle( String alias ) {
        return (CertificateKeyBundle) certificateKeyBundleCache.get(alias);
    }

    /*
        Generates a CA certificate
     */
    public CertificateKeyBundle generateCaCertificate(String c, String st, String l, String o, String cn) throws PKIProviderException {
        CertificateKeyBundle caCertificateKeyBundle = null;
        try {
            KeyPairGenerator keyGen = null;
            keyGen = KeyPairGenerator.getInstance("RSA");
            KeyPair certKeyPair = keyGen.generateKeyPair();
            BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
            Instant validFrom = Instant.now();
            Instant validUntil = validFrom.plus(10 * 360, ChronoUnit.DAYS);

            X500Name issuerName;
            PrivateKey issuerKey;
            issuerName = new X500Name("CN=" + cn);
            issuerKey = certKeyPair.getPrivate();

            JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                    issuerName,
                    serialNumber,
                    Date.from(validFrom), Date.from(validUntil),
                    issuerName, certKeyPair.getPublic());
            builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
            builder.addExtension(Extension.subjectAlternativeName,
                    false,
                    new GeneralNames(new GeneralName(GeneralName.dNSName, default_cn)));
            ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(issuerKey);
            X509CertificateHolder certHolder = builder.build(signer);
            X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certHolder);
            //System.out.println(String.format("Generated CA Certificate: %s", cert.toString()));
            caCertificateKeyBundle = new CertificateKeyBundle("rootca", certKeyPair.getPrivate(), new X509Certificate[]{cert});

        } catch (NoSuchAlgorithmException e) {
            throw new PKIProviderException(e.getMessage());
        } catch (CertIOException e) {
            throw new PKIProviderException(e.getMessage());
        } catch (OperatorCreationException e) {
            throw new PKIProviderException(e.getMessage());
        } catch (CertificateException e) {
            throw new PKIProviderException(e.getMessage());
        }
        return caCertificateKeyBundle;
    }

    @Override
    public void generateCa(String c, String st, String l, String o, String cn) throws PKIProviderException {
        CertificateKeyBundle certificateKeyBundle = generateCaCertificate(c,st,l,o,cn);
        saveKeyStoreEntry(certificateKeyBundle);
        saveKeystore();
    }

    @Override
    public SSLSocket upgradeConnection(Socket socket, String hostname, int port) throws PKIProviderException {
        long startTime = System.currentTimeMillis();
        SSLSocket upgradedSocket = null;
        try {
            CertificateKeyBundle certificateKeyBundle = signCertificate(hostname, port);
            KeyStore ks = certificateKeyBundle.getAsKeyStore();
            KeyManagerFactory kmf = null;
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, PKIUtils.DEFAULT_KEYSTORE_PASSWORD.toCharArray());
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(kmf.getKeyManagers(), null, null);

            SSLSocketFactory fact = context.getSocketFactory();
            upgradedSocket = (SSLSocket)fact.createSocket(socket, null, port, true);
            upgradedSocket.setUseClientMode(false);
            upgradedSocket.startHandshake();

        } catch (NoSuchAlgorithmException e) {
            throw new PKIProviderException(e.getMessage());
        } catch (UnrecoverableKeyException e) {
            throw new PKIProviderException(e.getMessage());
        } catch (KeyStoreException e) {
            throw new PKIProviderException(e.getMessage());
        } catch (IOException e) {
            throw new PKIProviderException(e.getMessage());
        } catch (KeyManagementException e) {
            throw new PKIProviderException(e.getMessage());
        }
        PERFLOGGER.info(String.format("%d msec", System.currentTimeMillis()-startTime));
        return upgradedSocket;
    }
    /*
        Creates a new certificate signed by the CA
     */
    public CertificateKeyBundle signCertificate(String hostname, int port ) throws PKIProviderException {
        String hostAlias = getHostAlias(hostname, port);
        // Try to get from cache first
        CertificateKeyBundle mitmCertificate = getCachedCertificateBundle(hostAlias);
        if ( mitmCertificate == null ) {
            try {
                // Get the ca key material out of the keystore
                CertificateKeyBundle rootCaCertificateKeyBundle = getKeyStoreEntry("rootca");

                // Prepare the certificate
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                KeyPair certKeyPair = keyGen.generateKeyPair();
                X500Name name = new X500Name("CN=" + hostname);
                X500Name issuer = new X500Name(rootCaCertificateKeyBundle.getCertificateChain()[0].getIssuerDN().getName());
                BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
                Instant validFrom = Instant.now();
                Instant validUntil = validFrom.plus(10 * 360, ChronoUnit.DAYS);


                JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                        issuer,
                        serialNumber,
                        Date.from(validFrom), Date.from(validUntil),
                        name, certKeyPair.getPublic());
                builder.addExtension(Extension.subjectAlternativeName, false,
                        new GeneralNames(new GeneralName(GeneralName.dNSName, hostname)));
                ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(rootCaCertificateKeyBundle.getPrivateKey());
                X509CertificateHolder certHolder = builder.build(signer);
                X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certHolder);
                mitmCertificate = new CertificateKeyBundle(hostname, certKeyPair.getPrivate(),new X509Certificate[]{cert, rootCaCertificateKeyBundle.getCertificateChain()[0]});
                saveCachedCertificateKeyBundle(hostAlias, mitmCertificate);
            } catch (NoSuchAlgorithmException e) {
                throw new PKIProviderException(e.getMessage());
            } catch (CertIOException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (OperatorCreationException e) {
                e.printStackTrace();
            }
        }
        return mitmCertificate;
    }

    /*
        Gets a keystore entry by alias, returns a Cert + Key pair
     */
    public CertificateKeyBundle getKeyStoreEntry( String alias ) throws PKIProviderException {
        CertificateKeyBundle certificateKeyBundle = null;
        try {
            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
            if ( certificate != null ) {
                PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, PKIUtils.DEFAULT_KEYSTORE_PASSWORD.toCharArray());
                certificateKeyBundle = new CertificateKeyBundle(alias, privateKey, new X509Certificate[]{certificate});
            }
        } catch (KeyStoreException e) {
            throw new PKIProviderException(e.getMessage());
        } catch (UnrecoverableKeyException e) {
            throw new PKIProviderException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new PKIProviderException(e.getMessage());
        }
        return certificateKeyBundle;
    }

    /*
        Saves a keystore item
     */

    public void saveKeyStoreEntry( CertificateKeyBundle certificateKeyBundle) throws PKIProviderException {
        try {
            keyStore.setKeyEntry(certificateKeyBundle.getAlias(), certificateKeyBundle.getPrivateKey(), PKIUtils.DEFAULT_KEYSTORE_PASSWORD.toCharArray(), certificateKeyBundle.getCertificateChain());
        } catch (KeyStoreException e) {
            throw new PKIProviderException(e.getMessage());
        }
    }

    /*
        Creates the keystore if it doesn't exist
        Adds the root CA if it doesn't exist
     */
    public void initKeystore() throws PKIProviderException {
        try {
            keyStore = KeyStore.getInstance("PKCS12");
            File keystoreFile = new File(keystoreFileName);
            // If the keystore doesn't already exist initialize an empty one
            if ( !keystoreFile.exists()) {
                keyStore.load(null, PKIUtils.DEFAULT_KEYSTORE_PASSWORD.toCharArray());
            }
            // If a keystore exists load it
            else {
                InputStream fileInputStream = new FileInputStream(keystoreFileName);
                keyStore.load(fileInputStream, PKIUtils.DEFAULT_KEYSTORE_PASSWORD.toCharArray());
            }
            X509Certificate rootCa = (X509Certificate) keyStore.getCertificate("rootca");
            if ( rootCa == null ) {
                //System.out.println("Root CA does not exist - generating");
                generateCa( default_c,  default_st,  default_l,  default_o,  "MitmWs Root Certificate Authority ");
                saveKeystore();
            }
        } catch (IOException e) {
            throw new PKIProviderException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new PKIProviderException(e.getMessage());
        } catch (CertificateException e) {
            throw new PKIProviderException(e.getMessage());
        } catch (KeyStoreException e) {
            throw new PKIProviderException(e.getMessage());
        }
    }

    /*
        Saves the keystore to the disk
     */
    public void saveKeystore() throws PKIProviderException {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(keystoreFileName);
            keyStore.store(fileOutputStream, PKIUtils.DEFAULT_KEYSTORE_PASSWORD.toCharArray());
        } catch (FileNotFoundException e) {
            throw new PKIProviderException(e.getMessage());
        } catch (CertificateException e) {
            throw new PKIProviderException(e.getMessage());
        } catch (KeyStoreException e) {
            throw new PKIProviderException(e.getMessage());
        } catch (IOException e) {
            throw new PKIProviderException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new PKIProviderException(e.getMessage());
        }
    }

    @Override
    public void init() throws PKIProviderException {
        initKeystore();
    }
}
