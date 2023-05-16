package com.mitmws.httpproxy;

import java.io.IOException;

public interface HttpMessageReader {
    byte[] read() throws IOException, HttpMessageParseException;
}
