package com.cegesoft;

import com.cegesoft.data.handlers.FullStorageHandler;
import com.cegesoft.data.handlers.StorageHandler;
import com.cegesoft.data.StorageManager;
import com.cegesoft.game.Board;
import com.cegesoft.game.BoardConfiguration;
import com.cegesoft.game.SimulationInformation;
import com.cegesoft.opencl.CLHandler;
import com.cegesoft.ui.GameFrame;
import com.cegesoft.util.weighting.ConstantScoreWeighting;
import com.cegesoft.util.weighting.ScoreWeighting;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Application de simulation d'une partie de billard
 * L'objet de ce programme est de trouver le meilleur coup à jouer
 *
 * @author Clément DESJONQUERES
 */
public class Main {

    public static int count = 0;
    public static final int BALL_AMOUNT = 16;
    public static BoardConfiguration BOARD_CONFIGURATION;
    public static SimulationInformation SIMULATION_INFORMATION;
    public static Board MAIN_BOARD;

    public static void main(String[] args) throws IOException {
        initialise();
        // Création du billard
        MAIN_BOARD = new Board(BOARD_CONFIGURATION, Board.INITIAL_POSITION);

        // Création de la fenêtre
        new GameFrame(MAIN_BOARD);

        // Création du processus de simulation
        Timer timer = new Timer();

        // Lorsque l'application est fermée, on libère les ressources
        Runtime.getRuntime().addShutdownHook(new Thread(timer::cancel));

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                MAIN_BOARD.tick();
            }
        }, 0L, (long) (Board.TIME_STEP * 1000));
    }

    public static void initialise() throws IOException {
        CLHandler handler = new CLHandler();
        // Création de la configuration
        BOARD_CONFIGURATION = new BoardConfiguration(98, 48, BALL_AMOUNT, -0.0012f, handler);
        // Création des informations de simulation
//        SIMULATION_INFORMATION = new SimulationInformation(900, 100, 15);
        SIMULATION_INFORMATION = new SimulationInformation(500, 119, 15);
        ScoreWeighting storageWeighting = new ConstantScoreWeighting(new int[] {8}, new int[] {3}, new int[] {4}, true);
        StorageManager.register(StorageManager.StorageTag.AI_DATA, new FullStorageHandler("python/data/" + StorageManager.StorageTag.AI_DATA.getName() + "/", "data",
                4, Main.SIMULATION_INFORMATION.getDataGroupSize(), Main.SIMULATION_INFORMATION, storageWeighting));

        StorageManager.register(StorageManager.StorageTag.STATISTIC_POSITION, new StorageHandler("python/data/" + StorageManager.StorageTag.STATISTIC_POSITION.getName() + "/", "pos",
                10, 8 * BALL_AMOUNT));

    }
}