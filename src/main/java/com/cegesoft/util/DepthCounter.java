package com.cegesoft.util;

import lombok.Getter;

/**
 * Compte le nombre de fois que l'on est descendu dans la récursivité.
 */
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

    /**
     * Ajoute 1 au compteur de profondeur si la profondeur n'est pas maximale
     * @return this sous forme de Builder
     */
    public DepthCounter increment() {
        if (depth < maxDepth) {
            return new DepthCounter(maxDepth, depth + 1);
        }
        return this;
    }

    /**
     * Soustrait 1 au compteur de profondeur
     */
    public void decrement() {
        depth--;
    }

    /**
     * @return true si la profondeur actuelle est maximale
     */
    public boolean isMaxDepth() {
        return depth == maxDepth;
    }

}
