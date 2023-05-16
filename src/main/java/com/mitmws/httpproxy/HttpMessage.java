package com.mitmws.httpproxy;
import com.mitmws.util.GuiUtils;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpMessage implements Serializable {
    private byte[] rawBytes = null;         // as it was read off the wire
    private String messageHeader = null;
    private byte[] messageBody = null;
    private URL httpUrl = null;
    private String statusMessage = null;
    private int statusCode = 0;
    private String httpMethod = null;
    private String protocol = null;
    private int contentLength = 0;
    private ArrayList<byte[]> bodyChunks = new ArrayList<>();
    private String messageUUID = UUID.randomUUID().toString();
    private long createTime = System.currentTimeMillis();
    private boolean sslEnabled = false;

    public HttpMessage() {
    }
    public HttpMessage ( String messageUUID, byte[] header, byte[] body ) throws HttpMessageParseException {
        this.messageUUID = messageUUID;
        fromParts( header, body );
    }

    public HttpMessage getCopy() {
        HttpMessage newMsg = new HttpMessage();
        try {
            newMsg.fromBytes(getBytes());
        } catch (HttpMessageParseException e) {
            e.printStackTrace();
        }
        newMsg.setSslEnabled(isSslEnabled());
        return newMsg;
    }
    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String[] getHeaders() {
        String[] headerArr = null;
        String[] headerLines = messageHeader.split("\r\n");
        ArrayList<String> tmpArr = new ArrayList<>();
        if ( headerLines.length > 1 ) {
            for ( int i = 1; i < headerLines.length; i++ ) {
                if ( headerLines[i].trim().length() > 0 ) {
                    tmpArr.add(headerLines[i].trim());
                }
            }
            headerArr = tmpArr.toArray(new String[0]);
        }
        return headerArr;
    }

    public String getHeaderValue (String headerName ) {
        String headerValue = null;
        String[] lines = messageHeader.split("\r\n");
        String searchStr = String.format("^(%s:\\s)",headerName);
        Pattern r = Pattern.compile(searchStr,Pattern.CASE_INSENSITIVE);
        for ( String currentHeader : lines ) {
            Matcher m = r.matcher(currentHeader);
            if (m.find()) {
                headerValue = m.replaceAll("").trim();
            }
        }
        return headerValue;
    }

    public byte[] getBodyBytes() {
        return messageBody;
    }

    public String getBodyPreviewString() {
        String previewStr = "";
        if ( messageBody != null ) {
            previewStr = GuiUtils.getBinPreviewStr(messageBody);
        }
        if ( bodyChunks.size() > 0 ) {
            int size = 0;
            int p = 0;
            for ( byte[] buff : getBodyChunks() ) {
                size += buff.length;
            }
            byte fullBody[] = new byte[size];
            for ( byte[] buff : getBodyChunks() ) {
                System.arraycopy(buff,0,fullBody,p,buff.length);
                p += buff.length;
            }
            previewStr = GuiUtils.getBinPreviewStr(fullBody);
        }
        if ( previewStr.length() > 1000 ) {
            previewStr = previewStr.substring(0, 1000);
        }
        return previewStr;
    }


    /*
        Returns the header as it should be written directly to a server not an upstream proxy
     */

    public byte[] getHeaderBytes() {
        int responseLen = messageHeader.getBytes().length+2;
        //if ( messageBody != null ) {
        //    responseLen += messageBody.length;
        //}
        ByteBuffer respBytes = ByteBuffer.allocate(responseLen);
        respBytes.put(messageHeader.getBytes());
        respBytes.put("\r\n".getBytes());
        return respBytes.array();
    }


    public byte[] getBytes() {
        int responseLen = messageHeader.getBytes().length+2;
        if ( messageBody != null ) {
            responseLen += messageBody.length;
        }
        ByteBuffer respBytes = ByteBuffer.allocate(responseLen);
        respBytes.put(messageHeader.getBytes());
        respBytes.put("\r\n".getBytes());
        if ( messageBody != null ) {
            respBytes.put(messageBody);
        }
        return respBytes.array();
    }

    public void parseContentLength() {
        Pattern r = Pattern.compile("^content-length\\s?:\\s(\\d+)^?",Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
        Matcher m = r.matcher(messageHeader);
        if (m.find()) {
            contentLength = Integer.parseInt(m.group(1));
        }
    }

    public void parseProtocol() throws HttpMessageParseException {
        Pattern r = Pattern.compile("HTTP/(\\d+[\\d.]+)",Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
        Matcher m = r.matcher(messageHeader);
        if (m.find()) {
            protocol = m.group(1);
        }
        else {
            throw new HttpMessageParseException("Could not find protocol");
        }
    }

    public void parseStatusMessage() {
        Pattern r = Pattern.compile("^HTTP/\\d+[\\d.]+\\s\\d+(.*)$",Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
        Matcher m = r.matcher(messageHeader);
        if (m.find()) {
            statusMessage = m.group(1);
        }
    }

    public void parseStatusCode() {
        Pattern r = Pattern.compile("^HTTP/\\d+[\\d.]+\\s(\\d+).*$",Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
        Matcher m = r.matcher(messageHeader);
        if (m.find()) {
            statusCode = Integer.parseInt(m.group(1));
        }
    }
    public void parseMethod() {
        Pattern r = Pattern.compile("^([a-z]+)\\s.*HTTP/\\d+[\\d.]+$",Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
        Matcher m = r.matcher(messageHeader);
        if (m.find()) {
            httpMethod = m.group(1);
        }
    }

    public String getConnectAddress() {
        String connectAddress = null;
        Pattern r = Pattern.compile("^connect\\s(.*):(\\d+)\\sHTTP/\\d+[\\d.]+$",Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
        Matcher m = r.matcher(messageHeader);
        if (m.find()) {
            connectAddress = String.format("%s:%s", m.group(1),m.group(2));
        }
        return connectAddress;
    }

    public void parseHttpUrl() throws HttpMessageParseException {
        Pattern r = Pattern.compile("^[a-z]+\\s(.*)HTTP/\\d+[\\d.]+$",Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
        Matcher m = r.matcher(messageHeader);
        if (m.find()) {
            try {
                httpUrl = new URL(m.group(1));
            } catch (MalformedURLException e) {
                //throw new HttpMessageParseException(e.getMessage());
            }
        }
    }

    public String getPath() {
        String path = null;
        if ( messageHeader != null ) {
            Pattern r = Pattern.compile("^[a-z]+\\s(/.*)HTTP/\\d+[\\d.]+$",Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
            Matcher m = r.matcher(messageHeader);
            if (m.find()) {
                path = m.group(1);
            }
        }
        return path;
    }

    public String getUrl() {
        String url = null;
        if ( httpUrl != null ) {
            url = httpUrl.toString();
        }
        else {
            if ( messageHeader != null ) {
                String urlPath = getPath();
                String hostHeader = getHeaderValue("host");
                if ( urlPath != null && hostHeader != null ) {
                    url = String.format("%s://%s%s", isSslEnabled() ? "https" : "http",hostHeader,urlPath );

                }
            }
        }
        return url;
    }

    public void filterHeaders( String filterRe ) {
        // Remove the url from the path if it is present
        Pattern r = Pattern.compile("^[a-z]+\\s(.*)\\sHTTP/\\d+[\\d.]+$",Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
        Matcher m = r.matcher(messageHeader);
        if (m.find()) {
            try {
                URL testUrl = new URL(m.group(1));
                String cleanPath = String.format(
                        "%s%s",
                        testUrl.getPath() != null ? testUrl.getPath() : "/",
                        testUrl.getQuery() != null ? testUrl.getQuery(): ""
                );
                messageHeader = String.format("%s%s%s", messageHeader.substring(0,m.start(1)),cleanPath,messageHeader.substring(m.end(1)));

            } catch (MalformedURLException e) {
                // TODO
            }
        }

        // Rebuild the header
        StringBuilder sb = new StringBuilder();
        for ( String curHeader: messageHeader.split("\r\n")) {
            if ( curHeader.matches(filterRe)) {
                continue;
            }
            sb.append(String.format("%s\r\n",curHeader));
        }
        messageHeader = sb.toString();
    }

    /*
        Removes a header value but leaves the header name
        Sec-WebSocket-Extensions: permessage-deflate; client_max_window_bits; server_max_window_bits=10, permessage-deflate; cient_max_window_bits

     */
    private void filterHeaderValue ( String filterValue ) {

    }

    private String getMessageHeader( byte[] buff ) {
        String header = null;
        int headerEndPos = HttpUtil.getMessageHeaderEndPos(buff);
        if ( headerEndPos > 0 ) {
            byte[] headerBytes = new byte[headerEndPos];
            System.arraycopy(buff,0,headerBytes,0,headerEndPos);
            header = new String(headerBytes);
        }
        return header;
    }

    private byte[] parseMessageBody(byte[] buff) {
        int headerEndPos = HttpUtil.getMessageHeaderEndPos(buff);
        if ( headerEndPos > 0 ) {
            if ( headerEndPos + 2 < buff.length ) {
                messageBody = new byte[buff.length-(headerEndPos + 2)];
                System.arraycopy(buff,headerEndPos + 2,messageBody,0,buff.length-(headerEndPos + 2));
            }
        }
        return messageBody;
    }

    public void responseFromParams (int statusCode, String statusMessage, String[] headers, byte[] body, String protocol, boolean allowCache ) throws HttpMessageParseException {
        if ( body == null ) {
            body = new byte[0];
        }
        if ( headers == null ) {
            headers = new String[] {};
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("HTTP/%s %d %s\r\n",protocol,statusCode,statusMessage!=null ? statusMessage : ""));
        if ( headers.length > 0 ) {
            for ( String header: headers ) {
                sb.append(String.format("%s\r\n",header));
            }
        }
        if (!allowCache) {
            sb.append("Cache-Control: no-store, max-age=0\r\n");
        }
        sb.append("\r\n");
        String headerStr = sb.toString();
        byte[] reqbuff = new byte[headerStr.getBytes().length + body.length ];
        System.arraycopy(headerStr.getBytes(), 0, reqbuff,0,headerStr.getBytes().length);
        System.arraycopy(body, 0, reqbuff,headerStr.getBytes().length,body.length);
        fromBytes(reqbuff);
    }

    public void requestFromParams (String method, String url, String[] headers, byte[] body, String protocol ) throws HttpMessageParseException {
        if ( body == null ) {
            body = new byte[0];
        }
        StringBuilder sb = new StringBuilder();
        String headerStr = String.format("%s %s HTTP/%s\r\n%s\r\n",method, url, protocol,String.join("\r\n",headers));
        byte[] reqbuff = new byte[headerStr.getBytes().length + body.length ];
        System.arraycopy(headerStr.getBytes(), 0, reqbuff,0,headerStr.getBytes().length);
        System.arraycopy(body, 0, reqbuff,headerStr.getBytes().length,body.length);
        fromBytes(reqbuff);
    }

    public void fromParts( byte[] header, byte[] body ) throws HttpMessageParseException {
        byte[] buff = new byte[header.length + ( body != null ? body.length : 0)];
        System.arraycopy(header,0,buff,0,header.length);
        if ( body != null ) {
            System.arraycopy(body,0,buff,header.length,body.length);
        }
        fromBytes(buff);
    }

    public void fromBytes( byte[] buff ) throws HttpMessageParseException {
        String header = getMessageHeader(buff);
        if ( header != null ) {
            rawBytes = buff;
            messageHeader = header;
            messageBody = parseMessageBody(buff);
            parseContentLength();
            parseProtocol();
            parseStatusCode();
            parseStatusMessage();
            parseMethod();
            parseHttpUrl();
            if ( httpUrl != null ) {
                if ( httpUrl.toString().toLowerCase().startsWith("https")) {
                    setSslEnabled(true);
                }
            }
        }
        else {
            throw new HttpMessageParseException("Could not extract message header");
        }
    }


    @Override
    public String toString() {
        return "HttpMessage{" +
                "rawBytes=" + Arrays.toString(rawBytes) +
                ", messageHeader='" + messageHeader + '\'' +
                ", messageBody=" + Arrays.toString(messageBody) +
                ", httpUrl=" + httpUrl +
                ", statusMessage='" + statusMessage + '\'' +
                ", statusCode=" + statusCode +
                ", httpMethod='" + httpMethod + '\'' +
                ", protocol='" + protocol + '\'' +
                ", contentLength=" + contentLength +
                ", bodyChunks=" + bodyChunks +
                ", messageUUID='" + messageUUID + '\'' +
                ", createTime=" + createTime +
                ", sslEnabled=" + sslEnabled +
                '}';
    }

    public byte[] getRawBytes() {
        return rawBytes;
    }

    public String getMessageHeader() {
        return messageHeader;
    }

    public byte[] getMessageBody() {
        return messageBody;
    }

    public URL getHttpUrl() {
        return httpUrl;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getProtocol() {
        return protocol;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setMessageBody(byte[] messageBody) {
        this.messageBody = messageBody;
    }

    public ArrayList<byte[]> getBodyChunks() {
        return bodyChunks;
    }

    public void setBodyChunks(ArrayList<byte[]> bodyChunks) {
        this.bodyChunks = bodyChunks;
    }

    public String getMessageUUID() {
        return messageUUID;
    }
    public void setMessageUUID(String messageUUID) {
        this.messageUUID = messageUUID;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public String extract( String regex, int matchGroup ) {
        String matchText = null;
        if ( getBodyBytes() != null ) {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(new String(getBodyBytes()));
            if ( m.find() ) {
                if ( m.groupCount() >= matchGroup ) {
                    matchText = m.group(matchGroup);
                }
            }
        }
        return matchText;
    }
}
