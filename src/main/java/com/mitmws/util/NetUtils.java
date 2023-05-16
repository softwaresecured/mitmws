package com.mitmws.util;

import com.mitmws.network.MitmWsSocketFactory;
import com.mitmws.version.VERSION;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;

public final class NetUtils {
    public static String getRemoteUrl( String url ) throws IOException {
        return getRemoteUrl( url, null);
    }

    public static String getFrieldyClientName( Socket clientSocket ) {
        return clientSocket.getRemoteSocketAddress().toString().split("/")[0];
    }
    /*
        Returns a utf-8 string
     */
    public static String getRemoteUrl( String url, Proxy proxy ) throws IOException {
        String ret = null;
        URL conn = new URL(url);
        URLConnection urlConn = null;

        if ( proxy != null ) {
            urlConn = conn.openConnection(proxy);
        }
        else {
            urlConn = conn.openConnection();
        }
        urlConn.setRequestProperty("User-Agent", String.format("mitmws/%s", VERSION.getVersionStr()));


        BufferedInputStream bi = new BufferedInputStream(urlConn.getInputStream());
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        int c = 0;
        do {
            c = bi.read();
            if ( c >= 0 ) {
                bo.write((byte) c);
            }
        } while ( c >= 0 );
        if ( bo.size() > 0 ) {
            ret = bo.toString();
        }
        bi.close();
        bo.close();
        return ret;
    }
    public static ArrayList<String> getLocalListenAddresses() {
        ArrayList<String> addrList = new ArrayList<>();
        try {
            for(Enumeration<NetworkInterface> inetInterface = NetworkInterface.getNetworkInterfaces(); inetInterface.hasMoreElements(); ) {
                NetworkInterface ifc = inetInterface.nextElement();
                for(Enumeration<InetAddress> ena = ifc.getInetAddresses(); ena.hasMoreElements(); ) {
                    addrList.add(ena.nextElement().getHostAddress());
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return addrList;
    }

    public static boolean testListen( String listenAddr, int listenPort ) {
        boolean listenOK = false;
        try {
            ServerSocket testServerSocket = null;
            SocketAddress localAddress =new InetSocketAddress(InetAddress.getByName(listenAddr), listenPort);
            testServerSocket = new ServerSocket();
            testServerSocket.bind(localAddress);
            testServerSocket.close();
            listenOK = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listenOK;
    }

    public static String apiRequest( String method, String baseUrl, String path, String query, String postBody, String auth ) throws IOException {
        String url = String.format("%s/%s", baseUrl, path);
        if ( query != null ) {
            url = String.format("%s/%s?%s", baseUrl, path, query);
        }
        URL conn = new URL(url);
        HttpsURLConnection urlConn = (HttpsURLConnection) conn.openConnection();
        if ( auth != null ) {
            urlConn.setRequestProperty("Authorization", auth);
        }
        urlConn.setRequestMethod(method);
        urlConn.setDoOutput(true);

        if ( postBody != null ) {
            OutputStream os = urlConn.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(postBody);
            osw.flush();
            osw.close();
            os.close();
        }

        String result;
        BufferedInputStream bis = new BufferedInputStream(urlConn.getInputStream());
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int bytesRead = bis.read();
        while(bytesRead != -1) {
            buf.write((byte) bytesRead);
            bytesRead = bis.read();
        }
        result = buf.toString();
        return result;
    }

    public static boolean testRemotePort ( String remoteAddr, int remotePort ) {
        boolean connectOK = false;
        Socket socket = null;
        try {
            socket = MitmWsSocketFactory.getPlaintextSocket(remoteAddr,remotePort,null);
            SocketAddress remoteAddress=new InetSocketAddress(InetAddress.getByName(remoteAddr), remotePort);
            socket.bind(remoteAddress);
            socket.connect(remoteAddress);
            connectOK = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if ( socket != null ) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return connectOK;
    }

}
