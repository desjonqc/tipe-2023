package com.cegesoft;

import com.cegesoft.game.Board;
import com.cegesoft.game.GamePosition;
import com.cegesoft.opencl.CLField;
import com.cegesoft.opencl.CLFile;
import com.cegesoft.opencl.CLFunction;
import com.cegesoft.opencl.CLHandler;
import com.cegesoft.ui.GameFrame;
import com.nativelibs4java.opencl.CLMem;
import org.bridj.PointerIO;
import org.bridj.util.Tuple;

import java.io.IOException;
import java.util.List;
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
        Board board = new Board(handler, 50, 100, 16);
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


//        for (int j = 0; j < 100; j++) {


//        }

//        for (int i = 0; i < 360; i++) {
//            CLEvent[] events = new CLEvent[100];
//            for (int j = 0; j < 100; j++) {
//                CLQueue queue = handler.createQueue();
//                events[j] = position.move((float) (j * 2 * Math.cos(Math.toRadians(i))), (float) (j * 2 * Math.sin(Math.toRadians(i))));
//            }
//            CLEvent.waitFor(events);
//        }
//
//        long end = System.currentTimeMillis();
//        System.out.println("Temps total : " + (end - start) + "ms");


        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                board.tick(frame);
            }
        }, 0L, (long) (Board.TIME_STEP * 1000));
    }
}