package com.jartur.asyncutils.sequencer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

class SynchronizedAsyncSequencer<K> implements AsyncSequencer<K> {
    private final Map<K, Queue<CompletableFuture<K>>> borrows = new HashMap<>();
    private final ExecutorService executorService;

    SynchronizedAsyncSequencer() {
        executorService = ForkJoinPool.commonPool();
    }

    public SynchronizedAsyncSequencer(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public synchronized CompletableFuture<K> borrow(K k) {
        CompletableFuture<K> future = new CompletableFuture<>();
        Queue<CompletableFuture<K>> queue = borrows.get(k);
        if(queue == null) {
            future.complete(k);
            borrows.put(k, new LinkedList<>());
            return future;
        }
        queue.add(future);
        return future;
    }

    @Override
    public synchronized void commit(K k) {
        Queue<CompletableFuture<K>> queue = borrows.get(k);
        if(queue != null) {
            CompletableFuture<K> nextFuture = queue.poll();
            if(nextFuture != null) {
                executorService.execute(() -> nextFuture.complete(k));
            } else {
                borrows.remove(k);
            }
        }
    }
}
