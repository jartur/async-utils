package com.jartur.asyncutils.cache;

import java.util.concurrent.CompletableFuture;

public interface CacheLoader<K, T> {
    CompletableFuture<T> load(K k);
}
