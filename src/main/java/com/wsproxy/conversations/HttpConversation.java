package com.wsproxy.conversations;

import com.wsproxy.configuration.ApplicationConfig;
import com.wsproxy.httpproxy.*;
import com.wsproxy.logging.AppLog;
import com.wsproxy.logging.PerfLog;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Logger;
/*
    Before:
        1.250631
        0.720533
        0.682860
        0.774481
        0.736254
        1.336309
        0.705866
        0.649426
        0.675480
        0.620759
 */
public final class HttpConversation {
    private static final String[] SUPPORTED_METHODS = {"GET","PUT","PATCH","POST","HEAD","OPTIONS","DELETE","CONNECT"};
    private Logger LOGGER = AppLog.getLogger(HttpConversation.class.getName() );
    private static Logger PERFLOGGER = PerfLog.getLogger(HttpConversation.class.getName());
    public static void writeHttpMessageBody(Socket socket, HttpMessage msg ) throws IOException {
        long startTime = System.currentTimeMillis();
        if ( msg.getMessageBody() != null || msg.getBodyChunks().size() > 0 ) {
            OutputStream os = socket.getOutputStream();
            String sniffedTeHeader = msg.getHeaderValue("transfer-encoding");
            if ( sniffedTeHeader != null ){
                if ( sniffedTeHeader.equalsIgnoreCase("chunked") ) {
                    ChunkedMessageWriter chunkWriter = new ChunkedMessageWriter();
                    chunkWriter.writeMessage(os,msg.getBodyChunks());
                }
            }
            else {
                BufferedOutputStream bos = new BufferedOutputStream(os);
                for ( int i = 0; i < msg.getMessageBody().length; i++ ) {
                    bos.write(msg.getMessageBody()[i]);
                }
                bos.flush();
                os.flush();
            }
        }
        PERFLOGGER.info(String.format("%d msec", System.currentTimeMillis()-startTime));
    }

    public static void writeHttpMessageHeader( Socket socket, HttpMessage msg, HttpMessageFormat msgFormat ) throws IOException {
        long startTime = System.currentTimeMillis();
        OutputStream socketOs = socket.getOutputStream();
        byte[] wireBytes = HttpUtil.reformatRequestHeader(msg,msgFormat);
        long swTime = System.currentTimeMillis();
        socketOs.write(wireBytes,0,wireBytes.length);
        socketOs.flush();
        long wTime = System.currentTimeMillis()-swTime;
        PERFLOGGER.info(String.format("%d msec, write took %d msec", System.currentTimeMillis()-startTime,wTime));
    }

    public static void writeHttpMessage(Socket socket, HttpMessage msg, HttpMessagePart messagePart, HttpMessageFormat msgFormat, ApplicationConfig applicationConfig) throws IOException {
        long startTime = System.currentTimeMillis();
        if ( msg != null ) {
            if ( applicationConfig != null ) {
                msg = applyRequestConfigRules(msg, applicationConfig);
            }
        }
        switch ( messagePart ) {
            case HEADER:
                writeHttpMessageHeader(socket, msg, msgFormat);
                break;
            case BODY:
                writeHttpMessageBody(socket, msg);
                break;
            case ALL:
                // write it out all in one if we can
                if ( msg.getHeaderValue("content-length") != null ) {
                    writeHttpMessageHeader(socket, msg, msgFormat);
                    writeHttpMessageBody(socket, msg);
                }
                // we must chunk
                else {
                    writeHttpMessageHeader(socket, msg, msgFormat);
                    writeHttpMessageBody(socket, msg);
                }
                break;
        }
        PERFLOGGER.info(String.format("%d msec", System.currentTimeMillis()-startTime));
    }
    public static void writeHttpMessage(Socket socket, HttpMessage msg, HttpMessagePart messagePart, HttpMessageFormat msgFormat ) throws IOException {
        writeHttpMessage(socket, msg, messagePart, msgFormat,null );
    }


    public static HttpMessage readHttpMessage(Socket socket, int maxHeaderSize ) throws IOException, HttpMessageParseException {
        return readHttpMessage(socket, HttpMessagePart.ALL, maxHeaderSize, null );
    }

