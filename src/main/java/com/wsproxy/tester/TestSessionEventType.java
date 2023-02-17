package com.wsproxy.tester;

public enum TestSessionEventType {
    TEST_STARTED,
    TEST_ENDED,
    BEFORE_CONNECT,
    AFTER_CONNECT,
    BEFORE_DISCONNECT,
    AFTER_DISCONNECT,
    FRAME_SENT,
    FRAME_RECEIVED,
    STEP_COMPLETED,
}
