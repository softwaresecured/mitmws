package com.mitmws.httpserver;

import com.mitmws.httpproxy.websocket.WebsocketFrameType;
import com.mitmws.util.WebsocketUtil;

public class CustomWebsocketFrame {
    private int fin = 0;
    private int rsv1 = 0;
    private int rsv2 = 0;
    private int rsv3 = 0;
    private WebsocketFrameType opcode = null;
    private byte[] payload = null;

    public CustomWebsocketFrame(int fin, int rsv1, int rsv2, int rsv3,String opcode, byte[] payload) {
        this.fin = fin;
        this.rsv1 = rsv1;
        this.rsv2 = rsv2;
        this.rsv3 = rsv3;
        this.opcode = WebsocketUtil.getWebsocketFrameTypeEnum(opcode);
        this.payload = payload;
    }

    public int getFin() {
        return fin;
    }

    public void setFin(int fin) {
        this.fin = fin;
    }

    public int getRsv1() {
        return rsv1;
    }

    public void setRsv1(int rsv1) {
        this.rsv1 = rsv1;
    }

    public int getRsv2() {
        return rsv2;
    }

    public void setRsv2(int rsv2) {
        this.rsv2 = rsv2;
    }

    public int getRsv3() {
        return rsv3;
    }

    public void setRsv3(int rsv3) {
        this.rsv3 = rsv3;
    }

    public String getOpcode() {
        return opcode.toString();
    }

    public void setOpcode(String opcode) {
        this.opcode = WebsocketUtil.getWebsocketFrameTypeEnum(opcode);
    }

    public byte[] getPayload() {
        return payload;
    }

    public String getPayloadAsString() {
        String payloadStr = "";
        if ( payload != null ) {
            payloadStr = new String(payload);
        }
        return payloadStr;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
}
