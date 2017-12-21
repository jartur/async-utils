package com.jartur.asyncutils.downlock;

public interface DownlockStrategy {
    boolean success();
    boolean failure();
    boolean isDown();
}
