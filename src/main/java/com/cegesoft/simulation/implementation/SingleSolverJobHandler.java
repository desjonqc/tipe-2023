package com.cegesoft.simulation.implementation;

import com.cegesoft.Main;
import com.cegesoft.app.property.Property;
import com.cegesoft.game.Board;
import com.cegesoft.game.SimulationInformation;
import com.cegesoft.game.position.BoardPosition;
import com.cegesoft.game.BoardSimulation;
import com.cegesoft.log.Logger;
import com.cegesoft.simulation.Job;
import com.cegesoft.simulation.MultipleJobHandler;

import java.util.Collections;
import java.util.List;

public class SingleSolverJobHandler extends MultipleJobHandler {

    private long start;
    private final BoardPosition initialPosition;
    private final SimulationInformation information;
    public SingleSolverJobHandler(BoardPosition initialPosition, SimulationInformation information) {
        this.initialPosition = initialPosition;
        this.information = information;
    }

    @Override
    public Job[] createJobs() {
        start = System.currentTimeMillis();
        return new Job[] {new Job(new BoardSimulation(Main.getTProperty(Property.BOARD_CONFIGURATION), this.initialPosition, this.information), Main.getIntProperty(Property.SIMULATION_TIME))};
    }

    @Override
    public void handleResults() {
        Job job = this.jobs[0];
        BoardSimulation simulation = (BoardSimulation) job.getExecutable();
        List<Integer> results = simulation.getResults(1);
        if (results.isEmpty()) {
            results = simulation.getResults(0);
            Collections.shuffle(results);
        }
        int bestShot = results.get(0);
        float bestAngle = simulation.getAngle(bestShot);
        float bestScore = simulation.getScore(bestShot);
        float bestNorm = simulation.getNorm(bestShot);
        Logger.info("Angle : " + bestAngle);
        Logger.info("Norme : " + bestNorm);
        Logger.info("Score : " + bestScore);
        Logger.info("Temps : " + (System.currentTimeMillis() - start) + "ms");

        Main.<Board>getTProperty(Property.MAIN_BOARD).setBallVelocity(0, (float) (Math.cos(Math.toRadians(bestAngle)) * bestNorm), (float) (Math.sin(Math.toRadians(bestAngle)) * bestNorm));
        Board.bestShot = false;
    }
}
