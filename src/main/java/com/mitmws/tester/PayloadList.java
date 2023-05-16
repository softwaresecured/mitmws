package com.mitmws.tester;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class PayloadList {
    private boolean enabled = true;
    private ArrayList<String> payloads = new ArrayList<>();
    private String payloadFile = null;
    // Header information
    private String payloadListName = "Custom";
    private String payloadDescription = "Custom user payloads";

    public PayloadList() {

    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void loadSamples() {
        payloads.add("<script>alert(__RINT__)</script>");
        payloads.add("a");
        payloads.add("-1");
        payloads.add("0");
        payloads.add("1");
        payloads.add("99999999999999999999999999999999");
        payloads.add("-99999999999999999999999999999999");
    }

    public PayloadList( String payloadFile ) throws IOException {
        this.payloadFile = payloadFile;
        loadPayloadFile();
    }
    private void resetPayloads() {
        payloads = new ArrayList<>();
    }

    public void loadPayloadFile() throws IOException {
        if ( payloadFile != null ) {
            byte[] envBytes = Files.readAllBytes(Paths.get(payloadFile));
            String content = new String(envBytes);
            String[] lines = content.split("\n");

            if ( lines.length > 2 && lines[0].startsWith("# ")) {
                payloadListName = lines[0].substring(1).trim();
                payloadDescription = lines[1].substring(1).trim();
            }
            payloads.addAll(Arrays.asList(lines).subList(2, lines.length));

        }
    }

    public String getPayloadFile() {
        return payloadFile;
    }


    public String getPayloadListName() {
        return payloadListName;
    }

    public void setPayloadListName(String payloadListName) {
        this.payloadListName = payloadListName;
    }

    public String getPayloadDescription() {
        return payloadDescription;
    }

    public void setPayloadDescription(String payloadDescription) {
        this.payloadDescription = payloadDescription;
    }

    public void setPayloads(ArrayList<String> payloads ) {
        this.payloads = payloads;
    }

    public ArrayList<String> getPayloads() {
        return payloads;
    }

    public ArrayList<String> getPayloadsSample( int sampleCount ) {
        ArrayList<String> samplePayloads = new ArrayList<>();
        int payloadCount = 0;
        for ( String payload : getPayloads() ) {
            samplePayloads.add(payload);
            if ( payloadCount > sampleCount ) {
                break;
            }
            payloadCount += 1;
        }
        return samplePayloads;

    }
}
