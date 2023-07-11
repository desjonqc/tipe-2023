package com.cegesoft.simulation;

import com.cegesoft.game.exception.BoardParsingException;
import com.cegesoft.game.position.BoardPosition;
import com.cegesoft.game.position.FullPosition;
import com.cegesoft.util.weighting.ScoreWeighting;

import java.util.List;

public interface IJobExecutable {

    void run(int index);

    void reset();

    /**
     * Returns the results for the given score
     * @param score 1 for croissant win, 0 for draw, -1 for croissant loss
     * @return the results for the given score
     */
    List<Integer> getResults(int score);

    BoardPosition getBoardPosition(int resultIndex) throws BoardParsingException;

    FullPosition getCurrentEvaluation(ScoreWeighting weighting);

}
