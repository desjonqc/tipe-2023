package com.cegesoft.ui.panels;

import com.cegesoft.Main;
import com.cegesoft.data.handlers.FullStorageHandler;
import com.cegesoft.data.handlers.StorageHandler;
import com.cegesoft.data.StorageManager;
import com.cegesoft.game.Board;
import com.cegesoft.game.BoardSimulation;
import com.cegesoft.game.position.BoardPosition;
import com.cegesoft.game.position.FullPosition;
import com.cegesoft.simulation.Job;
import com.cegesoft.simulation.implementation.DeepProportionateJobHandler;
import com.cegesoft.simulation.implementation.SingleSolverJobHandler;
import com.cegesoft.ui.AbstractGamePanel;
import com.cegesoft.ui.GameFrame;
import com.cegesoft.util.DepthCounter;
import com.cegesoft.util.weighting.ConstantScoreWeighting;
import com.cegesoft.util.weighting.ScoreWeighting;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;

public class ClassicGamePanel extends AbstractGamePanel {

    private final KeyboardListener keyboardListener;
    private final ClickListener clickListener;
    public ClassicGamePanel(GameFrame frame, Board board) {
        super(frame, board);
        this.keyboardListener = new KeyboardListener();
        this.clickListener = new ClickListener();
    }

    @Override
    public float[] getBallInformation(int i) {
        return this.board.getBallInformation(i);
    }

    @Override
    public String getTitle() {
        return "Plateau de Jeu";
    }

    @Override
    public void registerListeners() {
        frame.addKeyListener(this.keyboardListener);
        this.addMouseListener(this.clickListener);
    }

    @Override
    public void unregisterListeners() {
        frame.removeKeyListener(this.keyboardListener);
        this.removeMouseListener(this.clickListener);
    }

    private class KeyboardListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {}

        @Override
        public void keyPressed(KeyEvent evt) {
            if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                System.out.println("Calculating best shot...");
                Board.bestShot = true;
                SingleSolverJobHandler jobHandler = new SingleSolverJobHandler(board.savePosition(), Main.SIMULATION_INFORMATION);
                jobHandler.start();
            }
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                float bestNorm = 300;
                float bestAngle = 3;
                board.setBallVelocity(0, (float) (Math.cos(Math.toRadians(bestAngle)) * bestNorm), (float) (Math.sin(Math.toRadians(bestAngle)) * bestNorm));
            }

            if (evt.getKeyCode() == KeyEvent.VK_S) {
                StorageHandler handler = StorageManager.get(StorageManager.StorageTag.STATISTIC_POSITION);
                BoardPosition position = board.savePosition();
                handler.addStorable(position, true);
            }

            if (evt.getKeyCode() == KeyEvent.VK_L) {
                System.out.println("Loading position...");
                StorageHandler handler = StorageManager.get(StorageManager.StorageTag.STATISTIC_POSITION);

                BoardPosition position = handler.get(0).getDataGroup(BoardPosition.class, 0);
                board.setPosition(position);
                System.out.println("Position loaded !");
            }

            if (evt.getKeyCode() == KeyEvent.VK_D) {
                ScoreWeighting weighting = new ConstantScoreWeighting(new int[] {4}, new int[] {3}, new int[] {3}, true);
                DeepProportionateJobHandler jobHandler = new DeepProportionateJobHandler(
                        new Job(
                                new BoardSimulation(Main.BOARD_CONFIGURATION, board.savePosition(), Main.SIMULATION_INFORMATION),
                                BoardSimulation.SIMULATION_TIME),
                        new DepthCounter(1), weighting, Main.SIMULATION_INFORMATION,
                        (FullStorageHandler) StorageManager.get(StorageManager.StorageTag.AI_DATA));
                jobHandler.start();
            }

            if (evt.getKeyCode() == KeyEvent.VK_R) {
                StorageHandler handler = StorageManager.get(StorageManager.StorageTag.AI_DATA);
                frame.setCurrentPanel(new DataReaderPanel(frame, board, handler.listStorable(FullPosition.class)));
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {}
    }

    private class ClickListener implements MouseListener {

        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            if (board.everyBallStopped()) {
                Main.count = 0;
                float[] info = board.getBallInformation(0);
                float vx = (e.getX() - middleX - info[0] * scale) * 5 / scale;
                float vy = (e.getY() - middleY - info[1] * scale) * 5 / scale;
                board.setBallVelocity(0, vx, vy);
            }
        }

        @Override
        public void mousePressed(java.awt.event.MouseEvent e) {

        }

        @Override
        public void mouseReleased(java.awt.event.MouseEvent e) {

        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {

        }

        @Override
        public void mouseExited(java.awt.event.MouseEvent e) {

        }
    }
}
