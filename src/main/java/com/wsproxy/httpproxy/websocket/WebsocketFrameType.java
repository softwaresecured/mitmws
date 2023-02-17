package com.wsproxy.httpproxy.websocket;

public enum WebsocketFrameType {
    CONTINUATION, // 0
    TEXT, // 1
    BINARY, // 2
    RESERVED1,
    RESERVED2,
    RESERVED3,
    RESERVED4,
    RESERVED5,
    CLOSE, // 8
    PING, // 9
    PONG // 10
}
