package com.cegesoft.simulation;

import com.cegesoft.game.BoardPosition;

import java.util.List;

public interface IJobExecutable {

    void run(int index);

    void reset();

    List<Integer> getResults();

    BoardPosition getBoardPosition(int resultIndex);

}
