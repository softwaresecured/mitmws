package com.wsproxy.httpproxy;

public class HttpConfig {
    public int HTTP_CHUNK_SCAN_BUFF_LEN = 10; // the number of bytes we use to scan for chunk headers ( 32 bit int as hex string = 8 bytes + 2 for CRLF )
    public int HTTP_READ_TIMEOUT_SEC = 2;
    public int HTTP_READ_BUFF_LEN = 1024*1024;
}
