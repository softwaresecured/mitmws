package com.mitmws.util;

import com.mitmws.httpproxy.HttpMessage;
import com.mitmws.httpproxy.trafficlogger.WebsocketDirection;
import com.mitmws.httpproxy.websocket.WebsocketFrame;
import com.mitmws.httpproxy.websocket.extensions.DummyExtension;
import com.mitmws.httpproxy.websocket.extensions.PerMessageDeflateExtension;
import com.mitmws.httpproxy.websocket.extensions.WebsocketExtension;

import java.util.ArrayList;

public final class ExtensionUtil {
    public static ArrayList<WebsocketExtension> initExtensions (HttpMessage request, HttpMessage response ) {
        ArrayList<WebsocketExtension> extensions = new ArrayList<WebsocketExtension>();
        extensions.add(new DummyExtension());
        extensions.add(new PerMessageDeflateExtension());
        for ( WebsocketExtension extension : extensions ) {
            extension.init(request,response);
        }
        return extensions;
    }
    public static WebsocketFrame processExtensions (ArrayList<WebsocketExtension> extensions, WebsocketFrame frame, WebsocketDirection direction) {
        for ( WebsocketExtension extension : extensions ) {
            if ( direction.equals(WebsocketDirection.INBOUND)) {
                frame = extension.processWebsocketFrameIn(frame);
            }
            else {
                frame = extension.processWebsocketFrameOut(frame);
            }
        }
        return frame;
    }
}
