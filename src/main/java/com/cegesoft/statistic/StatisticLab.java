package com.cegesoft.statistic;

import com.cegesoft.Main;
import com.cegesoft.data.FileStorage;
import com.cegesoft.data.Storage;
import com.cegesoft.game.BoardPosition;
import com.cegesoft.game.BoardSimulation;
import com.cegesoft.game.BoardStructure;
import com.cegesoft.simulation.implementation.MeasurementJobHandler;

import java.io.IOException;

public class StatisticLab {

    public static final String FILE_NAME = "statistic.pos";
    public static void main(String[] args) throws InterruptedException, IOException {
        Main.initialise();
        FileStorage fileStorage = new FileStorage(FILE_NAME, 128);
        Storage storage = fileStorage.read();
        BoardPosition[] positions = new BoardPosition[10];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = storage.getDataGroup(BoardPosition.class, i);
        }

        for (int i = 0; i < StatisticManager.NORM_ANGLE_SHAPE[0]; i++) {
            for (int j = 0; j < StatisticManager.NORM_ANGLE_SHAPE[1]; j++) {
                BoardSimulation.NORM_PARTITION = 20 + i * 20;
                BoardSimulation.ANGLE_PARTITION = 180 + j * 114; // 180 -> 1206
                MeasurementJobHandler handler = new MeasurementJobHandler(positions, i, j);
                handler.start();
                handler.join();
                System.out.println("Finished " + i + " " + j);
            }
        }
        System.out.println("Finished");
        System.out.println("NICE_PLAY_LOSS :");
        System.out.println(StatisticManager.getOrCreateStatistic(StatisticManager.StatisticTag.NICE_PLAY_LOSS).saveToNumpy());
        System.out.println();
        System.out.println("BAD_PLAY_LOSS :");
        System.out.println(StatisticManager.getOrCreateStatistic(StatisticManager.StatisticTag.BAD_PLAY_LOSS).saveToNumpy());
        System.out.println();
        System.out.println("EQUAL_PLAY_LOSS :");
        System.out.println(StatisticManager.getOrCreateStatistic(StatisticManager.StatisticTag.EQUAL_PLAY_LOSS).saveToNumpy());
        System.out.println();
        System.out.println("COMPUTATION_TIME :");
        System.out.println(StatisticManager.getOrCreateStatistic(StatisticManager.StatisticTag.COMPUTATION_TIME).saveToNumpy());
        System.out.println();
    }

}
