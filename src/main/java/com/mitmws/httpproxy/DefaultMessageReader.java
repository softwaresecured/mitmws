package com.mitmws.httpproxy;

import java.io.IOException;
import java.io.InputStream;

public class DefaultMessageReader {

    private InputStream inputStream = null;
    private byte[] messageBytes = null;

    public DefaultMessageReader( InputStream inputStream, byte[] messageBytes ) {
        this.inputStream = inputStream;
        this.messageBytes = messageBytes;
    }
    public byte[] read() throws IOException, HttpMessageParseException {
        HttpMessage sniffMsg = new HttpMessage();
        sniffMsg.fromBytes(messageBytes);
        int sniffedContentLength = Integer.parseInt(sniffMsg.getHeaderValue("content-length"));
        int msgBlockEnd = HttpUtil.getMessageHeaderEndPos(messageBytes) + 2;
        int bytesRead = 0;
        int remainingBytes = sniffedContentLength - (messageBytes.length-msgBlockEnd);
        while ( remainingBytes > 0 ) {
            byte[] readBuff = new byte[remainingBytes];
            bytesRead = inputStream.read(readBuff, 0, remainingBytes);
            messageBytes = HttpUtil.extendMessageBytes(messageBytes, readBuff, bytesRead);
            remainingBytes = sniffedContentLength - (messageBytes.length-msgBlockEnd);
        }
        return messageBytes;
    }
}
