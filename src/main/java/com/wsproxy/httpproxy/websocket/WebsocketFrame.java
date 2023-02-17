package com.wsproxy.httpproxy.websocket;


import com.wsproxy.httpproxy.trafficlogger.WebsocketDirection;
import com.wsproxy.tester.RawWebsocketFrame;
import com.wsproxy.util.GuiUtils;
import com.wsproxy.util.WebsocketUtil;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.regex.Pattern;

public class WebsocketFrame implements Serializable {
    // https://datatracker.ietf.org/doc/html/rfc6455
    // Websocket frame flags

    private int id = -1;
    private int fin = 1;
    private int rsv1 = 0;
    private int rsv2 = 0;
    private int rsv3 = 0;
    private int masked = 0;
    private WebsocketFrameType opcode = null;
    int payloadLength = 0;
    private byte[] maskKey = null;
    private byte[] payload = null;

    private String messageUUID = UUID.randomUUID().toString(); // unique id for the frame
    private String upgradeMessageUUID = null; // the upgrade http message used to start the conversation
    private String conversationUUID = null; // the test that this frame may be associated with
    private long createTime = System.currentTimeMillis();
    private WebsocketDirection direction = null;

    private boolean isTrapped = false;
    private String upgradeUrl = null;
    private boolean isDropped = false;

    public WebsocketFrame() {

    }

    public WebsocketFrame(int id, String messageUUID, String upgradeMessageUUID, int fin, int rsv1, int rsv2, int rsv3, int masked, WebsocketFrameType opcode, int payloadLength, byte[] maskKey, byte[] payload, WebsocketDirection direction) {
        this.id = id;
        this.messageUUID = messageUUID;
        this.upgradeMessageUUID = upgradeMessageUUID;
        this.fin = fin;
        this.rsv1 = rsv1;
        this.rsv2 = rsv2;
        this.rsv3 = rsv3;
        this.masked = masked;
        this.opcode = opcode;
        this.payloadLength = payloadLength;
        this.maskKey = maskKey;
        this.payload = payload;
        this.direction = direction;
    }

