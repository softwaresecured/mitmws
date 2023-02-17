package com.wsproxy.pki;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.Socket;


/*
    The hostId is hostname:port /C=/ST=/L=/O=/CN=WsProxyRootCa
    We only need port to keep track of the lables
 */
public interface PKIProvider {
    void generateCa( String c, String st, String l, String o, String cn ) throws PKIProviderException;
    /*
    void importCaPKCS12( String filepath ) throws PKIException;
    void importHostPKCS12( String filepath ) throws PKIException;
    void removeHostCertificate(String hostId ) throws PKIException;
     */
    SSLSocket upgradeConnection (Socket socket, String hostname, int port ) throws PKIProviderException;
    void init() throws PKIProviderException, IOException;
}
