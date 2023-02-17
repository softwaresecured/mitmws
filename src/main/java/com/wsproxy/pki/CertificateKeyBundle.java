package com.wsproxy.pki;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class CertificateKeyBundle {
    public PrivateKey privateKey;
    public X509Certificate[] certificateChain;
    public String alias;

    public CertificateKeyBundle(String alias, PrivateKey privateKey, X509Certificate[] certificateChain) {
        this.privateKey = privateKey;
        this.certificateChain = certificateChain;
        this.alias = alias;
    }

    public KeyStore getAsKeyStore() throws PKIProviderException {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, PKIUtils.DEFAULT_KEYSTORE_PASSWORD.toCharArray());
            keyStore.setKeyEntry(getAlias(), getPrivateKey(), PKIUtils.DEFAULT_KEYSTORE_PASSWORD.toCharArray(), getCertificateChain());
        } catch (KeyStoreException e) {
            throw new PKIProviderException(e.getMessage());
        } catch (CertificateException e) {
            throw new PKIProviderException(e.getMessage());
        } catch (IOException e) {
            throw new PKIProviderException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new PKIProviderException(e.getMessage());
        }
        return keyStore;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public X509Certificate[] getCertificateChain() {
        return certificateChain;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < certificateChain.length; i++ ) {
            sb.append(String.format("Certificate %d\n%s\n", i,certificateChain[i].toString()));
        }
        return sb.toString();
    }
}
