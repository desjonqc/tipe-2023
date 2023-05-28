package com.cegesoft.simulation.implementation;

import com.cegesoft.game.BoardPosition;
import com.cegesoft.simulation.Job;
import com.cegesoft.simulation.MultipleJobHandler;

import java.util.function.Function;

public class DeepProportionateJobHandler extends MultipleJobHandler {

    public DeepProportionateJobHandler(BoardPosition initialPosition, Function<Integer, Float> weighting, int jobHandlerId) {
        super(jobHandlerId);
    }

    @Override
    public Job[] createJobs() {
        return new Job[0];
    }

    @Override
    public void handleResults() {

    }
}
