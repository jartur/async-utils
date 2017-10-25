package com.jartur.asyncutils.cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncLoadingCacheTest {

    @Test
    public void testLoading() throws ExecutionException, InterruptedException {
        Object o = new Object();
        AsyncLoadingCache<String, Object> cache = new AsyncLoadingCache<>(s -> CompletableFuture.completedFuture(o));
        cache.get("test").thenAccept(r -> Assertions.assertEquals(o, r)).join();
    }

    @Test
    public void testCaching() throws ExecutionException, InterruptedException {
        AsyncLoadingCache<String, Object> cache = new AsyncLoadingCache<>(s -> CompletableFuture.completedFuture(new Object()));
        cache.get("test").thenCompose(r -> cache.get("test").thenAccept(r2 -> Assertions.assertEquals(r, r2))).join();
    }

    @Test
    public void testDiscrimination() throws ExecutionException, InterruptedException {
        AsyncLoadingCache<String, Object> cache = new AsyncLoadingCache<>(s -> CompletableFuture.completedFuture(new Object()));
        cache.get("test").thenCompose(r -> cache.get("test2").thenAccept(r2 -> Assertions.assertNotEquals(r, r2))).join();
    }

    @Test
    public void testNoDoubleLoading() throws ExecutionException, InterruptedException {
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        AtomicInteger loads = new AtomicInteger(0);
        AsyncLoadingCache<String, Object> cache = new AsyncLoadingCache<>(s -> {
            loads.incrementAndGet();
            CompletableFuture<Object> f = new CompletableFuture<>();
            ses.schedule(() -> {
                f.complete(new Object());
            }, 1, TimeUnit.SECONDS);
            return f;
        });
        CompletableFuture.allOf(cache.get("test"), cache.get("test")).join();
        Assertions.assertEquals(1, loads.get());
    }

    @Test
    public void testInvalidation() throws ExecutionException, InterruptedException {
        AsyncLoadingCache<String, Object> cache = new AsyncLoadingCache<>(s -> CompletableFuture.completedFuture(new Object()));
        cache.get("test").thenCompose(r -> {
            cache.invalidate("test");
            return cache.get("test").thenAccept(r2 -> Assertions.assertNotEquals(r, r2));
        }).join();
    }

    @Test
    public void testUpdate() throws ExecutionException, InterruptedException {
        AsyncLoadingCache<String, Object> cache = new AsyncLoadingCache<>(s -> CompletableFuture.completedFuture(new Object()));
        cache.get("test").thenCompose(r -> {
            Object updated = new Object();
            cache.update("test", updated);
            return cache.get("test").thenAccept(r2 -> Assertions.assertEquals(updated, r2));
        }).join();
    }

    @Test
    public void testUpdateListenerNotification() throws ExecutionException, InterruptedException {
        AsyncLoadingCache<String, Object> cache = new AsyncLoadingCache<>(s -> CompletableFuture.completedFuture(new Object()));
        AtomicInteger listenersInvoked = new AtomicInteger();
        cache.subscribe((s, o) -> listenersInvoked.incrementAndGet());
        cache.subscribe((s, o) -> listenersInvoked.incrementAndGet());
        cache.get("test").thenAccept(r -> {
            cache.update("test", new Object());
        }).join();
        Assertions.assertEquals(2, listenersInvoked.get());
    }
}
