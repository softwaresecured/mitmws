package com.wsproxy.client;

import com.wsproxy.configuration.ApplicationConfig;
import com.wsproxy.conversations.HttpConversation;
import com.wsproxy.httpproxy.*;
import com.wsproxy.logging.AppLog;
import com.wsproxy.network.WsProxySocketFactory;
import com.wsproxy.pki.BouncyCastleSSLProvider;
import com.wsproxy.pki.PKIProvider;
import com.wsproxy.pki.PKIProviderException;
import org.python.antlr.ast.Str;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

/*
    Speaks HTTP to servers
 */
public class HttpClient {
    private Logger LOGGER = AppLog.getLogger(HttpClient.class.getName());
    private PKIProvider pkiProvider = null;
    private Socket socket = null;
    private ApplicationConfig applicationConfig = new ApplicationConfig();
    public HttpClient() {
        pkiProvider = new BouncyCastleSSLProvider();
    }

    /*
        Returns the socket used to connect to the server
     */
    public Socket getSocket() {
        return socket;
    }



    public HttpMessage doProxyConnect( HttpMessage msg ) throws HttpClientException {
        HttpMessage response = null;
        if ( isConnected() ) {
            URL remoteUrl = msg.getHttpUrl();
            if ( remoteUrl != null ) {
                int port = HttpUtil.getPort(remoteUrl);
                String host = remoteUrl.getHost();
                if ( host != null && port > 0 ) {
                    HttpMessage connectMsg = new HttpMessage();
                    String connectReqStr = String.format("CONNECT %s:%s HTTP/1.1\r\n\r\n", host,port);
                    try {
                        connectMsg.fromBytes(connectReqStr.getBytes());
                        response = sendMessage(connectMsg,HttpMessageFormat.DIRECT);
                    } catch (HttpMessageParseException e) {
                        throw new HttpClientException(e.getMessage());
                    }
                }
            }
        }
        return response;
    }

    private String getMsgHost( HttpMessage msg ) {
        URL remoteUrl = msg.getHttpUrl();
        return remoteUrl.getHost();
    }

    private int getMsgPort( HttpMessage msg ) {
        URL remoteUrl = msg.getHttpUrl();
        return HttpUtil.getPort(remoteUrl);
    }

    public Socket connect(HttpMessage msg, boolean tls) throws HttpClientException {
        // Connect directly to the remote host
        if ( !applicationConfig.upstreamProxyAvailable() ) {
            socket = connectDirect(getMsgHost(msg), getMsgPort(msg), tls);
        }
        // Connect to the proxy
        else {
            prepareProxyConnection(msg);
        }
        return socket;
    }

    public void prepareProxyConnection(HttpMessage msg) throws HttpClientException {
        int port = Integer.parseInt(applicationConfig.getProperty("outbound_proxy.port"));
        String host = applicationConfig.getProperty("outbound_proxy.address");
        if ( host != null && port >= 0 ) {
            // TODO: When we add https proxy support this will need to read from that prop
            // - should it just autodetect?
            socket = connectDirect(host, port, false);
            HttpMessage connectResponse = doProxyConnect(msg);
            if ( connectResponse == null ) {
                throw new HttpClientException("Could not connect to proxy");
            }
            if ( connectResponse.getStatusCode() != 200 ) {
                throw new HttpClientException("Proxy did not return a HTTP 200 response");
            }
            if ( msg.isSslEnabled() ) {
                try {
                    //socket = (SocketpkiProvider.upgradeConnection(socket,getMsgHost(msg),getMsgPort(msg));
                    socket = (Socket) WsProxySocketFactory.upgradeSocketConnection(socket,msg,applicationConfig);
                } catch (NoSuchAlgorithmException e) {
                    throw new HttpClientException(e.getMessage());
                } catch (IOException e) {
                    throw new HttpClientException(e.getMessage());
                } catch (KeyManagementException e) {
                    throw new HttpClientException(e.getMessage());
                }
            }
        }
        else {
            throw new HttpClientException("Could not connect to proxy");
        }
    }

    public Socket connectDirect(String host, int port, boolean tls) throws HttpClientException {
        try {
            if ( tls ) {
                socket = WsProxySocketFactory.getEncryptedSocket(host,port,applicationConfig);
            }
            else {
                socket = WsProxySocketFactory.getPlaintextSocket(host,port,applicationConfig);
            }
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            socket = null;
            throw new HttpClientException("TLS Exception");
        } catch (UnknownHostException e) {
            socket = null;
            throw new HttpClientException("Unknown host");
        } catch (IOException e) {
            socket = null;
            throw new HttpClientException("IO error");
        }
        return socket;
    }

    public boolean isConnected() {
        return socket != null;
    }
    public void disconenct() {
        if ( socket != null ) {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
        socket = null;
    }

    private HttpMessage sendMessage( HttpMessage msg, HttpMessageFormat msgFormat ) throws HttpClientException {
        HttpMessage response = null;
        try {
            HttpConversation.writeHttpMessage(socket, msg, HttpMessagePart.ALL, msgFormat, applicationConfig );
            response = HttpConversation.readHttpMessage(socket, HttpMessagePart.ALL,Integer.parseInt(applicationConfig.getProperty("http.max_header_size")), applicationConfig);

        } catch (IOException e) {
            throw new HttpClientException( e.getMessage());
        } catch (HttpMessageParseException e) {
            throw new HttpClientException( e.getMessage());
        }
        return response;
    }


    public HttpMessage send( HttpMessage msg, boolean tls ) throws HttpClientException {
        socket = connect( msg, tls );
        HttpMessageFormat msgFormat = HttpMessageFormat.DIRECT;
        if ( applicationConfig.upstreamProxyAvailable() && !tls ) {
            msgFormat = HttpMessageFormat.UPSTREAM_HTTP_PROXY;
        }
        return sendMessage(msg,msgFormat);
    }

    public HttpMessage send( HttpMessage msg ) throws HttpClientException {
        return send(msg,msg.isSslEnabled());
    }

    public HttpMessage send( String method, String urlStr, String headers[], String body, boolean block ) {
        HttpMessage response = null;
        if ( headers == null ) {
            headers = new String[0];
        }

        String hostStr = getHostStr(urlStr);
        if ( hostStr != null ) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%s %s HTTP/1.1\r\n", method, urlStr));
            sb.append(String.format("Host: %s\r\n", hostStr));
            if ( body != null ) {
                sb.append(String.format("Content-length: %d\r\n", body.length()));
            }
            for ( String header : headers ) {
                sb.append(String.format("%s\r\n", header));
            }
            sb.append("\r\n");
            if ( body != null ) {
                sb.append(body);
            }
            HttpMessage msg = new HttpMessage();
            try {
                msg.fromBytes(sb.toString().getBytes(StandardCharsets.UTF_8));
                if (!block) {
                    Thread t1 = new Thread(new Runnable() {
                        public void run() {
                            try {
                                send(msg);
                            } catch (HttpClientException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    t1.start();
                }
                else {
                    response = send(msg);
                }
            } catch (HttpMessageParseException e) {
                e.printStackTrace();
            } catch (HttpClientException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    private String getHostStr( String urlStr ) {
        String hostStr = null;
        try {
            URL url = new URL(urlStr);
            if ( url != null ) {
                hostStr = url.getHost();
                if ( url.getPort() > 0 ) {
                    hostStr += String.format(":%d", url.getPort());
                }
            }
        } catch (MalformedURLException e) {
            ;
        }
        return hostStr;
    }

}
