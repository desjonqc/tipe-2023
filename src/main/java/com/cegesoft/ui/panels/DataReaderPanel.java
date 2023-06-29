package com.cegesoft.ui.panels;

import com.cegesoft.game.Board;
import com.cegesoft.game.position.FullPosition;
import com.cegesoft.ui.AbstractGamePanel;
import com.cegesoft.ui.GameFrame;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

public class DataReaderPanel extends AbstractGamePanel {

    private int currentPositionIndex = 0;
    private final List<FullPosition> positions;
    private final KeyboardListener listener;
    public DataReaderPanel(GameFrame frame, Board board, List<FullPosition> positions) {
        super(frame, board);
        this.positions = positions;
        this.switchPosition(0);
        this.listener = new KeyboardListener();
    }

    public void switchPosition(int i) {
        if (i < 0 || i >= this.positions.size()) {
            return;
        }
        currentPositionIndex = i;
        this.board.setPosition(positions.get(currentPositionIndex).getPosition());
        frame.repaint();
    }

    @Override
    public float[] getBallInformation(int i) {
        return board.getBallInformation(i);
    }

    @Override
    public String getTitle() {
        return "Position Reader (" + (currentPositionIndex + 1) + " / " + this.positions.size() + ")";
    }

    @Override
    public void registerListeners() {
        frame.addKeyListener(this.listener);
    }

    @Override
    public void unregisterListeners() {
        frame.removeKeyListener(this.listener);
    }

    private class KeyboardListener implements KeyListener {

        @Override
        public void keyTyped(java.awt.event.KeyEvent e) {

        }

        @Override
        public void keyPressed(java.awt.event.KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                switchPosition(currentPositionIndex - 1);
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                switchPosition(currentPositionIndex + 1);
            }
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                frame.resetPanel();
            }
        }

        @Override
        public void keyReleased(java.awt.event.KeyEvent e) {

        }
    }
}
