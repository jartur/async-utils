package com.jartur.asyncutils.sequencer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

public class SynchronizedAsyncSequencerTest {
    @Test
    public void testSequentiality() throws ExecutionException, InterruptedException {
        SynchronizedAsyncSequencer<String> sequencer = new SynchronizedAsyncSequencer<>();
        LinkedBlockingQueue<Integer> journal = new LinkedBlockingQueue<>();
        CompletableFuture<Void> waitingForCommit = sequencer.borrow("test")
                .thenAccept(_x -> journal.add(1))
                .thenCompose(_x -> sequencer.borrow("test").thenAccept(_x1 -> journal.add(3)).thenAccept(_x1 -> sequencer.commit("test")))
                .thenCompose(_x -> sequencer.borrow("test").thenAccept(_x1 -> journal.add(4)).thenAccept(_x1 -> sequencer.commit("test")));
        sequencer.borrow("test2").thenAccept(_x1 -> journal.add(2)).thenAccept(_x1 -> sequencer.commit("test2"));
        sequencer.commit("test");
        waitingForCommit.join();
        sequencer.borrow("test")
                .thenAccept(x -> journal.add(5))
                .join();
        CompletableFuture<Void> lastF = sequencer.borrow("test")
                .thenAccept(x -> journal.add(6));
        Assertions.assertFalse(lastF.isDone());
        boolean timedOut = false;
        try {
            lastF.get(1, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            timedOut = true;
        }
        Assertions.assertTrue(timedOut);
        Assertions.assertArrayEquals(new int[]{1, 2, 3, 4, 5}, journal.stream().mapToInt(x -> x).toArray());
        sequencer.commit("test");
        lastF.join();
        Assertions.assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6}, journal.stream().mapToInt(x -> x).toArray());
    }
}
