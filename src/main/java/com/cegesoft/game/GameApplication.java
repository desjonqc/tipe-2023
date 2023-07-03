package com.cegesoft.game;

import com.cegesoft.Main;
import com.cegesoft.app.SimulationApplication;
import com.cegesoft.app.argument.ApplicationArgument;
import com.cegesoft.app.property.Property;
import com.cegesoft.data.StorageManager;
import com.cegesoft.data.handlers.FullStorageHandler;
import com.cegesoft.game.Board;
import com.cegesoft.game.SimulationInformation;
import com.cegesoft.ui.GameFrame;
import com.cegesoft.util.weighting.ConstantScoreWeighting;
import com.cegesoft.util.weighting.ScoreWeighting;

import java.util.Timer;
import java.util.TimerTask;

public class GameApplication extends SimulationApplication {

    @Override
    public void start() throws Exception {
        super.start();

        SimulationInformation information = this.getTProperty(Property.SIMULATION_INFORMATION);
        ScoreWeighting storageWeighting = new ConstantScoreWeighting(new int[]{8}, new int[]{3}, new int[]{4}, true);
        StorageManager.register(StorageManager.StorageTag.AI_DATA, new FullStorageHandler("python/data/" + StorageManager.StorageTag.AI_DATA.getName() + "/", "data",
                4, information.getDataGroupSize(), information, storageWeighting));

        Board board = new Board(this.getTProperty(Property.BOARD_CONFIGURATION), Board.INITIAL_POSITION);
        this.setProperty(Property.MAIN_BOARD, board);

        // Création de la fenêtre
        new GameFrame(board);

        // Création du processus de simulation
        Timer timer = new Timer();

        // Lorsque l'application est fermée, on libère les ressources
        Runtime.getRuntime().addShutdownHook(new Thread(timer::cancel));

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                board.tick();
            }
        }, 0L, (long) (Board.TIME_STEP * 1000));
    }
}
