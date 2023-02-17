package com.wsproxy.httpproxy.websocket.extensions;

import com.wsproxy.httpproxy.HttpMessage;
import com.wsproxy.httpproxy.websocket.WebsocketFrame;

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class PerMessageDeflateExtension implements WebsocketExtension {
    private boolean enabled = false;
    public PerMessageDeflateExtension() {

    }

    @Override
    public void init(HttpMessage req, HttpMessage response) {
        String extHeaderValue = response.getHeaderValue("sec-websocket-extensions");
        if ( extHeaderValue != null ) {
            // TODO client_max_window_bits https://www.rfc-editor.org/rfc/rfc7692.html
            if ( extHeaderValue.matches(".*permessage-deflate.*")) {
                enabled = true;
            }
        }
    }

    @Override
    public WebsocketFrame processWebsocketFrameIn(WebsocketFrame frame) {
        if ( enabled ) {
            if ( frame.getPayload() != null ) {
                try {
                    frame.setPayload(decompress(frame.getPayload()));
                } catch (DataFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return frame;
    }

    @Override
    public WebsocketFrame processWebsocketFrameOut(WebsocketFrame frame) {
        if ( enabled ) {
            if ( frame.getPayload() != null ) {
                frame.setPayload(compress(frame.getPayload()));
            }
        }
        return frame;
    }

    @Override
    public WebsocketFrame processWebsocketFrame(WebsocketFrame frame) {
        return frame;
    }

    private byte[] decompress( byte buff[] ) throws DataFormatException {
        byte output[] = null;
        Inflater i = new Inflater(true);
        i.setInput(buff);
        byte tmpOutput[] = new byte[buff.length];
        int size = i.inflate(tmpOutput);
        if ( size > 0 ) {
            output = new byte[size];
            System.arraycopy(tmpOutput,0,output,0,size);
        }
        return output;
    }

    private byte[] compress ( byte buff[] ) {
        byte output[] = null;
        Deflater d = new Deflater(Deflater.DEFLATED, true);
        d.setInput(buff);
        d.finish();

        byte tmpOutput[] = new byte[buff.length];
        int size = d.deflate(tmpOutput);
        if ( size > 0 ) {
            output = new byte[size];
            System.arraycopy(tmpOutput,0,output,0,size);
        }
        return output;
    }
}
