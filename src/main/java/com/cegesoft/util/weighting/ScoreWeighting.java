package com.cegesoft.util.weighting;

public interface ScoreWeighting {

    /** Renvoit un tableau contenant pour chaque profondeur le nombre de positions à explorer
     * @param score -1 pour perte, 0 pour nul, 1 pour victoire
     * @return tableau de poids
     */
    int[] getWeighting(float score);

    /**
     * @return true si le choix des positions est aléatoire
     */
    boolean isRandom();

}
