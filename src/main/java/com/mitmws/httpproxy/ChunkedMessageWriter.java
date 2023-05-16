package com.mitmws.httpproxy;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class ChunkedMessageWriter {
    public ChunkedMessageWriter() {
    }
    public void writeChunkHeader( OutputStream outputStream, int length ) throws IOException {
        outputStream.write(String.format("%s\r\n", Integer.toHexString(length)).getBytes());
        outputStream.flush();
    }
    public void writeMessage(OutputStream outputStream, ArrayList<byte[]> chunks ) throws IOException {
        for ( byte[] chunk : chunks ) {
            writeChunkHeader(outputStream, chunk.length);
            outputStream.write(chunk);
            outputStream.write("\r\n".getBytes());
            outputStream.flush();
        }
    }
}
