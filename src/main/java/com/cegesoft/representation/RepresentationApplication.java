package com.cegesoft.representation;

import com.cegesoft.Main;
import com.cegesoft.app.SimulationApplication;
import com.cegesoft.app.argument.ApplicationArgument;
import com.cegesoft.app.property.Property;
import com.cegesoft.data.StorageManager;
import com.cegesoft.data.handlers.FullStorageHandler;
import com.cegesoft.data.handlers.StorageHandler;
import com.cegesoft.data.metadata.SimulationFileMetadata;
import com.cegesoft.game.Board;
import com.cegesoft.game.position.FullPosition;
import com.cegesoft.ui.GameFrame;
import com.cegesoft.ui.panels.DataReaderPanel;

/**
 * Application permettant l'affichage d'une collection de simulations, indicé par l'identifiant id.
 */
public class RepresentationApplication extends SimulationApplication {

    private int id;
    private StorageHandler handler;
    public RepresentationApplication() {
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
        new GameFrame(board, frame -> new DataReaderPanel(frame, board, handler.getReader(FullPosition.class)));
    }

    @Override
    public void stop() {
        this.handler.close();
        super.stop();
    }

    @Override
    protected boolean readArgument(ApplicationArgument<?> argument, Object value) {
        if (argument.getPrefix().equalsIgnoreCase("id"))
            this.id = (int) value;
        return super.readArgument(argument, value);
    }
}
