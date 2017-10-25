package com.jartur.asyncutils.cache;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AsyncLoadingCache<K, T> implements AsyncCache<K, T> {
    private final CacheLoader<K, T> loader;
    private ConcurrentHashMap<K, CompletableFuture<T>> futures = new ConcurrentHashMap<>();
    private CopyOnWriteArrayList<CacheUpdatedListener<K, T>> listeners = new CopyOnWriteArrayList<>();

    public AsyncLoadingCache(CacheLoader<K,T> loader) {
        this.loader = loader;
    }

	@Override
	public CompletableFuture<T> get(K k) {
        CompletableFuture<?> startFuture = new CompletableFuture<>();
        CompletableFuture<T> loadingFuture = startFuture.thenCompose(_x -> loader.load(k));
        CompletableFuture<T> f = futures.putIfAbsent(k, loadingFuture);
        if(f == null) {
            startFuture.complete(null);
            return loadingFuture;
        }
		return f;
    }

    @Override
    public void invalidate(K k) {
        futures.remove(k);
    }

    @Override
    public void update(K k, T v) {
        futures.put(k, CompletableFuture.completedFuture(v));
        listeners.forEach(l -> l.onUpdated(k, v));
    }

    @Override
    public void subscribe(CacheUpdatedListener<K, T> listener) {
        listeners.add(listener);
    }
}
