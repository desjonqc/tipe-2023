package com.cegesoft.simulation.implementation;

import com.cegesoft.Main;
import com.cegesoft.game.BoardPosition;
import com.cegesoft.game.BoardSimulation;
import com.cegesoft.simulation.Job;
import com.cegesoft.simulation.MultipleJobHandler;
import com.cegesoft.statistic.Statistic;
import com.cegesoft.statistic.StatisticManager;

public class MeasurementJobHandler extends MultipleJobHandler {

    private long start;
    private final BoardPosition[] initialPositions;
    private final int norm_size;
    private final int angle_size;

    public MeasurementJobHandler(BoardPosition[] initialPositions, int norm_size, int angle_size) {
        this.initialPositions = initialPositions;
        this.norm_size = norm_size;
        this.angle_size = angle_size;
    }

    @Override
    public Job[] createJobs() {
        start = System.currentTimeMillis();
        Job[] jobs = new Job[this.initialPositions.length];
        for (int i = 0; i < this.initialPositions.length; i++) {
            jobs[i] = new Job(new BoardSimulation(Main.BOARD_CONFIGURATION, this.initialPositions[i]), BoardSimulation.SIMULATION_TIME);
        }
        return jobs;
    }

    @Override
    public void handleResults() {
        long time = System.currentTimeMillis() - start;
        int[] currentIndices = new int[] {this.norm_size, this.angle_size};
        StatisticManager.getOrCreateStatistic(StatisticManager.StatisticTag.COMPUTATION_TIME).getOrCreateValue(currentIndices).addValue(time);
        Statistic.Value nicePlayValue = StatisticManager.getOrCreateStatistic(StatisticManager.StatisticTag.NICE_PLAY_LOSS).getOrCreateValue(currentIndices);
        Statistic.Value equalPlayValue = StatisticManager.getOrCreateStatistic(StatisticManager.StatisticTag.EQUAL_PLAY_LOSS).getOrCreateValue(currentIndices);
        Statistic.Value badPlayValue = StatisticManager.getOrCreateStatistic(StatisticManager.StatisticTag.BAD_PLAY_LOSS).getOrCreateValue(currentIndices);
        for (Job job : this.jobs) {
            nicePlayValue.addValue(job.getExecutable().getResults(1).size());
            equalPlayValue.addValue(job.getExecutable().getResults(0).size());
            badPlayValue.addValue(job.getExecutable().getResults(-1).size());
        }
    }
}
