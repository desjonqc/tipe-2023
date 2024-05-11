package com.cegesoft.simulation;

import com.cegesoft.game.exception.BoardParsingException;
import com.cegesoft.game.position.BoardPosition;
import com.cegesoft.game.position.FullPosition;
import com.cegesoft.util.weighting.ScoreWeighting;

import java.util.List;

/**
 * Tâche exécutable par un JobHandler
 */
public interface IJobExecutable {

    /**
     * Exécute la tâche pour l'itération donnée
     * @param index l'itération
     */
    void run(int index);

    /**
     * Réinitialise la tâche
     */
    void reset();

    /**
     * Récupère les résultats pour un score donné
     * @param score 1 pour une vitoire (ordonnée par score croissant), 0 pour une égalité, -1 pour une défaite (ordonnée par score croissant)
     * @return Les indices des résultats
     */
    List<Integer> getResults(int score);

    /**
     * Récupère la position du plateau pour un résultat donné
     * @param resultIndex l'indice du résultat
     * @return La position du plateau
     * @throws BoardParsingException si la position du plateau ne peut pas être récupérée
     */
    BoardPosition getBoardPosition(int resultIndex) throws BoardParsingException;

    /**
     * Récupère la position complète pour un résultat donné
     * @param weighting la distribution des bons et mauvais coups
     * @return La position complète
     */
    FullPosition getCurrentEvaluation(ScoreWeighting weighting);

}
