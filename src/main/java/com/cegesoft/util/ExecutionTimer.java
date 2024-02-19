package com.cegesoft.util;

public class ExecutionTimer {

    private long timer = 0;
    private long lastRecord = 0;

    public void record() {
        lastRecord = System.nanoTime();
    }

    public void stopRecord() {
        timer += System.nanoTime() - lastRecord;
    }

    public long getTotalTime() {
        return timer;
    }

    public void reset() {
        timer = 0;
    }

}
