package com.wsproxy.mvc.model;

public class InteractShTestPayload {
    private String testPayload;
    private String interactShPayload;

    public InteractShTestPayload(String testPayload, String interactShPayload) {
        this.testPayload = testPayload;
        this.interactShPayload = interactShPayload;
    }

    public String getTestPayload() {
        return testPayload;
    }

    public void setTestPayload(String testPayload) {
        this.testPayload = testPayload;
    }

    public String getInteractShPayload() {
        return interactShPayload;
    }

    public void setInteractShPayload(String interactShPayload) {
        this.interactShPayload = interactShPayload;
    }

}
