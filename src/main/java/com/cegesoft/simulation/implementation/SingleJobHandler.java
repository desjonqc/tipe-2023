package com.cegesoft.simulation.implementation;

import com.cegesoft.Main;
import com.cegesoft.game.Board;
import com.cegesoft.game.BoardPosition;
import com.cegesoft.game.BoardSimulation;
import com.cegesoft.game.BoardStructure;
import com.cegesoft.simulation.Job;
import com.cegesoft.simulation.MultipleJobHandler;

public class SingleJobHandler extends MultipleJobHandler {

    private long start;
    private final BoardPosition initialPosition;
    public SingleJobHandler(BoardPosition initialPosition, int jobHandlerId) {
        super(jobHandlerId);
        this.initialPosition = initialPosition;
    }

    @Override
    public Job[] createJobs() {
        start = System.currentTimeMillis();
        BoardSimulation simulation = new BoardSimulation(Main.BOARD_CONFIGURATION);
        simulation.initialise(this.initialPosition);
        return new Job[] {new Job(simulation, 6600)};
    }

    @Override
    public void handleResults() {
        Job job = this.jobs[0];
        BoardSimulation simulation = (BoardSimulation) job.getExecutable();
        int bestShot = simulation.getResults().get(0);
        float bestAngle = simulation.getAngle(bestShot);
        float bestScore = simulation.getScore(bestShot);
        float bestNorm = simulation.getNorm(bestShot);
        System.out.println("Angle : " + bestAngle);
        System.out.println("Norme : " + bestNorm);
        System.out.println("Score : " + bestScore);
        System.out.println("Temps : " + (System.currentTimeMillis() - start) + "ms");

        Main.MAIN_BOARD.setBallVelocity(0, (float) (Math.cos(Math.toRadians(bestAngle)) * bestNorm), (float) (Math.sin(Math.toRadians(bestAngle)) * bestNorm));
        Board.bestShot = false;
    }
}
