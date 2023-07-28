package com.cegesoft.ai;

import com.cegesoft.Main;
import com.cegesoft.ai.models.Model;
import com.cegesoft.app.SimulationApplication;
import com.cegesoft.app.argument.ApplicationArgument;
import com.cegesoft.app.property.Property;
import com.cegesoft.data.StorageManager;
import com.cegesoft.data.handlers.StorageHandler;
import com.cegesoft.data.handlers.StorageReader;
import com.cegesoft.data.metadata.SimulationFileMetadata;
import com.cegesoft.game.Board;
import com.cegesoft.game.position.FullPosition;
import com.cegesoft.log.Logger;

public class AITrainingApplication extends SimulationApplication {

    private int id;
    private StorageHandler handler;
    public AITrainingApplication() {
        super();
        this.registerArgument(new ApplicationArgument<>(true, "id", -1, "Id of the simulation to read."));
    }

    @Override
    public void start() throws Exception {
        super.start();
        Board board = new Board(this.getTProperty(Property.BOARD_CONFIGURATION), Board.INITIAL_POSITION);
        this.setProperty(Property.MAIN_BOARD, board);
        handler = new StorageHandler("python/datastored/" + StorageManager.StorageTag.AI_DATA.getName() + "-" + id + "/", "data",
                20, SimulationFileMetadata.class);
        try (Model model = new Model()) {
            Logger.info("Model created !");
            Logger.info("Training...");
            StorageReader<FullPosition> reader = handler.getReader(FullPosition.class);
            while (reader.hasNext()) {
                FullPosition position = reader.next();
                model.train(position);
            }
            Logger.info("Training done !");
            Logger.info("Predicting...");
            Float[] prediction = model.predict(Board.INITIAL_POSITION);
            Logger.info("Prediction done !");
            Logger.info("Prediction : " + prediction[0] + ", " + prediction[1]);
        }
        this.stop();
        Main.listenCommand();
    }

    @Override
    public void stop() {
        super.stop();
        this.handler.close();
    }

    @Override
    protected boolean readArgument(ApplicationArgument<?> argument, Object value) {
        if (argument.getPrefix().equalsIgnoreCase("id"))
            this.id = (int) value;
        return false;
    }
}