    /*
        Should be HTTP or SUPPORTED_METHODS
     */
    public static boolean sniffFirstWordValid( byte buff[] ) {
        if( buff != null && buff.length > 0 ) {
            String firstWord = new String(buff).split("[\\s/]")[0].trim();
            // Check for HTTP
            if ( "HTTP".equals(firstWord)){
                return true;
            }
            // Check for a valid verb
            for ( String verb : SUPPORTED_METHODS ) {
                if ( verb.equals(firstWord)) {
                    return true;
                }
            }
        }
        return false;
    }
    /*
        Checks if we have a first word
     */
    public static boolean hasFirstWord ( byte buff[] ) {
        int maxFirstWordLen = 0;
        for ( String verb : SUPPORTED_METHODS ) {
            if ( verb.length() > maxFirstWordLen ) {
                maxFirstWordLen = verb.length();
            }
        }
        if ( buff != null && buff.length > maxFirstWordLen + 1 ) {
            String str = new String(buff);
            String words[] = str.split("[\\s/]");
            if ( words.length > 1 ) {
                return true;
            }

        }
        return false;
    }
    /*
        TODO:
            - Read as much as we can and then parse header once we know we have it
            - Read remainder of body if content-length present
            - Handle chunking once we get the header, can always go back and snip out the chunk and invoke chunker after
            - Reading in one byte at a time works but perf sucks

     */
    public static HttpMessage readHttpMessage(Socket socket, HttpMessagePart messagePart, int maxHeaderSize, ApplicationConfig applicationConfig ) throws IOException, HttpMessageParseException {
        boolean firstWordSeen = false;
        long startTime = System.currentTimeMillis();
        byte[] messageBytes = {};
        byte[] headerEndBytes = "\r\n\r\n".getBytes();
        InputStream is = socket.getInputStream();
        HttpMessage msg = null;
        byte[] msgHeaderBuff = new byte[maxHeaderSize];
        int bytesRead = 0;
        // Read until we encounter the end of the message header
        int c;
        boolean matched = false;
        while ( bytesRead < msgHeaderBuff.length ) {
            c = is.read();
            msgHeaderBuff[bytesRead] = (byte)c;
            bytesRead += 1;

            if ( bytesRead >= headerEndBytes.length ) {
                int offset = bytesRead-headerEndBytes.length;
                matched = true;
                for ( int j = 0; j < headerEndBytes.length; j++ ) {
                    if ( msgHeaderBuff[offset+j] != headerEndBytes[j] ) {
                        matched = false;
                        break;
                    }
                }

            }

            if ( matched ) {
                messageBytes = new byte[bytesRead];
                System.arraycopy(msgHeaderBuff,0,messageBytes,0,bytesRead);
                break;
            }
            if ( !firstWordSeen ) {
                if ( hasFirstWord(msgHeaderBuff) ) {
                    if ( !sniffFirstWordValid(msgHeaderBuff) ) {
                        throw new HttpMessageParseException("Invalid message start");
                    }
                    firstWordSeen = true;
                }
            }
        }

        if ( messageBytes.length > 0 ) {
            // Check the header to see if it is a valid verb
            String headerStr = new String(msgHeaderBuff);
            msg = new HttpMessage();
            msg.fromBytes(messageBytes);
            if ( messagePart.equals(HttpMessagePart.ALL)) {
                /*
                Figure out if we have content-length and use it to determine how much more
                we have to read
                */
                String sniffedClHeader = msg.getHeaderValue("content-length");
                String sniffedTeHeader = msg.getHeaderValue("transfer-encoding");
                if ( sniffedClHeader != null || sniffedTeHeader != null ) {
                    if ( sniffedClHeader != null ) {
                        if ( sniffedClHeader.matches("\\d+")) {
                            DefaultMessageReader messageReader = new DefaultMessageReader( is, messageBytes );
                            byte[] msgBytes = messageReader.read();
                            msg.fromBytes(msgBytes);
                        }
                    }
                /*
                    If we have a transfer-encoding option we need to use it to read the remaining bytes
                 */
                    if ( sniffedTeHeader != null ){
                        if ( sniffedTeHeader.equalsIgnoreCase("chunked")) {
                            ChunkedMessageReader messageReader = new ChunkedMessageReader( is, messageBytes );
                            msg.setBodyChunks(messageReader.read());
                        }
                    }
                }
            }
        }
        if ( msg != null ) {
            msg = applyResponseConfigRules(msg, applicationConfig);
            PERFLOGGER.info(String.format("%d msec", System.currentTimeMillis()-startTime));
        }
        return msg;
    }

    private static HttpMessage applyRequestConfigRules(HttpMessage msg, ApplicationConfig applicationConfig) {
        if ( applicationConfig.getProperty("http.remove_websocket_extensions").equals("true")) {
            msg.filterHeaders("^(?i)sec-websocket-extensions.*");
        }
        return msg;
    }

    private static HttpMessage applyResponseConfigRules(HttpMessage msg, ApplicationConfig applicationConfig) {
        if ( msg != null ) {
            if ( applicationConfig.getProperty("http.remove_hsts").equals("true")) {
                msg.filterHeaders("^(?i)strict-transport-security.*");
            }
            if ( applicationConfig.getProperty("http.remove_websocket_extensions").equals("true")) {
                msg.filterHeaders("^(?i)sec-websocket-extensions.*");
            }
        }
        return msg;
    }
}
