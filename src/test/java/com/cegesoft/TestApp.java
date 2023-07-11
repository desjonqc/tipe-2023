package com.cegesoft;

import com.cegesoft.app.Application;
import com.cegesoft.app.argument.ApplicationArgument;
import com.cegesoft.app.property.Property;
import com.cegesoft.app.property.PropertyArgument;
import com.cegesoft.game.Board;
import com.cegesoft.game.BoardConfiguration;
import com.cegesoft.game.SimulationInformation;
import com.cegesoft.opencl.CLHandler;

import java.io.File;
import java.io.IOException;

public class TestApp extends Application {
    public TestApp() throws IOException {
        Main.CURRENT_APPLICATION = this;
        this.registerArgument(new PropertyArgument<>(false, "", 16, "", Property.BALL_AMOUNT));
        this.registerArgument(new PropertyArgument<>(false, "", -0.016f, "", Property.ALPHA));
        this.registerArgument(new PropertyArgument<>(false, "", 6600, "", Property.SIMULATION_TIME));
        this.registerArgument(new PropertyArgument<>(false, "", 100, "", Property.RESULT_LIMIT));

        this.readArguments(new String[0]);
        this.start();
    }

    @Override
    public void start() throws IOException {
        CLHandler handler = new CLHandler();
        this.setProperty(Property.CL_HANDLER, handler);

        BoardConfiguration configuration = new BoardConfiguration(98, 48, 16, -0.016f, handler);
        this.setProperty(Property.BOARD_CONFIGURATION, configuration);
        SimulationInformation information = new SimulationInformation(500, 100, 100);
        this.setProperty(Property.SIMULATION_INFORMATION, information);
        this.setProperty(Property.MAIN_BOARD, new Board(configuration, Board.INITIAL_POSITION));
    }

    @Override
    protected boolean readArgument(ApplicationArgument<?> argument, Object value) {
        return false;
    }

    public static void clearDirectory() {
        File file = new File("test/");
        for (File child : file.listFiles())
            child.delete();
        file.delete();
    }
}
