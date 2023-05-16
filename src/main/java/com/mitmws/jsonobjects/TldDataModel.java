package com.mitmws.jsonobjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TldDataModel {

    public TldDataModel() {

    }

    private String id = UUID.randomUUID().toString();

    @JsonProperty("full-id")
    private String fullId = null;

    @JsonProperty("unique-id")
    private String uniqueId = null;

    @JsonProperty("protocol")
    private String protocol = null;

    @JsonProperty("raw-request")
    private String rawRequest = null;

    @JsonProperty("remote-address")
    private String remoteAddress = null;

    @JsonProperty("smtp-from")
    private String smtpFrom = null;

    @JsonProperty("timestamp")
    private String timeStamp = null;

    @JsonProperty("q-type")
    private String qType = null;

    public String getId() {
        return id;
    }

    public String getFullId() {
        return fullId;
    }

    public void setFullId(String fullId) {
        this.fullId = fullId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getRawRequest() {
        return rawRequest;
    }

    public void setRawRequest(String rawRequest) {
        this.rawRequest = rawRequest;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getSmtpFrom() {
        return smtpFrom;
    }

    public void setSmtpFrom(String smtpFrom) {
        this.smtpFrom = smtpFrom;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getqType() {
        return qType;
    }

    public void setqType(String qType) {
        this.qType = qType;
    }

    @Override
    public String toString() {
        return "TldDataModel{" +
                "fullId='" + fullId + '\'' +
                ", uniqueId='" + uniqueId + '\'' +
                ", protocol='" + protocol + '\'' +
                ", rawRequest='" + rawRequest + '\'' +
                ", remoteAddress='" + remoteAddress + '\'' +
                ", smtpFrom='" + smtpFrom + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", qType='" + qType + '\'' +
                '}';
    }
}
