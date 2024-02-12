package com.cegesoft.ui.panels;

import com.cegesoft.Main;
import com.cegesoft.app.property.Property;
import com.cegesoft.data.exception.ParseFromFileException;
import com.cegesoft.data.handlers.StorageReader;
import com.cegesoft.game.Board;
import com.cegesoft.game.SimulationInformation;
import com.cegesoft.game.position.FullPosition;
import com.cegesoft.game.position.IPositionContainer;
import com.cegesoft.game.position.PositionResult;
import com.cegesoft.log.Logger;
import com.cegesoft.ui.AbstractGamePanel;
import com.cegesoft.ui.GameFrame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Line2D;
import java.io.IOException;

/**
 * Interface d'affichage des positions enregistées. Aucune intéraction possible,
 * si ce n'est changer vers la position suivante / précédente
 */
public class DataReaderPanel extends AbstractGamePanel {

    private final StorageReader<? extends IPositionContainer> positionsReader;
    private final KeyboardListener listener;
    private IPositionContainer currentPosition = null;

    public DataReaderPanel(GameFrame frame, Board board, StorageReader<? extends IPositionContainer> positionsReader) {
        super(frame, board);
        this.positionsReader = positionsReader;
        try {
            this.nextPosition();
        } catch (IOException | ParseFromFileException e) {
            throw new RuntimeException(e);
        }
        this.listener = new KeyboardListener();
    }

    public void nextPosition() throws IOException, ParseFromFileException {
        if (!positionsReader.hasNext())
            return;
        this.board.setPosition((currentPosition = positionsReader.next()).getBoardPosition());
        frame.repaint();
    }

    public void previousPosition() throws IOException, ParseFromFileException {
        if (!positionsReader.hasPrevious())
            return;
        this.board.setPosition((currentPosition = positionsReader.previous()).getBoardPosition());
        frame.repaint();
    }

    @Override
    public float[] getBallInformation(int i) {
        return board.getBallInformation(i);
    }

    @Override
    public String getTitle() {
        return "Position Reader (" + (this.positionsReader.getCurrentStorageIndex() + 1) +
                " / " + this.positionsReader.getStorageSize() +
                " | " + (this.positionsReader.getStorageIndex() + 1) +
                " / " + this.positionsReader.getStorageAmount() + ")";
    }

    @Override
    public void registerListeners() {
        frame.addKeyListener(this.listener);
    }

    @Override
    public void unregisterListeners() {
        frame.removeKeyListener(this.listener);
    }

    /**
     * Draw an arrow line between two points.
     * @param g the graphics component.
     * @param x1 x-position of first point.
     * @param y1 y-position of first point.
     * @param x2 x-position of second point.
     * @param y2 y-position of second point.
     * @param d  the width of the arrow.
     * @param h  the height of the arrow.
     */
    private void drawArrowLine(Graphics g, float x1, float y1, float x2, float y2, int d, int h) {
        float dx = x2 - x1, dy = y2 - y1;
        double D = Math.sqrt(dx*dx + dy*dy);
        double xm = D - d, xn = xm, ym = h, yn = -h, x;
        double sin = dy / D, cos = dx / D;

        x = xm*cos - ym*sin + x1;
        ym = xm*sin + ym*cos + y1;
        xm = x;

        x = xn*cos - yn*sin + x1;
        yn = xn*sin + yn*cos + y1;
        xn = x;

        int[] xpoints = {(int) x2, (int) xm, (int) xn};
        int[] ypoints = {(int) y2, (int) ym, (int) yn};

        Line2D line = new Line2D.Float(x1, y1, x2, y2);
        ((Graphics2D)g).setStroke(new BasicStroke(2));
        ((Graphics2D)g).draw(line);

        g.fillPolygon(xpoints, ypoints, 3);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (this.currentPosition instanceof FullPosition) {
            float[] info = this.getBallInformation(0);
            float x1 = (info[0] * scale + middleX);
            float y1 = (info[1] * scale + middleY);

            PositionResult result = ((FullPosition) this.currentPosition).getResults()[0];
            g.setColor(result.getResult() < 0 ? Color.RED : (result.getResult() == 0 ? Color.YELLOW : Color.BLACK));

            double angle = result.getAngle() * 360.0f / Main.<SimulationInformation>getTProperty(Property.SIMULATION_INFORMATION).getAnglePartition();
            double norm = result.getNorm() * 300.0f / Main.<SimulationInformation>getTProperty(Property.SIMULATION_INFORMATION).getNormPartition();
            this.drawArrowLine(g, x1, y1, x1 + (float) (Math.cos(Math.toRadians(angle)) * norm), y1 + (float) (Math.sin(Math.toRadians(angle)) * norm), 10, 5);
            g.drawString("S: " + result.getResult(), (int) x1, (int) y1);
        }
    }

    private class KeyboardListener implements KeyListener {

        @Override
        public void keyTyped(java.awt.event.KeyEvent e) {

        }

        @Override
        public void keyPressed(java.awt.event.KeyEvent e) {
            try {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    previousPosition();
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    nextPosition();
                }
            } catch (IOException | ParseFromFileException ex) {
                Logger.error("Can't change position", ex);
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
