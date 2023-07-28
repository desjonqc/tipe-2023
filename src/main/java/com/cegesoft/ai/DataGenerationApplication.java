package com.cegesoft.ai;

import com.cegesoft.Main;
import com.cegesoft.app.SimulationApplication;
import com.cegesoft.app.argument.ApplicationArgument;
import com.cegesoft.app.property.Property;
import com.cegesoft.data.StorageManager;
import com.cegesoft.data.handlers.FullStorageHandler;
import com.cegesoft.data.metadata.SimulationFileMetadata;
import com.cegesoft.game.Board;
import com.cegesoft.game.BoardSimulation;
import com.cegesoft.log.ConsoleColors;
import com.cegesoft.log.Logger;
import com.cegesoft.simulation.Job;
import com.cegesoft.simulation.implementation.DeepProportionateJobHandler;
import com.cegesoft.util.DepthCounter;
import com.cegesoft.util.weighting.ConstantScoreWeighting;
import com.cegesoft.util.weighting.ScoreWeighting;

import java.util.Random;

public class DataGenerationApplication extends SimulationApplication {

    private int id;
    public DataGenerationApplication() {
        super();
        this.registerArgument(new ApplicationArgument<>(false, "id", -1, "Id of the simulation. -1 is random."));
    }
    @Override
    public void start() throws Exception {
        super.start();
        Logger.info("Starting data generation simulation with id " + ConsoleColors.BLUE + id + ConsoleColors.RESET + ".");
        ScoreWeighting storageWeighting = new ConstantScoreWeighting(new int[]{1}, new int[] {0}, new int[] {0}, false);
        StorageManager.register(StorageManager.StorageTag.AI_DATA, new FullStorageHandler("python/datastored/" + StorageManager.StorageTag.AI_DATA.getName() + "-" + id + "/", "data",
                20, SimulationFileMetadata.class, storageWeighting));

        ScoreWeighting scoreWeighting = new ConstantScoreWeighting(new int[] {4, 4}, new int[] {3, 3}, new int[] {2, 2}, true);
        DeepProportionateJobHandler jobHandler =
                new DeepProportionateJobHandler(
                        new Job(
                                new BoardSimulation(
                                        this.getTProperty(Property.BOARD_CONFIGURATION),
                                        Board.INITIAL_POSITION,
                                        this.getTProperty(Property.SIMULATION_INFORMATION)), this.getIntProperty(Property.SIMULATION_TIME)),
                        new DepthCounter(2),
                        scoreWeighting,
                        this.getTProperty(Property.SIMULATION_INFORMATION),
                        (FullStorageHandler) StorageManager.get(StorageManager.StorageTag.AI_DATA));
        jobHandler.start();
        jobHandler.join();
        Logger.info(ConsoleColors.GREEN + "Simulation complete !" + ConsoleColors.RESET + " Data stored in " + ConsoleColors.BLUE + "python/datastored/" + StorageManager.StorageTag.AI_DATA.getName() + "-" + id + ConsoleColors.RESET + ".");

        this.stop();
        Main.listenCommand();
    }

    @Override
    public void stop() {
        super.stop();
        StorageManager.unregister(StorageManager.StorageTag.AI_DATA);
    }

    @Override
    protected boolean readArgument(ApplicationArgument<?> argument, Object value) {
        if (argument.getPrefix().equals("id"))
            this.id = (int) value == -1 ? new Random().nextInt(9000) : (int) value;
        return super.readArgument(argument, value);
    }
}
