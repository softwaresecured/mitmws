package com.wsproxy.httpproxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChunkedMessageReader  {
    public int HTTP_CHUNK_SCAN_BUFF_LEN = 10; // the number of bytes we use to scan for chunk headers ( 32 bit int as hex string = 8 bytes + 2 for CRLF )
    private InputStream inputStream = null;
    private byte[] messageBytes = null;

    public ChunkedMessageReader( InputStream inputStream, byte[] messageBytes ) {
        this.inputStream = inputStream;
        this.messageBytes = messageBytes;
    }

    public int getChunkLength(byte[] sampleBuff) {
        int chunkLength = -1;
        String curBuffStr = new String(sampleBuff);
        Pattern p = Pattern.compile("^([a-f0-9]+)\\r\\n",Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
        Matcher m = p.matcher(curBuffStr);
        if ( m.find() ) {
            chunkLength = Integer.parseInt(m.group(1),16);
        }
        return chunkLength;
    }

    public ArrayList<byte[]> read() throws IOException, HttpMessageParseException {
        ArrayList<byte[]> chunks = new ArrayList<>();
        while ( true ) {
            // Figure out the next chunk length
            int chunkLength = -1;
            byte[] chunkHeaderScanBuff = new byte[HTTP_CHUNK_SCAN_BUFF_LEN];
            int headerScanPos = 0;
            while ( headerScanPos < HTTP_CHUNK_SCAN_BUFF_LEN ) {
                inputStream.read(chunkHeaderScanBuff,headerScanPos,1);
                chunkLength = getChunkLength(chunkHeaderScanBuff);
                headerScanPos += 1;
                if ( chunkLength >= 0 ) {
                    break;
                }
            }
            /*
            if ( headerScanPos > 0 ) {
                messageBytes = HttpUtil.extendMessageBytes(messageBytes,chunkHeaderScanBuff,headerScanPos);
            }
             */
            // end
            if ( chunkLength == 0 ) {
                messageBytes = HttpUtil.extendMessageBytes(messageBytes,"\r\n".getBytes(),2);
                break;
            }
            if ( chunkLength == -1 ) {
                break;
            }

            // Read a chunk
            if ( chunkLength > 0 ) {
                byte[] chunkReadBuff = new byte[chunkLength];
                int bytesRead = 0;
                while ( bytesRead < chunkReadBuff.length) {
                    bytesRead += inputStream.read(chunkReadBuff,bytesRead,chunkReadBuff.length-bytesRead);
                }
                inputStream.read(new byte[2]);
                chunks.add(chunkReadBuff);
                messageBytes = HttpUtil.extendMessageBytes(messageBytes,chunkReadBuff,bytesRead);
            }

        }
        chunks.add(new byte[0]);
        return chunks;
    }
}
