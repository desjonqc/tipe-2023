package com.cegesoft.app;

import com.cegesoft.app.argument.ApplicationArgument;
import com.cegesoft.app.property.Property;
import com.cegesoft.app.property.PropertyArgument;
import com.cegesoft.game.BoardConfiguration;
import com.cegesoft.game.SimulationInformation;
import com.cegesoft.log.Logger;
import com.cegesoft.opencl.CLHandler;

/**
 * Application abstraite permettant d'effectuer des simulations de billard
 */
public class SimulationApplication extends Application {
    protected int anglePartition, normPartition;

    public SimulationApplication() {
        this.registerArgument(new PropertyArgument<>(false, "ball-amount", 16, "Amount of balls on the pool", Property.BALL_AMOUNT));
        this.registerArgument(new PropertyArgument<>(false, "alpha", 0.8f, "Fluid coefficient of friction", Property.ALPHA));
        this.registerArgument(new PropertyArgument<>(false, "result-limit", 15, "Amount of couple (angle, norm) to store for each position", Property.RESULT_LIMIT));
        this.registerArgument(new PropertyArgument<>(false, "simulation-time", 6600, "Duration of the simulation (ms)", Property.SIMULATION_TIME));
        this.registerArgument(new ApplicationArgument<>(false, "partition", "1000,55", "Partition size '[angle],[norm]'"));
    }

    @Override
    public void start() throws Exception {
        CLHandler handler = new CLHandler();
        this.setProperty(Property.CL_HANDLER, handler);

        BoardConfiguration boardConfiguration = new BoardConfiguration(98, 48, this.getIntProperty(Property.BALL_AMOUNT), this.getFloatProperty(Property.ALPHA), handler);
        this.setProperty(Property.BOARD_CONFIGURATION, boardConfiguration);

        SimulationInformation information = new SimulationInformation(this.anglePartition, this.normPartition, this.getIntProperty(Property.RESULT_LIMIT));
        this.setProperty(Property.SIMULATION_INFORMATION, information);

    }

    @Override
    protected boolean readArgument(ApplicationArgument<?> argument, Object value) {
        switch (argument.toString()) {
            case "partition":
                String[] partitions = ((String) value).split(",");
                if (partitions.length != 2) {
                    Logger.info("La partition doit contenir 2 valeurs (séparées par ,)");
                    return true;
                }
                this.anglePartition = Integer.parseInt(partitions[0]);
                this.normPartition = Integer.parseInt(partitions[1]);
                break;
        }
        return false;
    }
}
