package com.wsproxy.tester;

import com.wsproxy.httpproxy.websocket.WebsocketFrameType;

import java.util.ArrayList;

public class ProtocolTesterSettings {
    private ArrayList<WebsocketFrameType> testFrameTypes = new ArrayList<WebsocketFrameType>();
    private int readTimeoutMsec = 1000;
    private int deathWaitTimeout = 5000;
}
