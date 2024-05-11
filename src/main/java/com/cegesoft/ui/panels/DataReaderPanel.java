package com.cegesoft.ui.panels;

import com.cegesoft.Main;
import com.cegesoft.app.property.Property;
import com.cegesoft.data.exception.ParseFromFileException;
import com.cegesoft.data.handlers.StorageReader;
import com.cegesoft.game.Board;
import com.cegesoft.game.SimulationInformation;
import com.cegesoft.game.exception.BoardParsingException;
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
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Interface d'affichage des positions enregistrées. Aucune intéraction n'est possible,
 * si ce n'est changer l'affichage vers la position suivante / précédente
 */
public class DataReaderPanel extends AbstractGamePanel {

    private final StorageReader<? extends IPositionContainer> positionsReader;
    private final KeyboardListener listener;
    private IPositionContainer currentPosition = null;
    private boolean showBest = true;
    private final List<IBallSet> sets;

    public DataReaderPanel(GameFrame frame, Board board, StorageReader<? extends IPositionContainer> positionsReader) {
        super(frame, board);
        this.positionsReader = positionsReader;
        try {
            this.nextPosition();
        } catch (IOException | ParseFromFileException e) {
            throw new RuntimeException(e);
        }
        this.listener = new KeyboardListener();
        this.sets = Collections.singletonList(new DefaultBallSet());
    }

    /**
     * Affiche la position suivante
     * @throws IOException Si la lecture du fichier contenant la position pose un problème
     * @throws ParseFromFileException si le fichier est illisible
     */
    public void nextPosition() throws IOException, ParseFromFileException {
        if (!positionsReader.hasNext())
            return;
        this.board.setPosition((currentPosition = positionsReader.next()).getBoardPosition());
        frame.repaint();
    }

    /**
     * Affiche la position précédente
     * @throws IOException Si la lecture du fichier contenant la position pose un problème
     * @throws ParseFromFileException si le fichier est illisible
     */
    public void previousPosition() throws IOException, ParseFromFileException {
        if (!positionsReader.hasPrevious())
            return;
        this.board.setPosition((currentPosition = positionsReader.previous()).getBoardPosition());
        frame.repaint();
    }

    /**
     * Affiche ou cache le meilleyr coup à jouer.
     */
    public void toggleShowBest() {
        showBest = !showBest;
        frame.repaint();
    }

    /**
     * Lance la simulation avec les conditions initiales correspondant à la position enregistrée.
     * @throws BoardParsingException Si la position est corrompue.
     */
    public void run() throws BoardParsingException {
        final Board boardClone = new Board(board.getConfiguration(), board.savePosition());
        ClassicGamePanel panel = new ClassicGamePanel(this.frame, boardClone);

        // Modification de la vitesse de la boule blanche
        PositionResult result = ((FullPosition) this.currentPosition).getResults()[0];
        double angle = result.getAngle() * 360.0f / Main.<SimulationInformation>getTProperty(Property.SIMULATION_INFORMATION).getAnglePartition();
        double norm = result.getNorm() * 300.0f / Main.<SimulationInformation>getTProperty(Property.SIMULATION_INFORMATION).getNormPartition();

        float vx = (float) (Math.cos(Math.toRadians(angle)) * norm);
        float vy = (float) (Math.sin(Math.toRadians(angle)) * norm);
        boardClone.setBallVelocity(0, vx, vy);

        frame.setCurrentPanel(panel);
        Timer timer = new Timer();

        // Lorsque l'application est fermée, on libère les ressources
        final Thread hook = new Thread(timer::cancel);
        Runtime.getRuntime().addShutdownHook(hook);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                boardClone.tick();
                if (boardClone.everyBallStopped()) {
                    this.cancel();
                    frame.setCurrentPanel(DataReaderPanel.this);
                    Logger.info("Simulation terminée !");
                    Runtime.getRuntime().removeShutdownHook(hook);
                }
            }
        }, 0L, (long) (Board.TIME_STEP * 1000));
    }

    @Override
    public List<? extends IBallSet> getBallSets() {
        return this.sets;
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
     * Affiche une flèche entre 2 points.
     * @param g le Graphics.
     * @param x1 coordonnée x du premier point.
     * @param y1 coordonnée y du premier point.
     * @param x2 coordonnée x du second point.
     * @param y2 coordonnée y du second point.
     * @param d  l'épaisseur de la flèche.
     * @param h  la hauteur de la flèche.
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

    /**
     * Affiche la flèche représentant la force et la direction de la boule blanche.
     * @param g le Graphics.
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (this.currentPosition instanceof FullPosition) {
            float[] info = this.sets.get(0).getBallInformation(0);
            float x1 = (info[0] * scale + middleX);
            float y1 = (info[1] * scale + middleY);

            PositionResult result = ((FullPosition) this.currentPosition).getResults()[0];
            g.setColor(result.getResult() < 0 ? Color.RED : (result.getResult() == 0 ? Color.YELLOW : Color.BLACK));

            if (showBest) {
                double angle = result.getAngle() * 360.0f / Main.<SimulationInformation>getTProperty(Property.SIMULATION_INFORMATION).getAnglePartition();
                double norm = result.getNorm() * 300.0f / Main.<SimulationInformation>getTProperty(Property.SIMULATION_INFORMATION).getNormPartition();
                this.drawArrowLine(g, x1, y1, x1 + (float) (Math.cos(Math.toRadians(angle)) * norm), y1 + (float) (Math.sin(Math.toRadians(angle)) * norm), 10, 5);
                g.drawString("S: " + result.getResult(), (int) x1, (int) y1);
            }
        }
    }

    /**
     * Écoute les évènements claviers
     */
    private class KeyboardListener implements KeyListener {

        @Override
        public void keyTyped(java.awt.event.KeyEvent e) {

        }

        /**
         * Change la position affichée en fonction de la touche pressée
         * @param e the event to be processed
         */
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
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                toggleShowBest();
            }
            try {
                if (e.getKeyCode() == KeyEvent.VK_R) {
                    run();
                }
            } catch (BoardParsingException ex) {
                Logger.error("Can't run simulation", ex);
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
