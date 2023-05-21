package com.mitmws.analyzer;

public class CacheItem {
    private long cacheTime = 0;
    private String value;

    public CacheItem(long cacheTime, String value) {
        this.cacheTime = cacheTime;
        this.value = value;
    }

    public long getCacheTime() {
        return cacheTime;
    }

    public String getValue() {
        return value;
    }
}
