package com.jartur.asyncutils.cache;

public interface CacheUpdatedListener<K, T> {
    void onUpdated(K k, T t);
}
