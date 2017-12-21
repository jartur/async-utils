package com.jartur.asyncutils.downlock;

import java.util.concurrent.atomic.AtomicInteger;

public class DownlockSequentialFailuresStrategy implements DownlockStrategy {
    private final AtomicInteger failures = new AtomicInteger();
    private final int threshold;

    public DownlockSequentialFailuresStrategy(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean success() {
        failures.set(0);
        return false;
    }

    @Override
    public boolean failure() {
        return failures.incrementAndGet() >= threshold;
    }

    @Override
    public boolean isDown() {
        return failures.get() >= threshold;
    }
}
