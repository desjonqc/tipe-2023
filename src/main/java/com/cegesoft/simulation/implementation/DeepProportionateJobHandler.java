package com.cegesoft.simulation.implementation;

import com.cegesoft.Main;
import com.cegesoft.game.BoardPosition;
import com.cegesoft.game.BoardSimulation;
import com.cegesoft.simulation.IJobExecutable;
import com.cegesoft.simulation.Job;
import com.cegesoft.simulation.MultipleJobHandler;
import com.cegesoft.util.DepthCounter;
import com.cegesoft.util.DepthDependantScoreWeighting;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class DeepProportionateJobHandler extends MultipleJobHandler {

    private final Job lastJob;
    private final DepthCounter counter;
    private final DepthDependantScoreWeighting weighting;

    public DeepProportionateJobHandler(Job lastJob, DepthCounter counter, DepthDependantScoreWeighting weighting) {
        this.lastJob = lastJob;
        this.counter = counter;
        this.weighting = weighting;
    }

    @Override
    public Job[] createJobs() {
        if (counter.isMaxDepth()) {
            return new Job[0];
        }
        if (!lastJob.isDone()) {
            counter.decrement();
            return new Job[] {lastJob};
        }
        List<Integer> goodScores = lastJob.getExecutable().getResults(1);
        List<Integer> equalScores = lastJob.getExecutable().getResults(0);
        List<Integer> badScores = lastJob.getExecutable().getResults(-1);

        int goodScoreAmount = Math.min(goodScores.size(), weighting.getWeighting(1)[counter.getDepth()]);
        int equalScoreAmount = Math.min(equalScores.size(), weighting.getWeighting(0)[counter.getDepth()]);
        int badScoreAmount = Math.min(badScores.size(), weighting.getWeighting(-1)[counter.getDepth()]);
        Job[] jobs = new Job[goodScoreAmount + equalScoreAmount + badScoreAmount];

        Collections.shuffle(equalScores);
        if (weighting.isRandom()) {
            Collections.shuffle(goodScores);
            Collections.shuffle(badScores);
        }

        for (int i = 0; i < goodScoreAmount; i++) {
            jobs[i] = new Job(new BoardSimulation(Main.BOARD_CONFIGURATION, lastJob.getExecutable().getBoardPosition(goodScores.get(i))), BoardSimulation.SIMULATION_TIME);
        }
        for (int i = 0; i < equalScoreAmount; i++) {
            jobs[i + goodScoreAmount] = new Job(new BoardSimulation(Main.BOARD_CONFIGURATION, lastJob.getExecutable().getBoardPosition(equalScores.get(i))), BoardSimulation.SIMULATION_TIME);
        }
        for (int i = 0; i < badScoreAmount; i++) {
            jobs[i + goodScoreAmount + equalScoreAmount] = new Job(new BoardSimulation(Main.BOARD_CONFIGURATION, lastJob.getExecutable().getBoardPosition(badScores.get(i))), BoardSimulation.SIMULATION_TIME);
        }
        System.out.println(jobs.length + " jobs created at depth " + counter.getDepth());
        return jobs;
    }

    @Override
    public void handleResults() {
        System.out.println("End of a part depth " + counter.getDepth());
        System.out.println("Starting " + this.jobs.length + " jobs...");
        for (Job job : this.jobs) {
            DeepProportionateJobHandler jobHandler = new DeepProportionateJobHandler(job, counter.increment(), weighting);
            jobHandler.start();
        }
    }


}
