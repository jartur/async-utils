package com.jartur.asyncutils.downlock;

public interface Downlock {
    boolean success();
    boolean failure();
    boolean isDown();
}
