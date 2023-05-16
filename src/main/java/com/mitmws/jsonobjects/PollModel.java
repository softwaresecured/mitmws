package com.mitmws.jsonobjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;

public class PollModel {
    @JsonProperty("data")
    private String data[] = null;
    @JsonProperty("extra")
    private String extra[] = null;
    @JsonProperty("aes_key")
    private String aesKey = null;
    @JsonProperty("tlddata")
    private TldDataModel tldData[] = null;

    @Override
    public String toString() {
        return "PollModel{" +
                "data=" + Arrays.toString(data) +
                ", extra=" + Arrays.toString(extra) +
                ", aesKey='" + aesKey + '\'' +
                ", tldData=" + Arrays.toString(tldData) +
                '}';
    }

    public String[] getData() {
        return data;
    }

    public void setData(String[] data) {
        this.data = data;
    }

    public String[] getExtra() {
        return extra;
    }

    public void setExtra(String[] extra) {
        this.extra = extra;
    }

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }

    public TldDataModel[] getTldData() throws JsonProcessingException {
        return tldData;
    }

    public void setTldData(String[] tldData) throws JsonProcessingException {
        if ( tldData != null ) {
            ArrayList<TldDataModel> models = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();
            for ( String tldDataJson : tldData ) {
                models.add(mapper.readValue(tldDataJson,TldDataModel.class));
            }
            this.tldData = new TldDataModel[models.size()];
            for ( int i = 0 ; i < models.size(); i++ ) {
                this.tldData[i] = models.get(i);
            }
        }
    }
}
