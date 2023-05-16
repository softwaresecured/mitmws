package com.mitmws.pki;

import sun.security.provider.X509Factory;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;

public final class PKIUtils {
    public static String DEFAULT_KEYSTORE_PASSWORD = "changeit";
    public static TrustManager[] getAllTrusting() {
        return new TrustManager[]{new X509ExtendedTrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) {

            }

            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) {

            }
        }};
    }
    public static String getCertificatePEM(X509Certificate[] certificates) {
        StringBuilder sb = new StringBuilder();
        try {
            for (X509Certificate certificate : certificates) {
                sb.append(X509Factory.BEGIN_CERT + "\n");
                sb.append(new String(Base64.getEncoder().encode(certificate.getEncoded())));
                sb.append("\n" + X509Factory.END_CERT);

            }
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }}
