package com.jartur.asyncutils.sequencer;

import java.util.concurrent.CompletableFuture;

interface AsyncSequencer<K> {
    CompletableFuture<K> borrow(K k);
    void commit(K k);
}
