package com.cegesoft.simulation.implementation;

import com.cegesoft.Main;
import com.cegesoft.app.property.Property;
import com.cegesoft.game.SimulationInformation;
import com.cegesoft.game.position.BoardPosition;
import com.cegesoft.game.BoardSimulation;
import com.cegesoft.log.Logger;
import com.cegesoft.simulation.Job;
import com.cegesoft.simulation.MultipleJobHandler;
import com.cegesoft.statistic.Statistic;
import com.cegesoft.statistic.StatisticManager;
import com.cegesoft.util.exception.IndiceDimensionException;

/**
 * Tâche de mesure statistique, pour comparer les différentes partitions [normes, angles]
 */
public class MeasurementJobHandler extends MultipleJobHandler {

    private long start;
    private final BoardPosition[] initialPositions;
    private final int norm_size;
    private final int angle_size;
    private final SimulationInformation information;

    public MeasurementJobHandler(BoardPosition[] initialPositions, int norm_size, int angle_size, SimulationInformation information) {
        this.initialPositions = initialPositions;
        this.norm_size = norm_size;
        this.angle_size = angle_size;
        this.information = information;
    }

    @Override
    public Job[] createJobs() {
        start = System.currentTimeMillis();
        Job[] jobs = new Job[this.initialPositions.length];
        for (int i = 0; i < this.initialPositions.length; i++) {
            jobs[i] = new Job(new BoardSimulation(Main.getTProperty(Property.BOARD_CONFIGURATION), this.initialPositions[i], information), Main.getIntProperty(Property.SIMULATION_TIME));
        }
        return jobs;
    }

    @Override
    public void handleResults() {
        long time = System.currentTimeMillis() - start;
        int[] currentIndices = new int[] {this.norm_size, this.angle_size};
        try {
            StatisticManager.getOrCreateStatistic(StatisticManager.StatisticTag.COMPUTATION_TIME).getOrCreateValue(currentIndices).addValue(time);
            Statistic.Value nicePlayValue = StatisticManager.getOrCreateStatistic(StatisticManager.StatisticTag.NICE_PLAY_LOSS).getOrCreateValue(currentIndices);
            Statistic.Value equalPlayValue = StatisticManager.getOrCreateStatistic(StatisticManager.StatisticTag.EQUAL_PLAY_LOSS).getOrCreateValue(currentIndices);
            Statistic.Value badPlayValue = StatisticManager.getOrCreateStatistic(StatisticManager.StatisticTag.BAD_PLAY_LOSS).getOrCreateValue(currentIndices);

            for (Job job : this.jobs) {
                nicePlayValue.addValue(job.getExecutable().getResults(1).size());
                equalPlayValue.addValue(job.getExecutable().getResults(0).size());
                badPlayValue.addValue(job.getExecutable().getResults(-1).size());
            }
        } catch (IndiceDimensionException e) {
            Logger.error("Can't perform statistics : ", e);
        }
    }
}
