package com.wsproxy.util;

import com.wsproxy.httpproxy.HttpMessage;
import com.wsproxy.httpproxy.trafficlogger.WebsocketDirection;
import com.wsproxy.httpproxy.websocket.WebsocketFrame;
import com.wsproxy.httpproxy.websocket.extensions.DummyExtension;
import com.wsproxy.httpproxy.websocket.extensions.PerMessageDeflateExtension;
import com.wsproxy.httpproxy.websocket.extensions.WebsocketExtension;

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
