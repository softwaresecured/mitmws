package com.wsproxy.util;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.wsproxy.environment.Environment;
import com.wsproxy.httpproxy.websocket.WebsocketFrame;
import com.wsproxy.httpproxy.websocket.WebsocketFrameType;
import com.wsproxy.tester.PayloadEncoding;
import com.wsproxy.tester.PayloadList;
import com.wsproxy.tester.TestTarget;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

public final class TestUtil {
    public static final String DEFAULT_TEST_WS_MESSAGE = "The quick brown fox jumps over the lazy dog.";
    public static final String DEFAULT_TEST_NAME = "UNTITLED";
    public static final String DEFAULT_HEADERS = "Host: localhost:9898\n" +
            "User-Agent: __DEFAULT_USER_AGENT__\n" +
            "Sec-WebSocket-Version: 13\n" +
            "Sec-WebSocket-Extensions: __WS_EXTENSIONS__\n" +
            "Sec-WebSocket-Key: __SEC_WEBSOCKET_KEY__\n" +
            "Connection: keep-alive, Upgrade\n" +
            "Upgrade: websocket\n";

    public static final String DEFAULT_TARGET_URL = "http://localhost:9898";
    public static ArrayList<WebsocketFrame> applyEnvironment(ArrayList<WebsocketFrame> frames, Environment env ) {
        return frames;
    }
    public static void delay( long msec ) {
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
        The base name is everything before the first -
     */
    public static String getTestBaseName( String testName ) {
        if ( testName != null ) {
            String parts[] = testName.split("-");
            return parts[0];
        }
        return null;
    }

    /*
        The run number is everything after the first -
     */
    public static int getTestRunNo ( String testName ) {
        if ( testName != null ) {
            String parts[] = testName.split("-");
            if ( parts.length == 2 ) {
                return Integer.parseInt(parts[1]);
            }
        }
        return 0;
    }
    public static byte[] applyTestTarget(TestTarget target, byte[] payload, WebsocketFrame frame) {
        byte appliedPayload[] = null;
        if ( payload == null || frame.getPayloadUnmasked() == null ) {
            return null;
        }
        if ( payload.length == 0 || frame.getPayloadUnmasked().length == 0 ) {
            return null;
        }
        // UTF-8 ( we insert the payload, potentially making the message bigger )
        if ( frame.getOpcode().equals(WebsocketFrameType.TEXT) || frame.getOpcode().equals(WebsocketFrameType.PING) || frame.getOpcode().equals(WebsocketFrameType.PONG)) {
            String sourceText = new String(frame.getPayloadUnmasked());
            StringBuilder sb = new StringBuilder();
            sb.append(sourceText, 0, target.getStartPos());
            sb.append(new String(payload));
            sb.append(sourceText.substring(target.getEndPos()));
            appliedPayload = sb.toString().getBytes();
        }
        // Binary ( we fit whatever we can but maintain size )
        else {
            appliedPayload = frame.getPayloadUnmasked();
            for ( int i = 0; i < payload.length && target.getStartPos() + i < target.getEndPos(); i++ ) {
                appliedPayload[target.getStartPos()+i] = payload[i];
            }
        }
        return appliedPayload;
    }

    public static String encodePayload (PayloadEncoding payloadEncoding, String payload ) {
        String encodedPayload = null;
        if ( payload != null ) {
            if ( payloadEncoding.equals(PayloadEncoding.BASE64)) {
                encodedPayload = Base64.getEncoder().encodeToString(payload.getBytes());
            }
            if ( payloadEncoding.equals(PayloadEncoding.URL)) {
                StringBuilder sb = new StringBuilder();
                for ( byte b : payload.getBytes() ) {
                    sb.append(String.format("%%%02x", b));
                }
                encodedPayload = sb.toString();
            }
            if ( payloadEncoding.equals(PayloadEncoding.XML)) {
                // TODO
            }
            if ( payloadEncoding.equals(PayloadEncoding.JAVASCRIPT)) {
                JsonStringEncoder e = JsonStringEncoder.getInstance();
                encodedPayload = String.valueOf(e.quoteAsString(payload));

            }
            if ( payloadEncoding.equals(PayloadEncoding.HTML)) {
                // TODO
            }
            if ( payloadEncoding.equals(PayloadEncoding.RAW)) {
                return payload;
            }
        }
        return encodedPayload;
    }

    public static ArrayList<String> encodePayloads(ArrayList<PayloadEncoding> payloadEncodings, String payload ) {
        ArrayList<String> payloads = new ArrayList<>();
        payloads.add(payload);
        for ( PayloadEncoding payloadEncoding : payloadEncodings ) {
            String encodedPayload = encodePayload(payloadEncoding,payload);
            if ( encodedPayload != null ) {
                if ( !payloads.contains(encodedPayload)) {
                    payloads.add(encodedPayload);
                }
            }
        }
        return payloads;
    }

    public static ArrayList<PayloadList> reloadPayloadLibrary(PayloadList customPayloadList, String configPath ) {
        ArrayList<PayloadList> payloadLibrary = new ArrayList<>();
        if ( customPayloadList != null ) {
            payloadLibrary.add(customPayloadList);
        }
        File folder = new File(String.format("%s/payloads", configPath));
        try {
            for (File file : folder.listFiles()) {
                if (file.isFile() && file.getName().toLowerCase().endsWith("payloads")) {
                    PayloadList newList = new PayloadList(file.getAbsolutePath());
                    if ( newList.getPayloads().size() > 0 ) {
                        if ( !newList.getPayloadListName().equals("Custom")) {
                            payloadLibrary.add(newList);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return payloadLibrary;
    }
}

