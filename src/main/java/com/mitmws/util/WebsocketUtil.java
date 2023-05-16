package com.mitmws.util;

import com.mitmws.httpproxy.websocket.WebsocketFrame;
import com.mitmws.httpproxy.websocket.WebsocketFrameType;
import com.mitmws.httpserver.CustomWebsocketFrame;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public final class WebsocketUtil {

    public static byte[] xorBytes(byte[] key, byte[] src) {
        byte[] buff = new byte[src.length];
        int k_offset = 0;
        for ( int i = 0; i < src.length; i++, k_offset++ ) {
            if ( k_offset > key.length-1 ) {
                k_offset = 0;
            }
            buff[i] = (byte)(0xff & ((int)src[i] ^ (int)key[k_offset]));
        }
        return buff;
    }

    public static String getWebsocketKeyAnswer ( String websocketKey ) {
        String key = null;
        if ( websocketKey != null ) {
            try {
                //byte[] clientIn = Base64.getDecoder().decode(websocketKey);
                byte[] clientIn = websocketKey.getBytes(StandardCharsets.UTF_8);
                byte[] magicNum = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11".getBytes(StandardCharsets.UTF_8);
                byte buff[] = new byte[clientIn.length+magicNum.length];
                System.arraycopy(clientIn,0,buff,0,clientIn.length);
                System.arraycopy(magicNum,0,buff,clientIn.length,magicNum.length);
                key = HashUtils.sha1sum(buff);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return key;
    }

    public static WebsocketFrame customWebsocketFrameToWebsocketFrame( CustomWebsocketFrame customWebsocketFrame ) {
        WebsocketFrame websocketFrame = null;
        if ( customWebsocketFrame != null ) {
            websocketFrame = new WebsocketFrame();
            websocketFrame.setFin(customWebsocketFrame.getFin());
            websocketFrame.setRsv1(customWebsocketFrame.getRsv1());
            websocketFrame.setRsv2(customWebsocketFrame.getRsv2());
            websocketFrame.setRsv3(customWebsocketFrame.getRsv3());
            websocketFrame.setOpcode(getWebsocketFrameTypeEnum(customWebsocketFrame.getOpcode()));
            websocketFrame.setPayload(customWebsocketFrame.getPayload());
        }
        return websocketFrame;
    }

    public static CustomWebsocketFrame websocketFrameToCustomWebsocketFrame(WebsocketFrame frame ) {
        CustomWebsocketFrame response = null;
        if ( frame != null ) {
            response = new CustomWebsocketFrame(frame.getFin(), frame.getRsv1(),frame.getRsv2(),frame.getRsv3(), frame.getOpcode().toString(), frame.getPayloadUnmasked());
        }
        return response;
    }

    public static WebsocketFrameType getWebsocketFrameTypeEnum(String opcode ) {
        WebsocketFrameType websocketFrameType = null;
        if ( opcode.equals("CONTINUATION")) {
            websocketFrameType = WebsocketFrameType.CONTINUATION;
        }
        else if ( opcode.equals("TEXT")) {
            websocketFrameType = WebsocketFrameType.TEXT;
        }
        else if ( opcode.equals("BINARY")) {
            websocketFrameType = WebsocketFrameType.BINARY;
        }
        else if ( opcode.equals("RESERVED1")) {
            websocketFrameType = WebsocketFrameType.RESERVED1;
        }
        else if ( opcode.equals("RESERVED2")) {
            websocketFrameType = WebsocketFrameType.RESERVED2;
        }
        else if ( opcode.equals("RESERVED3")) {
            websocketFrameType = WebsocketFrameType.RESERVED3;
        }
        else if ( opcode.equals("RESERVED4")) {
            websocketFrameType = WebsocketFrameType.RESERVED4;
        }
        else if ( opcode.equals("RESERVED5")) {
            websocketFrameType = WebsocketFrameType.RESERVED5;
        }
        else if ( opcode.equals("CLOSE")) {
            websocketFrameType = WebsocketFrameType.CLOSE;
        }
        else if ( opcode.equals("PING")) {
            websocketFrameType = WebsocketFrameType.PING;
        }
        else if ( opcode.equals("PONG")) {
            websocketFrameType = WebsocketFrameType.PONG;
        }
        return  websocketFrameType;
    }
}
