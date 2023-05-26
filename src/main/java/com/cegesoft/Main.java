package com.cegesoft;

import com.cegesoft.game.Board;
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

    public static void main(String[] args) throws IOException {
        CLHandler handler = new CLHandler();

        // Création du billard
        Board board = new Board(handler, 48, 98, 16);
        board.initialise(Board.INITIAL_POSITION);

        // Création de la fenêtre
        GameFrame frame = new GameFrame(board);

        // Création du processus de simulation
        Timer timer = new Timer();

        // Lorsque l'application est fermée, on libère les ressources
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            timer.cancel();
            handler.release();
        }));

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                board.tick(frame);
            }
        }, 0L, (long) (Board.TIME_STEP * 1000));
    }
}