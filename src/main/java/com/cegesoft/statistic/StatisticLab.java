package com.cegesoft.statistic;

import com.cegesoft.Main;
import com.cegesoft.data.FileStorage;
import com.cegesoft.data.Storage;
import com.cegesoft.game.BoardPosition;
import com.cegesoft.game.BoardSimulation;
import com.cegesoft.simulation.implementation.MeasurementJobHandler;

import java.io.IOException;

public class StatisticLab {

    public static String FILE_NAME = "statistic-2.pos";
    public static void main(String[] args) throws InterruptedException, IOException {
        if (args.length == 0) {
            throw new IllegalArgumentException("Please provide a simulation id");
        }
        int simulationId = Integer.parseInt(args[0]);
        int[] coefficients = new int[] {180, 205, 20, 20};
        if (args.length > 1) {
            String[] coeff_str = args[1].split(",");
            coefficients = new int[] {Integer.parseInt(coeff_str[0]), Integer.parseInt(coeff_str[1]), Integer.parseInt(coeff_str[2]), Integer.parseInt(coeff_str[3])};
        }
        if (args.length > 2) {
            FILE_NAME = args[2];
        }
        Main.initialise();
        FileStorage fileStorage = new FileStorage(FILE_NAME, 128);
        Storage storage = fileStorage.read();
        BoardPosition[] positions = new BoardPosition[10];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = storage.getDataGroup(BoardPosition.class, i);
        }

        for (int i = 0; i < StatisticManager.NORM_ANGLE_SHAPE[0]; i++) {
            for (int j = 0; j < StatisticManager.NORM_ANGLE_SHAPE[1]; j++) {
                BoardSimulation.NORM_PARTITION = coefficients[2] + i * coefficients[3];
                BoardSimulation.ANGLE_PARTITION = coefficients[0] + j * coefficients[1]; // 180 -> 1206
                MeasurementJobHandler handler = new MeasurementJobHandler(positions, i, j);
                handler.start();
                handler.join();
                System.out.println("Finished " + i + " " + j);
            }
        }
        System.out.println("Finished");
        StatisticManager.getOrCreateStatistic(StatisticManager.StatisticTag.NICE_PLAY_LOSS).saveFileToNumpy(simulationId, "nice_play_loss", true, coefficients);
        StatisticManager.getOrCreateStatistic(StatisticManager.StatisticTag.BAD_PLAY_LOSS).saveFileToNumpy(simulationId, "bad_play_loss", true, coefficients);
        StatisticManager.getOrCreateStatistic(StatisticManager.StatisticTag.EQUAL_PLAY_LOSS).saveFileToNumpy(simulationId, "equal_play_loss", true, coefficients);
        StatisticManager.getOrCreateStatistic(StatisticManager.StatisticTag.COMPUTATION_TIME).saveFileToNumpy(simulationId, "computation_time", true, coefficients);
    }

}
