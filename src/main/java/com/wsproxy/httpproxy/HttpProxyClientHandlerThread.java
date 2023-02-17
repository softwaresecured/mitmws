package com.wsproxy.httpproxy;

import com.wsproxy.client.HttpClient;
import com.wsproxy.client.HttpClientException;
import com.wsproxy.configuration.ApplicationConfig;
import com.wsproxy.conversations.HttpConversation;
import com.wsproxy.httpproxy.trafficlogger.TrafficLogger;
import com.wsproxy.httpproxy.trafficlogger.TrafficSource;
import com.wsproxy.httpproxy.websocket.WebsocketException;
import com.wsproxy.httpproxy.websocket.WebsocketSession;
import com.wsproxy.logging.AppLog;
import com.wsproxy.logging.PerfLog;
import com.wsproxy.mvc.model.BreakpointModel;
import com.wsproxy.network.WsProxySocketFactory;
import com.wsproxy.pki.BouncyCastleSSLProvider;
import com.wsproxy.pki.PKIProvider;
import com.wsproxy.pki.PKIProviderException;
import com.wsproxy.util.ExtensionUtil;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

//https://www.w3.org/Protocols/rfc2616/rfc2616-sec8.html
public class HttpProxyClientHandlerThread extends Thread {
    private boolean HTTP_PROXY_DEBUG = false;
    private int HTTP_READ_BUFF_LEN = 1024*1024;
    private Socket clientSocket = null;
    private Socket serverSocket = null;
    private String protocolMode = "http";
    private PKIProvider pkiProvider = null;
    private ApplicationConfig appConfig = null;
    private TrafficLogger trafficLogger = null;
    private HostCache hostCache = null;
    private boolean PROXY_CONNECTION_KEEPALIVE=true; // the client can ask for this
    private boolean HOST_CONNECTION_KEEPALIVE=true; // the target's responses determine this
    private WebsocketSession websocketSession = null;
    private Logger LOGGER = AppLog.getLogger(HttpProxyClientHandlerThread.class.getName());
    private Logger PERFLOGGER = PerfLog.getLogger(HttpProxyClientHandlerThread.class.getName());
    private BreakpointModel breakpointModel;
    private ApplicationConfig applicationConfig = new ApplicationConfig();
    public HttpProxyClientHandlerThread(Socket socket, TrafficLogger trafficLogger, HostCache hostCache, BreakpointModel breakpointModel) {
        this.trafficLogger = trafficLogger;
        this.hostCache = hostCache;
        this.breakpointModel = breakpointModel;
        clientSocket = socket;
        try {
            clientSocket.setTcpNoDelay(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        appConfig = new ApplicationConfig();

    }

    public String getWebsocketSessionId() {
        String sess = null;
        if ( websocketSession != null ) {
            sess = websocketSession.getUpgradeMsgUUID();
        }
        return sess;
    }

    public WebsocketSession getWebsocketSession() {
        return websocketSession;
    }
    public void connectRemote (String host, int port, boolean ssl ) throws IOException, NoSuchAlgorithmException, KeyManagementException, HttpMessageParseException, HttpClientException {
        // connect using upstream proxy
        if ( applicationConfig.upstreamProxyAvailable() ) {
            // Build out enough to make the client make a connect request
            String request = String.format("GET %s://%s:%d HTTP/1.1\r\nHost: %s\r\n\r\n", ssl ? "https" : "http",host,port,host);
            HttpMessage msg = new HttpMessage();
            msg.fromBytes(request.getBytes());
            HttpClient hc = new HttpClient();
            hc.prepareProxyConnection(msg);
            serverSocket = hc.getSocket();

        }
        // Connect direct
        else {
            if ( !ssl ) {
                serverSocket = WsProxySocketFactory.getPlaintextSocket(host,port,applicationConfig);
            }
            else {
                serverSocket = WsProxySocketFactory.getEncryptedSocket(host,port,applicationConfig);
            }
        }

        if ( serverSocket != null ) {
            serverSocket.setSoTimeout(5000);
            serverSocket.setTcpNoDelay(true);
        }
    }

    public HttpMessage proxyHttp ( HttpMessage msg ) throws IOException, HttpMessageParseException {
        long startTime = System.currentTimeMillis();
        HttpMessage resp = null;
        String expectDirective = msg.getHeaderValue("expect");


        HttpConversation.writeHttpMessage(serverSocket,msg,HttpMessagePart.HEADER, HttpMessageFormat.DIRECT, applicationConfig);
        // Write the client's message to the remote host and write the response back to the client
        if ( expectDirective != null ) {
            //System.out.println("Handling expect/continue");
            if ( expectDirective.matches("100-continue")) {
                HttpMessage expectResp = HttpConversation.readHttpMessage(serverSocket, HttpMessagePart.ALL,Integer.parseInt(appConfig.getProperty("http.max_header_size")),applicationConfig);
                if ( expectResp.getStatusCode() == 100 ) {
                    HttpConversation.writeHttpMessage(serverSocket,msg,HttpMessagePart.BODY, HttpMessageFormat.DIRECT, applicationConfig);
                }
            }
        }
        HttpConversation.writeHttpMessage(serverSocket,msg,HttpMessagePart.BODY, HttpMessageFormat.DIRECT, applicationConfig);
        // Read the response
        HttpMessagePart msgPart = HttpMessagePart.ALL;
        if ( msg.getHttpMethod().equalsIgnoreCase("head")) {
            msgPart = HttpMessagePart.HEADER;
        }
        resp = HttpConversation.readHttpMessage(serverSocket,msgPart,Integer.parseInt(appConfig.getProperty("http.max_header_size")),applicationConfig);
        updateHostKeepAlive(resp);
        HttpConversation.writeHttpMessage(clientSocket, resp, HttpMessagePart.ALL, HttpMessageFormat.DIRECT, applicationConfig);
        PERFLOGGER.info(String.format("%d msec, url = %s", System.currentTimeMillis()-startTime,msg.getUrl()));
        return resp;
    }

    public HttpMessage handleProxyConnect ( HttpMessage msg ) throws IOException, PKIProviderException {
        long startTime = System.currentTimeMillis();
        String connectAddress = msg.getConnectAddress();
        String[] addrParts = connectAddress.split(":");
        if ( connectAddress != null ) {
            if ( detectTLS(addrParts[0],Integer.parseInt(addrParts[1])))  {
                msg.setSslEnabled(true);
            }
            try {
                // Connect to the remote host
                connectRemote(addrParts[0],Integer.parseInt(addrParts[1]), msg.isSslEnabled());
                // Return the 200 ok
                sendCustomResponse(200,"Connection Established", "1.1",null);
                //System.out.println("Upgrading connection to %s".format(msg.getHeaderValue("host")));
            } catch (NoSuchAlgorithmException e) {
                throw new PKIProviderException(e.getMessage());
            } catch (KeyManagementException e) {
                throw new PKIProviderException(e.getMessage());
            } catch (HttpMessageParseException e) {
                e.printStackTrace();
            } catch (HttpClientException e) {
                e.printStackTrace();
            }
            if ( msg.isSslEnabled() ) {
                clientSocket = pkiProvider.upgradeConnection(clientSocket,addrParts[0],Integer.parseInt(addrParts[1]));
                clientSocket.setTcpNoDelay(true);
            }
        }
        PERFLOGGER.info(String.format("%d msec", System.currentTimeMillis()-startTime));
        return msg;
    }

    public void teardownHostConnection() {
        if ( serverSocket != null ) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void teardownClientConnection() {
        if ( clientSocket != null ) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void sendCustomResponse(int httpCode, String httpStatus, String protocol, byte[] body) {
        HttpMessage customResponse = new HttpMessage();
        try {
            customResponse.responseFromParams(
                    httpCode,
                    httpStatus,
                    new String[] {
                            String.format("Date: %s",HttpUtil.getHttpDate())
                    },
                    body,
                    protocol,
                    false
            );
            HttpConversation.writeHttpMessage(clientSocket,customResponse,HttpMessagePart.ALL, HttpMessageFormat.DIRECT, applicationConfig);
        } catch (HttpMessageParseException | IOException e) {
            //e.printStackTrace();
        }
    }

    public void updateProxyKeepAlive( HttpMessage msg ) {
        // TODO: CPU issue when enabled :(
        PROXY_CONNECTION_KEEPALIVE = false;
        /*
        if ( msg != null ) {
            String proxyConnectionHeader = msg.getHeaderValue("proxy-connection");
            PROXY_CONNECTION_KEEPALIVE=false;
            if ( proxyConnectionHeader != null ) {
                if ( proxyConnectionHeader.toLowerCase().equals("keep-alive")) {
                    PROXY_CONNECTION_KEEPALIVE=true;
                }
            }
        }
         */

    }

    public void updateHostKeepAlive( HttpMessage msg ) {
        // TODO: CPU issue when enabled :(
        HOST_CONNECTION_KEEPALIVE = false;
        /*
        if ( msg != null ) {
            String proxyConnectionHeader = msg.getHeaderValue("connection");
            HOST_CONNECTION_KEEPALIVE=false;
            if ( proxyConnectionHeader != null ) {
                if ( proxyConnectionHeader.toLowerCase().equals("keep-alive")) {
                    HOST_CONNECTION_KEEPALIVE=true;
                }
            }
        }
         */
    }

    public HttpMessage readClientMessage() throws IOException, HttpMessageParseException {
        HttpMessage msg = null;
        msg = HttpConversation.readHttpMessage(clientSocket, HttpMessagePart.ALL,Integer.parseInt(appConfig.getProperty("http.max_header_size")), applicationConfig);
        return msg;
    }
    /*
        Connect to the remote host to see if it speaks SSL
     */
    public boolean detectTLS( String hostname, int port ) {
        boolean isTls = false;
        HostCacheItem cacheCheck = hostCache.get(hostname,port);
        if ( cacheCheck != null ) {
            System.out.println(String.format("CACHE-HIT: detectTls - host cache has %d", hostCache.size()));
            return cacheCheck.isTls();
        }
        else {
            // common https ports
            if( port == 443 || port == 8443 || port == 9443 ) {
                isTls = true;
                hostCache.put(hostname,port,isTls);
            }
            else {
                try {
                    System.out.println(String.format("CACHE-MISS: detectTls - host cache has %d", hostCache.size()));
                    SSLSocket s = (SSLSocket) WsProxySocketFactory.getEncryptedSocket(hostname,port,applicationConfig);
                    s.setSoTimeout(1000);
                    s.startHandshake();
                    s.close();
                    hostCache.put(hostname,port,true);
                    isTls = true;
                } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                    hostCache.put(hostname,port,false);
                }
            }
        }
        return isTls;
    }

    public HttpMessage setupHostConnection( HttpMessage msg ) throws IOException, HttpMessageParseException, PKIProviderException, KeyManagementException, NoSuchAlgorithmException, HttpClientException {
        // direct - could be ssl
        long startTime = System.currentTimeMillis();
        if ( msg.getHttpMethod().equalsIgnoreCase("connect")) {
            HttpMessage preConnect = handleProxyConnect(msg);
            // Read the new message in
            msg = HttpConversation.readHttpMessage(clientSocket, HttpMessagePart.ALL,Integer.parseInt(appConfig.getProperty("http.max_header_size")), applicationConfig);
            msg.setSslEnabled(preConnect.isSslEnabled());
            updateProxyKeepAlive(msg);
            // Strip the proxy headers
            msg.filterHeaders("^(?i)(x-proxy|proxy).*");
        }
        // http
        else {
            URL remoteUrl = msg.getHttpUrl();
            connectRemote(remoteUrl.getHost(),HttpUtil.getPort(remoteUrl), msg.isSslEnabled());
        }
        PERFLOGGER.info(String.format("%d msec", System.currentTimeMillis()-startTime));
        return msg;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        pkiProvider = new BouncyCastleSSLProvider();
        HttpMessage msg = null;
        HttpMessage resp = null;
        // Init the cryto provider
        try {
            pkiProvider.init();
        } catch (PKIProviderException | IOException e) {
            sendCustomResponse(502,"Bad Gateway", "1.1",null);
            LOGGER.severe("Could not initialize crypto provider ");
            e.printStackTrace();
        }

        while ( PROXY_CONNECTION_KEEPALIVE ) {
            try {
                msg = readClientMessage();
            } catch (IOException e ) {
                LOGGER.severe("IO Error reading message from client");
                sendCustomResponse(502,"Bad Gateway", "1.1",null);
                e.printStackTrace();
                break;
            } catch (HttpMessageParseException e) {
                LOGGER.severe("Error parsing message from client");
                sendCustomResponse(502,"Bad Gateway", "1.1",null);
                e.printStackTrace();
                break;
            }


            if ( msg != null ) {
                try {
                    msg = setupHostConnection(msg);
                } catch (IOException e ) {
                    LOGGER.severe(String.format("IO Error connecting to remote host: %s", e.getMessage()));
                    sendCustomResponse(502,"Bad Gateway", "1.1",null);
                    break;
                } catch ( HttpMessageParseException e ) {
                    LOGGER.severe(String.format("Error parsing message while setting up remote connection: %s", e.getMessage()));
                    sendCustomResponse(502,"Bad Gateway", "1.1",null);
                    break;
                } catch ( PKIProviderException | KeyManagementException | NoSuchAlgorithmException e ) {
                    LOGGER.severe(String.format("Crypto error while setting up remote connection to %s: %s", msg.getConnectAddress(),e.getMessage()));
                    sendCustomResponse(502,"Bad Gateway", "1.1",null);
                    break;
                } catch (HttpClientException e) {
                    e.printStackTrace();
                }

                String proxyReqUrl = "";
                if ( msg != null ) {
                    if ( msg.getUrl() != null ) {
                        proxyReqUrl = msg.getUrl();
                    }
                }

                try {
                    resp = proxyHttp(msg);
                } catch (IOException e) {
                    LOGGER.severe(String.format("IO error while handling proxy request [%s]: %s", e.getMessage(), proxyReqUrl));
                    sendCustomResponse(502,"Bad Gateway", "1.1",null);
                    break;
                } catch ( HttpMessageParseException e) {
                    LOGGER.severe("Error parsing proxy request");
                    sendCustomResponse(502,"Bad Gateway", "1.1",null);
                    break;
                }

                if ( resp != null ) {
                    LOGGER.info(String.format("PROXY-%d%s %d %s %s", getId(), clientSocket.getRemoteSocketAddress().toString(),resp.getStatusCode(), msg.getHttpMethod(), msg.getUrl()));
                    trafficLogger.logRFC2616Message(TrafficSource.PROXY,msg,resp,null);
                    if ( resp.getStatusCode() == 101 ) {
                        String upgradeHeader = resp.getHeaderValue("upgrade");
                        if ( upgradeHeader != null ) {
                            if ( upgradeHeader.equalsIgnoreCase("websocket")) {
                                String upgradeUrl = msg.getUrl();
                                websocketSession = new WebsocketSession(ExtensionUtil.initExtensions(msg,resp),msg.getMessageUUID(),clientSocket,serverSocket,trafficLogger,breakpointModel,upgradeUrl);
                                if ( !trafficLogger.inScope(msg.getUrl())) {
                                    websocketSession.disableLogging();
                                }
                                try {
                                    websocketSession.startSession();
                                } catch (SocketException e) {
                                    break;
                                } catch (WebsocketException e) {
                                    break;
                                }
                            }
                        }
                    }
                }
                // TODO: CPU issue if enabled :(
                /*
                if (!HOST_CONNECTION_KEEPALIVE) {
                    teardownHostConnection();
                }
                 */
                teardownHostConnection();
            }
            // TODO: CPU issue if enabled :(
            break;
        }
        teardownClientConnection();
        PERFLOGGER.info(String.format("%d msec", System.currentTimeMillis()-startTime));
    }
}