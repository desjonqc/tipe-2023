package com.cegesoft.statistic;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Classe permettant de gérer les statistiques
 */
public class StatisticManager {

    public static int[] NORM_ANGLE_SHAPE = new int[] { 10, 5 };
    private static final ConcurrentHashMap<StatisticTag, Statistic> statistics = new ConcurrentHashMap<>();

    /**
     * Récupère ou crée (si elle n'existe pas) une statistique à partir d'un tag
     * @param tag Tag de la statistique
     * @return La statistique
     */
    public static Statistic getOrCreateStatistic(StatisticTag tag) {
        if (!statistics.containsKey(tag)) {
            statistics.put(tag, new Statistic(tag.shape));
        }
        return statistics.get(tag);
    }

    /**
     * Déclaration des tags de statistiques
     */
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
