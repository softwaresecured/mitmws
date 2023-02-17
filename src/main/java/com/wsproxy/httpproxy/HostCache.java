package com.wsproxy.httpproxy;

import java.util.HashMap;

public class HostCache {
    private final int HOST_CACHE_ITEM_LIFETIME_MSEC = 600000;
    private HashMap<String, HostCacheItem> hostCache = new HashMap<String, HostCacheItem>();
    public HostCache() {

    }

    public int size() {
        return hostCache.size();
    }
    public void pruneExpired() {
        if ( hostCache.size() > 0 ) {
            int[] expired = new int[hostCache.size()];
            int pruneCount = 0;
            for ( int i = 0; i < hostCache.size(); i++ ) {
                if ( System.currentTimeMillis()-hostCache.get(i).getUpdated() > HOST_CACHE_ITEM_LIFETIME_MSEC ) {
                    expired[pruneCount] = i;
                    pruneCount += 1;
                }
            }
            for ( int i : expired ) {
                hostCache.remove(i);
            }
        }
    }

    public HostCacheItem get( String host, int port ) {
        HostCacheItem item = hostCache.get(String.format("%s:%s", host,port));
        if ( item != null ) {
            if ( System.currentTimeMillis()-item.getUpdated()>HOST_CACHE_ITEM_LIFETIME_MSEC) {
                item = null;
            }
        }
        return item;
    }

    public void put ( String host, int port, boolean tls ) {
        HostCacheItem item = new HostCacheItem();
        item.setUpdated(System.currentTimeMillis());
        item.setHost(host);
        item.setPort(port);
        item.setTls(tls);
        hostCache.put(String.format("%s:%s", host,port),item);
    }
}
