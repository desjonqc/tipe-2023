package com.cegesoft.statistic;

import java.util.concurrent.ConcurrentHashMap;

public class StatisticManager {

    public static final int[] NORM_ANGLE_SHAPE = new int[] { 10, 5 };
    private static final ConcurrentHashMap<StatisticTag, Statistic> statistics = new ConcurrentHashMap<>();

    public static Statistic getOrCreateStatistic(StatisticTag tag) {
        if (!statistics.containsKey(tag)) {
            statistics.put(tag, new Statistic(tag.shape));
        }
        return statistics.get(tag);
    }

    public enum StatisticTag {
        NICE_PLAY_LOSS(NORM_ANGLE_SHAPE),
        BAD_PLAY_LOSS(NORM_ANGLE_SHAPE),
        EQUAL_PLAY_LOSS(NORM_ANGLE_SHAPE),
        COMPUTATION_TIME(NORM_ANGLE_SHAPE);

        private final int[] shape;

        StatisticTag(int[] shape) {
            this.shape = shape;
        }
    }

}
