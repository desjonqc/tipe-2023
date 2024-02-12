package com.cegesoft.util.weighting;

import lombok.AllArgsConstructor;

/**
 * Cas particulier de ScoreWeighting dans lequel les poids sont tous constants.
 * @see ScoreWeighting
 */
@AllArgsConstructor
public class ConstantScoreWeighting implements ScoreWeighting {

    private final int[] goodWeights;
    private final int[] equalWeights;
    private final int[] badWeights;
    private final boolean random;
    @Override
    public int[] getWeighting(float score) {
        if (score > 0) {
            return goodWeights;
        }
        if (score == 0) {
            return equalWeights;
        }
        return badWeights;
    }

    @Override
    public boolean isRandom() {
        return random;
    }
}
