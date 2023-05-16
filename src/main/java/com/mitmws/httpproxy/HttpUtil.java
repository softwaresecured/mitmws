package com.mitmws.httpproxy;

import com.mitmws.conversations.HttpConversation;
import com.mitmws.logging.PerfLog;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Logger;

public final class HttpUtil {
    private static Logger PERFLOGGER = PerfLog.getLogger(HttpConversation.class.getName());
    public static String getHttpDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    public static byte[] reformatRequestHeader( HttpMessage msg, HttpMessageFormat msgFormat ) {
        long startTime = System.currentTimeMillis();
        byte[] msgHeaderBytes = msg.getHeaderBytes();
        //System.out.println(String.format("--->\n%s\n--->\n", new String(msgHeaderBytes)));
        if ( msgHeaderBytes != null ) {
            // outgoing changes
            if ( msg.getHttpMethod() != null ) {
                String headerStr = new String(msgHeaderBytes);
                String[] headerLines = headerStr.split("\r\n");
                if ( headerLines.length > 0 ) {
                    if ( msgFormat.equals(HttpMessageFormat.DIRECT)) {
                        // Only path + query can be in first header line
                        try {
                            //System.out.println(msg.toString());
                            URL httpUrl = new URL(msg.getUrl());
                            String path = httpUrl.getPath().length() == 0 ? "/" : httpUrl.getPath();
                            if (httpUrl.getQuery() != null ) {
                                if ( httpUrl.getQuery().length() > 0 ) {
                                    path += "?" + httpUrl.getQuery();
                                }
                            }
                            headerLines[0] = String.format("%s %s HTTP/%s", msg.getHttpMethod(), path, msg.getProtocol());
                        } catch (MalformedURLException e) {
                            ;
                        }
                    }
                    if ( msgFormat.equals(HttpMessageFormat.UPSTREAM_HTTP_PROXY)) {
                        // The full URL must be in the first header line
                        headerLines[0] = String.format("%s %s HTTP/%s", msg.getHttpMethod(), msg.getUrl(), msg.getProtocol());
                    }
                    headerStr = String.join("\r\n",headerLines);
                    headerStr += "\r\n\r\n";
                    //System.out.println(String.format("<---\n%s\n<---\n", headerStr));
                    msgHeaderBytes = headerStr.getBytes();
                }
            }
        }
        PERFLOGGER.info(String.format("%d msec", System.currentTimeMillis()-startTime));
        return msgHeaderBytes;
    }

    public static int getPort(URL url ) {
        int port = 0;
        if ( url.getPort() == -1 ) {
            if ( url.getProtocol().equalsIgnoreCase("http")) {
                port = 80;
            }
            if ( url.getProtocol().equalsIgnoreCase("https")) {
                port = 443;
            }
        }
        else {
            port = url.getPort();
        }
        return port;
    }

    public static byte[] decodeHexStr( String hexStr ) {
        byte[] decodedBytes = null;
        if ( hexStr.length() % 2 == 0 ) {
            decodedBytes = new byte[hexStr.length()/2];
            for ( int i = 0,j = 0; i < hexStr.length()-1; i += 2,j += 1 ) {
                String curByteHex = hexStr.substring(i,i+2);
                int curInt = Integer.parseInt(curByteHex,16);
                decodedBytes[j] = (byte)curInt;
            }
        }
        //System.out.println(String.format("Decoding [%s] -> %d", hexStr, Integer.parseInt(new String(decodedBytes), 16)));
        return decodedBytes;
    }

    public static byte[] extendMessageBytes( byte[] src, byte[] ext, int len ) {
        byte[] tmpBuff = new byte[src.length + len];
        System.arraycopy(src, 0, tmpBuff, 0, src.length);
        System.arraycopy(ext, 0, tmpBuff, src.length, len);
        return tmpBuff;
    }

    public byte[] stripFullUrlPath(byte[] headerBytes) {
        return headerBytes;
    }

    public static int getMessageHeaderEndPos(byte[] buff) {
        String headerEnd = "\r\n\r\n";
        int messageCrlf = -1;
        for ( int i = 0; i < buff.length-headerEnd.getBytes().length+1; i++ ) {
            if ( buff[i] == headerEnd.getBytes()[0] ) {
                boolean matched = true;
                for ( int j = 0; j < headerEnd.getBytes().length; j++ ) {
                    if ( buff[i+j] != headerEnd.getBytes()[j] ) {
                        matched = false;
                        break;
                    }
                }
                if ( matched ) {
                    messageCrlf = i+2; // include the last header's crlf
                    break;
                }
            }
        }
        return messageCrlf;
    }
}
