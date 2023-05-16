package com.mitmws.network;

import com.mitmws.configuration.ApplicationConfig;
import com.mitmws.httpproxy.HttpMessage;
import com.mitmws.httpproxy.HttpUtil;
import com.mitmws.pki.PKIUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class MitmWsSocketFactory {

    public static Socket getPlaintextSocket( String host, int port, ApplicationConfig applicationConfig ) throws IOException {
        return new Socket(host, port);
    }

    public static Socket getEncryptedSocket( String host, int port, ApplicationConfig applicationConfig ) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        Socket socket = null;
        SSLContext sc = SSLContext.getInstance("TLS");
        TrustManager[] tm = null;
        if ( applicationConfig.getProperty("http.tls_verify").equals("false")) {
            tm = PKIUtils.getAllTrusting();
        }
        sc.init(null, tm, new SecureRandom());
        SSLSocketFactory factory = sc.getSocketFactory();
        SSLSocketFactory.getDefault();
        socket = factory.createSocket(host,port);
        return socket;
    }

    public static SSLSocket upgradeSocketConnection(Socket socket, HttpMessage msg, ApplicationConfig applicationConfig ) throws KeyManagementException, NoSuchAlgorithmException, IOException {
        SSLSocket upgradedSocket = null;
        URL remoteUrl = msg.getHttpUrl();
        if ( remoteUrl != null ) {
            int port = HttpUtil.getPort(remoteUrl);
            String host = remoteUrl.getHost();
            SSLContext sc = SSLContext.getInstance("TLS");
            TrustManager[] tm = null;
            if ( applicationConfig.getProperty("http.tls_verify").equals("false")) {
                tm = PKIUtils.getAllTrusting();
            }
            sc.init(null, tm, new SecureRandom());
            SSLSocketFactory fact = sc.getSocketFactory();
            upgradedSocket = (SSLSocket)fact.createSocket(socket, host,port, true);
            upgradedSocket.setUseClientMode(true);
            upgradedSocket.startHandshake();
        }

        return upgradedSocket;
    }
}
