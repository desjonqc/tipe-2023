package com.cegesoft;

import com.cegesoft.game.Board;
import com.cegesoft.game.BoardConfiguration;
import com.cegesoft.opencl.CLHandler;
import com.cegesoft.ui.GameFrame;

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

    }
}