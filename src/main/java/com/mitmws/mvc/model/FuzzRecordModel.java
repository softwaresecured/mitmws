package com.mitmws.mvc.model;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

public class FuzzRecordModel {
    private String id = UUID.randomUUID().toString();
    private String conversationUuid = null;
    private long timestamp = 0;
    private byte[] txFrame;
    private byte[] rxFrame;

    public FuzzRecordModel(String id, String conversationUuid, long timestamp, byte[] txFrame, byte[] rxFrame) {
        this.timestamp = timestamp;
        this.txFrame = txFrame;
        this.rxFrame = rxFrame;
        if ( id != null ) {
            this.id = id;
        }
        this.conversationUuid = conversationUuid;
    }

    public FuzzRecordModel ( String rec ) {
        String direction;
        String parts[] = rec.split(",");
        if ( parts.length == 5 ) {
            id = parts[0];
            conversationUuid = parts[1];
            timestamp = Long.parseLong(parts[2]);
            direction = parts[3];
            if ( direction.equals("IN")) {
                rxFrame = parts[4].length() > 0 ? Base64.getDecoder().decode(parts[4].getBytes(StandardCharsets.UTF_8)) : null;
            }
            else {
                txFrame = parts[4].length() > 0 ? Base64.getDecoder().decode(parts[4].getBytes(StandardCharsets.UTF_8)) : null;
            }
        }
    }

    public String getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] getTxFrame() {
        return txFrame;
    }

    public byte[] getRxFrame() {
        return rxFrame;
    }

    public String getConversationUuid() {
        return conversationUuid;
    }

    @Override
    public String toString() {
        return "FuzzRecordModel{" +
                "id='" + id + '\'' +
                ", conversationUuid='" + conversationUuid + '\'' +
                ", timestamp=" + timestamp +
                ", txFrame=" + Arrays.toString(txFrame) +
                ", rxFrame=" + Arrays.toString(rxFrame) +
                '}';
    }

    public byte[] toFuzzRecord() {
        String direction = txFrame == null ? "IN" : "OUT";
        String fuzzRecord = String.format("%s,%s,%d,%s,%s",
                id,
                conversationUuid,
                timestamp,
                direction,
                direction == "IN" ? new String(Base64.getEncoder().encode(rxFrame)) : new String(Base64.getEncoder().encode(txFrame))
        );
        return fuzzRecord.getBytes(StandardCharsets.UTF_8);
    }

}
