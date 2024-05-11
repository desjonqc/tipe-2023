package com.cegesoft.comparator;

import com.cegesoft.app.SimulationApplication;
import com.cegesoft.app.property.Property;
import com.cegesoft.game.Board;
import com.cegesoft.game.BoardStructure;
import com.cegesoft.opencl.CLFunction;
import com.cegesoft.ui.GameFrame;
import com.cegesoft.ui.panels.ComparisonGamePanel;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Application de comparaison d'algorithmes avec interface graphique.
 * Chaque couleur représente une méthode de résolution.
 * Les algorithmes comparés sont :
 *   - Euler explicite
 *   - Solve IVP (de python)
 *   - Runge-Kutta à l'ordre 3
 */
public class ComparatorApplication extends SimulationApplication {
    private Timer timer;

    @Override
    public void start() throws Exception {
        super.start();
        BoardStructure.TIME_STEP = 0.005f;

        Board rungeKuttaBoard = new Board(this.getTProperty(Property.BOARD_CONFIGURATION), Board.INITIAL_POSITION);
        Board solve_ivpBoard = new Board(this.getTProperty(Property.BOARD_CONFIGURATION), Board.INITIAL_POSITION);
        Board eulerExplicitBoard = new Board(this.getTProperty(Property.BOARD_CONFIGURATION), Board.INITIAL_POSITION);

        rungeKuttaBoard.setName("Runge-Kutta");
        solve_ivpBoard.setName("Solve_ivp");
        eulerExplicitBoard.setName("Euler Explicit");

        PythonFunction solve_ivpFunction = new PythonFunction("python/solve_ivp_algorithm.py");
        for (int i = 0; i < solve_ivpBoard.getFunction().getArgumentCount(); i++) {
            solve_ivpFunction.setArgument(i, solve_ivpBoard.getFunction().getArgument(i));
        }
        solve_ivpBoard.setFunction(solve_ivpFunction);

        CLFunction eulerExplicitFunction = new CLFunction(eulerExplicitBoard.getFile(), "moveEulerExplicit",
                eulerExplicitBoard.getBallsField(), eulerExplicitBoard.getEditBallsField(), eulerExplicitBoard.getBallBufferSizeField(), eulerExplicitBoard.getBallsAmountField(),
                eulerExplicitBoard.getAlphaField(), eulerExplicitBoard.getHeightField(), eulerExplicitBoard.getWidthField(), eulerExplicitBoard.getTimeStepField(), eulerExplicitBoard.getGameInformationField(), eulerExplicitBoard.getDebugField());

        eulerExplicitBoard.setFunction(eulerExplicitFunction);

        this.setProperty(Property.MAIN_BOARD, rungeKuttaBoard);

        // Création de la fenêtre
        new GameFrame(rungeKuttaBoard, frame -> new ComparisonGamePanel(frame, rungeKuttaBoard, solve_ivpBoard, eulerExplicitBoard));

        // Création du processus de simulation
        timer = new Timer();

        // Lorsque l'application est fermée, on libère les ressources
        Runtime.getRuntime().addShutdownHook(new Thread(timer::cancel));

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                rungeKuttaBoard.tick();
                eulerExplicitBoard.tick();
                solve_ivpBoard.tick();
            }
        }, 0L, (long) (Board.TIME_STEP * 1000));
    }

    @Override
    public void stop() {
        super.stop();
        timer.cancel();
        BoardStructure.TIME_STEP = 0.001f;
    }
}
