package com.wsproxy.tester;

import com.wsproxy.util.GuiUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

public class RawWebsocketFrame {
    private int fin = 1;
    private int rsv1 = 0;
    private int rsv2 = 0;
    private int rsv3 = 0;
    private int masked = 0;
    private int opcode = 0;
    int payloadLength = 0;
    private byte[] maskKey = null;
    private byte[] payload = null;
    private byte[] rawFrame = null;

    public RawWebsocketFrame() {

    }

    public RawWebsocketFrame(int fin, int rsv1, int rsv2, int rsv3, int masked, int opcode, int payloadLength, byte[] maskKey, byte[] payload) {
        this.fin = fin;
        this.rsv1 = rsv1;
        this.rsv2 = rsv2;
        this.rsv3 = rsv3;
        this.masked = masked;
        this.opcode = opcode;
        this.payloadLength = payloadLength;
        this.maskKey = maskKey;
        this.payload = payload;
    }

    public byte[] getRawFrame() {
        return rawFrame;
    }

    public void setRawFrame(byte[] rawFrame) {
        this.rawFrame = rawFrame;
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

    public int getMasked() {
        return masked;
    }

    public void setMasked(int masked) {
        this.masked = masked;
    }

    public int getOpcode() {
        return opcode;
    }

    public void setOpcode(int opcode) {
        this.opcode = opcode;
    }

    public int getPayloadLength() {
        return payloadLength;
    }

    public void setPayloadLength(int payloadLength) {
        this.payloadLength = payloadLength;
    }

    public byte[] getMaskKey() {
        return maskKey;
    }

    public void setMaskKey(byte[] maskKey) {
        this.maskKey = maskKey;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        payloadLength = 0;
        if ( payload != null ) {
            payloadLength = payload.length;
        }
        this.payload = payload;
    }

    public int getBit (byte i, int p ) {
        return ( i >> p ) & 1;
    }

    public byte setBit ( byte i, int p ) {
        return (byte)(i | ( 1 << p ));
    }

    public byte[] toBytes() {
        if ( rawFrame == null ) {
            byte[] buff = null;
            ArrayList<byte[]> blockArr = new ArrayList<>();

            // block 1
            byte[] block1 = new byte[1];
            block1[0] = (byte) getOpcode();
            block1[0] = getFin() == 1 ? setBit(block1[0],7) : block1[0];
            block1[0] = getRsv1() == 1 ? setBit(block1[0],6) : block1[0];
            block1[0] = getRsv2() == 1 ? setBit(block1[0],5) : block1[0];
            block1[0] = getRsv3() == 1 ? setBit(block1[0],4) : block1[0];

            // block2
            byte[] block2 = new byte[] {(byte)getPayloadLength()};

            if ( getPayloadLength() > 125 ) {
                if ( getPayloadLength() < 65535 ) {
                    // 16 bit short
                    byte[] payloadLenBuff = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).putShort((short)getPayloadLength()).array();
                    ByteBuffer buffer = ByteBuffer.allocate(3);
                    buffer.put((byte)126);
                    buffer.put(payloadLenBuff);
                    block2 = buffer.array();
                }
                else {
                    // 32 bit int
                    byte[] payloadLenBuff = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt((short)getPayloadLength()).array();
                    ByteBuffer buffer = ByteBuffer.allocate(5);
                    buffer.put((byte)127);
                    buffer.put(payloadLenBuff);
                    block2 = buffer.array();
                }
            }
            if ( getMasked() == 1 ) {
                block2[0] = setBit(block2[0],7);
            }

            // block3
            byte[] block3 = getMaskKey();

            // wrap it all up
            ByteBuffer buffer = ByteBuffer.allocate( block1.length + block2.length + ( block3 != null ? block3.length : 0 ) + getPayloadLength());
            buffer.put(block1);
            buffer.put(block2);
            if ( block3 != null ) {
                buffer.put(block3);
            }
            if ( getPayloadLength() > 0 ) {
                buffer.put(getPayload());
            }
            return buffer.array();
        }
        return rawFrame;
    }

    @Override
    public String toString() {
        return "RawWebsocketFrame{" +
                "fin=" + fin +
                ", rsv1=" + rsv1 +
                ", rsv2=" + rsv2 +
                ", rsv3=" + rsv3 +
                ", masked=" + masked +
                ", opcode=" + opcode +
                ", payloadLength=" + payloadLength +
                ", maskKey=" + Arrays.toString(maskKey) +
                ", payload=" + Arrays.toString(payload) +
                '}';
    }

    public String toPreviewString() {
        byte mask[] = new byte[]{ (byte)0, (byte)0,(byte)0, (byte)0};
        if ( maskKey != null ) {
            mask = maskKey;
        }
        byte pl[] = new byte[0];
        if ( payload != null ) {
            pl = payload;
        }
        return String.format("%d%d%d%d%d%d%d%s%s",fin, rsv1,rsv2,rsv3,masked,opcode,payloadLength, GuiUtils.binToHexStr(mask),GuiUtils.getBinPreviewStr(pl));
    }
}