    public WebsocketFrame(int fin, int rsv1, int rsv2, int rsv3,int masked, WebsocketFrameType opcode, byte[] payload) {
        this.fin = fin;
        this.rsv1 = rsv1;
        this.rsv2 = rsv2;
        this.rsv3 = rsv3;
        this.masked = masked;
        this.opcode = opcode;
        this.payloadLength = 0;
        if ( this.masked == 1 ) {
            this.maskKey = generateMaskBytes();
        }
        if ( payload != null ) {
            payloadLength = payload.length;
        }
        setPayloadUnmasked(payload);
        this.direction = direction;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public WebsocketFrame getCopy() {
        WebsocketFrame newFrame = new WebsocketFrame();
        newFrame.setFin(getFin());
        newFrame.setRsv1(getRsv1());
        newFrame.setRsv2(getRsv2());
        newFrame.setRsv3(getRsv3());
        newFrame.setPayloadLength(getPayloadLength());
        newFrame.setOpcode(getOpcode());
        newFrame.setMasked(getMasked());
        if ( getMasked() == 1 ) {
            newFrame.setMaskKey(generateMaskBytes());
        }
        newFrame.setPayloadUnmasked(getPayloadUnmasked());
        newFrame.setDirection(getDirection());
        return newFrame;
    }

    public String getConversationUUID() {
        return conversationUUID;
    }

    public void setConversationUUID(String conversationUUID) {
        this.conversationUUID = conversationUUID;
    }

    public byte[] generateMaskBytes() {
        Random r = new Random();
        byte[] rMaskBytes = new byte[4];
        r.nextBytes(rMaskBytes);
        return rMaskBytes;
    }

    public WebsocketDirection getDirection() {
        return direction;
    }

    public void setDirection(WebsocketDirection direction) {
        this.direction = direction;
    }



    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public byte[] toBytes() {
        byte[] buff = null;

        ArrayList<byte[]> blockArr = new ArrayList<>();

        // block 1
        byte[] block1 = new byte[1];
        block1[0] = (byte) getOpcode().ordinal();
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

    public int getBit (byte i, int p ) {
        return ( i >> p ) & 1;
    }

    public byte setBit ( byte i, int p ) {
        return (byte)(i | ( 1 << p ));
    }

    // fin / opcode
    public boolean parseBlock1(byte[] buff){
        if ( buff != null ) {
            if ( buff.length == 1 ) {
                byte b = buff[0];
                setFin(getBit(b,7));
                setRsv1(getBit(b,6));
                setRsv2(getBit(b,5));
                setRsv3(getBit(b,4));
                byte mask = (byte)0b11110000;
                byte opcode_b = (byte)(b & ~mask);
                if ( (int) opcode_b <= 10 ) {
                    switch ((int) opcode_b) {
                        case 0:
                            setOpcode(WebsocketFrameType.CONTINUATION);
                            break;
                        case 1:
                            setOpcode(WebsocketFrameType.TEXT);
                            break;
                        case 2:
                            setOpcode(WebsocketFrameType.BINARY);
                            break;
                        case 3:
                            setOpcode(WebsocketFrameType.RESERVED1);
                            break;
                        case 4:
                            setOpcode(WebsocketFrameType.RESERVED2);
                            break;
                        case 5:
                            setOpcode(WebsocketFrameType.RESERVED3);
                            break;
                        case 6:
                            setOpcode(WebsocketFrameType.RESERVED4);
                            break;
                        case 7:
                            setOpcode(WebsocketFrameType.RESERVED5);
                            break;
                        case 8:
                            setOpcode(WebsocketFrameType.CLOSE);
                            break;
                        case 9:
                            setOpcode(WebsocketFrameType.PING);
                            break;
                        case 10:
                            setOpcode(WebsocketFrameType.PONG);
                            break;
                    }
                    return true;
                }
            }
            else {
                //System.out.println("Wrong size");
            }
        }
        return false;
    }

    // mask / payload len
    public boolean parseBlock2(byte[] buff) {
        if (buff != null) {
            if (buff.length == 1) {
                byte b = buff[0];
                setMasked(getBit(b,7));
                byte mask = (byte)0b10000000;
                b = (byte)(b & ~mask);
                setPayloadLength(b);
                return true;
            }
        }
        return false;
    }
/*
   https://datatracker.ietf.org/doc/html/rfc6455#section-5.1
   Payload length:  7 bits, 7+16 bits, or 7+64 bits

      The length of the "Payload data", in bytes: if 0-125, that is the
      payload length.  If 126, the following 2 bytes interpreted as a
      16-bit unsigned integer are the payload length.  If 127, the
      following 8 bytes interpreted as a 64-bit unsigned integer (the
      most significant bit MUST be 0) are the payload length.  Multibyte
      length quantities are expressed in network byte order.  Note that
      in all cases, the minimal number of bytes MUST be used to encode
      the length, for example, the length of a 124-byte-long string
      can't be encoded as the sequence 126, 0, 124.  The payload length
      is the length of the "Extension data" + the length of the
      "Application data".  The length of the "Extension data" may be
      zero, in which case the payload length is the length of the
      "Application data".
 */
    // extended payload len
    public boolean parseBlock3(byte[] buff){
        if ( buff != null ) {
            switch ( buff.length ) {
                case 2:
                    setPayloadLength(ByteBuffer.wrap(buff).order(ByteOrder.BIG_ENDIAN).getShort());
                    break;
                case 4:
                    setPayloadLength(ByteBuffer.wrap(buff).order(ByteOrder.BIG_ENDIAN).getInt());
                    break;
                default:
                    return false;
            }
            return true;
        }
        return false;
    }

    // masking key
    public boolean parseBlock4(byte[] buff){
        if ( buff != null ) {
            if ( buff.length == 4 ) {
                setMaskKey(buff);
                return true;
            }
        }
        return false;
    }

    // payload data
    public boolean parseBlock5(byte[] buff){
        if ( buff != null ) {
            setPayload(buff);
            return true;
        }
        return false;
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

    public WebsocketFrameType getOpcode() {
        return opcode;
    }

    public int getOpcodeInt() {
        return getOpcode().ordinal();
    }

    public void setOpcode(WebsocketFrameType opcode) {
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

    /*
    Applies mask if set
    */
    public byte[] getPayloadUnmasked() {
        byte[] buff = getPayload();
        if ( buff != null ) {
            if ( getMasked() == 1 && getMaskKey() != null ) {
                buff = WebsocketUtil.xorBytes(getMaskKey(),buff);
            }
        }
        return  buff;
    }

    public String getPayloadString() {
        String ret = "";
        byte[] payloadUnmasked = getPayloadUnmasked();
        if ( payloadUnmasked != null ) {
            ret = new String(payloadUnmasked);
        }
        return ret;
    }

    public boolean payloadMatchesRegex(String regex) {
        Pattern p = Pattern.compile(regex, Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
        return p.matcher(getPayloadString()).find();
    }

    public String getPayloadAsHexString() {
        if ( getPayloadUnmasked() != null ) {
            return GuiUtils.binToHexStr(getPayloadUnmasked());
        }
        return "";
    }

    public void setPayloadUnmasked(byte[] buff) {
        payloadLength = 0;
        if ( buff != null ) {
            payload = buff;
            payloadLength = buff.length;
            if ( buff != null && getMaskKey() != null) {
                payload = WebsocketUtil.xorBytes(getMaskKey(),buff);
            }
        }
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] rawPayload) {
        payloadLength = 0;
        if ( rawPayload != null ) {
            payloadLength = rawPayload.length;
        }
        this.payload = rawPayload;
    }
    public String getMessageUUID() {
        return messageUUID;
    }
    public int getMasked() {
        return masked;
    }

    public void setMasked(int masked) {
        this.masked = masked;
    }

    public void setMessageUUID(String messageUUID) {
        this.messageUUID = messageUUID;
    }

    public String getUpgradeMessageUUID() {
        return upgradeMessageUUID;
    }

    public void setUpgradeMessageUUID(String upgradeMessageUUID) {
        this.upgradeMessageUUID = upgradeMessageUUID;
    }

    public String toCsv() {
        String payloadb64 = "";
        if ( getPayloadUnmasked() != null ) {
            payloadb64 = Base64.getEncoder().encodeToString(getPayloadUnmasked());
        }
        String csv = String.format("%d,%d,%d,%d,%s,%s,%s", fin, rsv1,rsv2,rsv3,opcode.toString(),direction.toString(),payloadb64);
        return csv;
    }
    @Override
    public String toString() {
        return "WebsocketFrame{" +
                "id=" + id +
                ", fin=" + fin +
                ", rsv1=" + rsv1 +
                ", rsv2=" + rsv2 +
                ", rsv3=" + rsv3 +
                ", masked=" + masked +
                ", opcode=" + opcode +
                ", payloadLength=" + payloadLength +
                ", maskKey=" + Arrays.toString(maskKey) +
                ", payload=" + Arrays.toString(payload) +
                ", messageUUID='" + messageUUID + '\'' +
                ", upgradeMessageUUID='" + upgradeMessageUUID + '\'' +
                ", conversationUUID='" + conversationUUID + '\'' +
                ", createTime=" + createTime +
                ", direction=" + direction +
                '}';
    }

    public boolean isDropped() {
        return isDropped;
    }

    public void setDropped(boolean dropped) {
        isDropped = dropped;
    }

    public boolean isTrapped() {
        return isTrapped;
    }

    public void setTrapped(boolean trapped) {
        isTrapped = trapped;
    }

    public String getUpgradeUrl() {
        return upgradeUrl;
    }

    public void setUpgradeUrl(String upgradeUrl) {
        this.upgradeUrl = upgradeUrl;
    }

    public RawWebsocketFrame toRawFrame() {
        return new RawWebsocketFrame(fin, rsv1, rsv2, rsv3, masked, opcode.ordinal(), payloadLength, maskKey, payload);
    }
}
