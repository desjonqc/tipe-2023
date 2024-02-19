package com.cegesoft.ui.panels;

import com.cegesoft.Main;
import com.cegesoft.app.property.Property;
import com.cegesoft.game.Board;
import com.cegesoft.log.Logger;
import com.cegesoft.simulation.implementation.SingleSolverJobHandler;
import com.cegesoft.ui.AbstractGamePanel;
import com.cegesoft.ui.GameFrame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ComparisonGamePanel extends AbstractGamePanel {

    private final List<Board> boards;
    private final List<ComparisonBallSet> ballSets;
    private final KeyboardListener keyboardListener;
    private final ClickListener clickListener;

    public ComparisonGamePanel(GameFrame frame, Board... boards) {
        super(frame, boards[0]);
        this.boards = Arrays.asList(boards);
        this.ballSets = this.boards.stream().map(board -> new ComparisonBallSet(board, BALL_COLORS[this.boards.indexOf(board)])).collect(Collectors.toList());
        this.keyboardListener = new KeyboardListener();
        this.clickListener = new ClickListener();
    }

    @Override
    public List<? extends IBallSet> getBallSets() {
        return ballSets;
    }

    @Override
    public String getTitle() {
        return "Plateau de Comparaison";
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

    public static class ComparisonBallSet implements IBallSet {
        private final Board board;
        private final Color color;

        public ComparisonBallSet(Board board, Color color) {
            this.board = board;
            this.color = color;
        }

        @Override
        public float[] getBallInformation(int i) {
            return this.board.getBallInformation(i);
        }

        @Override
        public Color getBallColor(int i) {
            return this.color;
        }

        @Override
        public Board getBoard() {
            return board;
        }
    }

    private class KeyboardListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent evt) {
            try {
                if (evt.getKeyCode() == KeyEvent.VK_SPACE && !Board.bestShot) {
                    Logger.info("Calculating best shot...");
                    Board.bestShot = true;
                    SingleSolverJobHandler jobHandler = new SingleSolverJobHandler(board.savePosition(), Main.getTProperty(Property.SIMULATION_INFORMATION));
                    jobHandler.start();
                }
            } catch (Exception e) {
                Logger.error(e);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    }

    private class ClickListener implements MouseListener {

        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            for (Board board : boards) {
                if (board.everyBallStopped()) {
                    float[] info = board.getBallInformation(0);
                    float vx = (e.getX() - middleX - info[0] * scale) * 5 / scale;
                    float vy = (e.getY() - middleY - info[1] * scale) * 5 / scale;
                    board.setBallVelocity(0, vx, vy);
                    Logger.info("Ball 0 velocity set to " + vx + ", " + vy);
                }
            }
            for (IBallSet set : ballSets)
                Logger.info("Board " + set.getBoard().getName() + " is colored " + set.getBallColor(0));

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
