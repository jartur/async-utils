package com.jartur.asyncutils.cache;

import java.util.concurrent.CompletableFuture;

public interface AsyncCache<K, T> {
    CompletableFuture<T> get(K k);
    default void invalidate(K k) {}
    default void update(K k, T v) {}
    default void subscribe(CacheUpdatedListener<K, T> listener) { }
}
