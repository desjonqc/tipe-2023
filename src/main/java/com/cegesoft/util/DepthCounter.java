package com.cegesoft.util;

import lombok.Getter;

public class DepthCounter {

    @Getter
    private int depth;
    @Getter
    private final int maxDepth;

    public DepthCounter(int maxDepth, int depth) {
        this.maxDepth = maxDepth;
        this.depth = depth;
    }

    public DepthCounter(int maxDepth) {
        this(maxDepth, 0);
    }

    public DepthCounter increment() {
        if (depth < maxDepth) {
            return new DepthCounter(maxDepth, depth + 1);
        }
        return this;
    }

    public void decrement() {
        depth--;
    }

    public boolean isMaxDepth() {
        return depth == maxDepth;
    }

}
