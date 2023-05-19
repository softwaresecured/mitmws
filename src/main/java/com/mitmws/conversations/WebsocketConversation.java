package com.mitmws.conversations;

import com.mitmws.httpproxy.websocket.WebsocketException;
import com.mitmws.httpproxy.websocket.WebsocketFrame;
import com.mitmws.tester.RawWebsocketFrame;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public final class WebsocketConversation {

    public static void writeFrame (Socket dstSocket, WebsocketFrame frame ) throws WebsocketException {
        try {
            dstSocket.getOutputStream().write(frame.toBytes());
            dstSocket.getOutputStream().flush();
        } catch (IOException e) {
            throw new WebsocketException(String.format("Error sending %s frame - %s", frame.getOpcode().toString(),e.getMessage()));
        }
    }

    public static void writeRawFrame (Socket dstSocket, RawWebsocketFrame frame ) throws WebsocketException {
        sendSocketData(dstSocket,frame.toBytes());
    }

    public static void sendSocketData(Socket dstSocket, byte buff[] ) throws WebsocketException {
        try {
            dstSocket.getOutputStream().write(buff);
            dstSocket.getOutputStream().flush();
        } catch (IOException e) {
            throw new WebsocketException(e.getMessage());
        }
    }

    public static byte[] readBlock ( Socket srcSocket, int readLen ) throws IOException {
        byte[] block = new byte[readLen];
        int bytesRead = 0;
        int offset = 0;
        int totalBytesRead = 0;
        do {
            bytesRead = srcSocket.getInputStream().read(block,offset, block.length-offset);
            if ( bytesRead > 0 ) {
                totalBytesRead += bytesRead;
                if ( bytesRead > 0 ) {
                    offset += bytesRead;
                }
            }
        } while ( bytesRead > 0 && bytesRead < block.length );
        byte ret[] = new byte[totalBytesRead];
        if ( totalBytesRead > 0 ) {
            System.arraycopy(block,0,ret,0,totalBytesRead);
        }
        return ret;
    }


    public static WebsocketFrame readFrame( Socket srcSocket ) throws WebsocketException {
        WebsocketFrame frame = new WebsocketFrame();
        try {
            byte[] block1 = readBlock(srcSocket,1);
            if ( block1 == null ) {
                return null;
            }
            if (!frame.parseBlock1(block1)) {
                //throw new WebsocketException("Error parsing block1 FLAGS/OPCODE");
                return null;
            }

            byte[] block2 = readBlock(srcSocket,1);
            if ( block2 == null || !frame.parseBlock2(block2)) {
                throw new WebsocketException("Error parsing block2 MASK/PAYLOADLEN");
            }

            // Extended payload len?
            if ( frame.getPayloadLength() > 125 ) {
                byte[] payloadLenReadBuff = null;
                switch(frame.getPayloadLength()) {
                    case 126:
                        payloadLenReadBuff = readBlock(srcSocket,2);
                        break;
                    case 127:
                        payloadLenReadBuff = readBlock(srcSocket,4);
                        break;
                    default:
                        throw new WebsocketException("Error parsing block3 EXTENDED PAYLOADLEN");
                }
                if ( payloadLenReadBuff == null || !frame.parseBlock3(payloadLenReadBuff) ) {
                    throw new WebsocketException("Error parsing block3 EXTENDED PAYLOADLEN - but weird");
                }
            }

            if ( frame.getMasked() == 1 ) {
                byte[] block4 = readBlock(srcSocket,4);
                if ( block4 == null || !frame.parseBlock4(block4) ) {
                    throw new WebsocketException("Error parsing block4 - MASK");
                }
            }

            if ( frame.getPayloadLength() > 0 ) {
                byte[] block5 = readBlock(srcSocket,frame.getPayloadLength());
                if ( !frame.parseBlock5(block5)) {
                    throw new WebsocketException(String.format("Error parsing payload, expected %d bytes", frame.getPayloadLength() ));
                }
            }

        } catch (SocketException e) {
            throw new WebsocketException(e.getMessage());
        } catch ( SocketTimeoutException e ) {
            return null;
        } catch (IOException e) {
            throw new WebsocketException(e.getMessage());
        }
        return frame;
    }
}
