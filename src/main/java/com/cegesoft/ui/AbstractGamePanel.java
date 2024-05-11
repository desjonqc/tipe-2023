package com.cegesoft.ui;

import com.cegesoft.game.Board;
import com.cegesoft.log.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.Arc2D;
import java.io.IOException;
import java.text.AttributedString;
import java.util.List;

/**
 * Affichage abstrait d'un billard, gestion des boules et des mises à jour
 */
public abstract class AbstractGamePanel extends JPanel {
    public static final Color GREEN = new Color(64, 152, 68);

    public static final Color[] BALL_COLORS = new Color[]{
            new Color(210, 190, 19),
            new Color(2, 86, 162),
            new Color(175, 15, 12),
            new Color(52, 43, 101),
            new Color(207, 126, 58),
            new Color(17, 104, 64),
            new Color(138, 46, 48),
            new Color(0, 0, 0)
    };

    protected final Board board;
    protected final GameFrame frame;
    protected final float holeDiameter = 4.2f;
    protected final float scale;
    protected final int middleX;
    protected final int middleY;

    private final Image background;
    private final Polygon[] borders = new Polygon[6];

    public AbstractGamePanel(GameFrame frame, Board board) {
        this.frame = frame;
        this.board = board;

        this.scale = this.frame.scale;
        this.middleX = this.frame.middleX;
        this.middleY = this.frame.middleY;


        registerHorizontalBorders( 1, 1, holeDiameter, 0);
        registerHorizontalBorders(1, -1, holeDiameter, 1);
        registerHorizontalBorders(-1, 1, holeDiameter, 2);
        registerHorizontalBorders(-1, -1, holeDiameter, 3);

        registerVerticalBorders(1, holeDiameter, 4);
        registerVerticalBorders(-1, holeDiameter, 5);

        Image background = null;
        try {
            background = ImageIO.read(ClassLoader.getSystemResource("wood.png"));
        } catch (IOException e) {
            Logger.error("Can't load background image :", e);
            System.exit(-1);
        } finally {
            this.background = background;
        }
    }


    /**
     * @return les ensembles de boules à afficher
     */
    public abstract List<? extends IBallSet> getBallSets();

    /**
     * @return le titre de la fenêtre
     */
    public abstract String getTitle();

    /**
     * Enregistre les différents évènements (clics, touches...)
     */
    public abstract void registerListeners();

    /**
     * Supprime les évènements précédemment enregistrés
     */
    public abstract void unregisterListeners();

    /**
     * Fonction héritée de JPanel
     * @param g the <code>Graphics</code> context in which to paint
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(background, 0, 0, this.getWidth(), this.getHeight(), null);
        g.setColor(new Color(64, 152, 68));
        g.fillRect((int) (-(board.getWidth() + 2) * scale / 2) + middleX, (int) (-(board.getHeight() + 2) * scale / 2) + middleY, (int) ((board.getWidth() + 2) * scale), (int) ((board.getHeight() + 2) * scale));

        g.setColor(Color.BLACK);

        g.fillArc((int) ((-board.getWidth() - holeDiameter) * scale / 2) + middleX, (int) ((-board.getHeight() - holeDiameter) * scale / 2) + middleY, (int) (holeDiameter * scale), (int) (holeDiameter * scale), 0, 360);
        g.fillArc((int) ((board.getWidth() - holeDiameter) * scale / 2) + middleX, (int) ((-board.getHeight() - holeDiameter) * scale / 2) + middleY, (int) (holeDiameter * scale), (int) (holeDiameter * scale), 0, 360);
        g.fillArc((int) ((-board.getWidth() - holeDiameter) * scale / 2) + middleX, (int) ((board.getHeight() - holeDiameter) * scale / 2) + middleY, (int) (holeDiameter * scale), (int) (holeDiameter * scale), 0, 360);
        g.fillArc((int) ((board.getWidth() - holeDiameter) * scale / 2) + middleX, (int) ((board.getHeight() - holeDiameter) * scale / 2) + middleY, (int) (holeDiameter * scale), (int) (holeDiameter * scale), 0, 360);
        g.fillArc((int) (middleX - 3 * scale), (int) ((board.getHeight() - 1.4f) * scale / 2) + middleY, (int) (6 * scale), (int) (1.4f * holeDiameter * scale), 0, 180);
        g.fillArc((int) (middleX - 3 * scale), (int) ((-board.getHeight() - 2.0f * holeDiameter - 1.5f) * scale / 2) + middleY, (int) (6 * scale), (int) (1.4f * holeDiameter * scale), 180, 180);

        drawBorders((Graphics2D) g);

        AttributedString iterator = new AttributedString(this.getTitle());
        iterator.addAttribute(TextAttribute.FAMILY, "Play");
        iterator.addAttribute(TextAttribute.FOREGROUND, Color.WHITE);
        iterator.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        iterator.addAttribute(TextAttribute.SIZE, 25);
        g.drawString(iterator.getIterator(), 15, 40);

        for (int i = 1; i < board.getBallsAmount(); i++) {
            float radiusOffset = 1.2f;
            Arc2D.Float arc = new Arc2D.Float((i * 3.0f - radiusOffset) * scale + middleX, ((-board.getHeight() - 10 - 2 * radiusOffset) * scale / 2) + middleY, 2 * (radiusOffset) * scale, 2 * (radiusOffset) * scale, 0, 360, Arc2D.PIE);
            g.setColor(Color.WHITE);
            ((Graphics2D) g).fill(arc);
        }



        for (IBallSet set : this.getBallSets()) {
            for (int i = 0; i < board.getBallsAmount(); i++) {
                float[] info = set.getBallInformation(i);
                g.setColor(set.getBallColor(i));
                this.drawBall(info, i, g);
            }
        }
    }

    /**
     * Affiche une boule
     * @param info les informations de la boules (5 floats)
     * @param i l'indice de la boule
     * @param g le graphics
     */
    private void drawBall(float[] info, int i, Graphics g) {
        float x = info[0] * scale - scale + middleX;
        float y = info[1] * scale - scale + middleY;
        float r = 2.0f * scale;
        Arc2D.Float positionArc = new Arc2D.Float(x, y, r, r, 0, 360, Arc2D.PIE);
        ((Graphics2D) g).fill(positionArc);
        if (i >= 8) {
            g.setColor(Color.WHITE);
            int offset = Math.round(scale / 4.0f);
            Arc2D.Float arc = new Arc2D.Float(x + offset, y + offset, r - 2 * offset, r - 2 * offset, 0, 360, Arc2D.PIE);
            ((Graphics2D) g).fill(arc);
            // On dessine un cercle blanc autour du numéro de rayon plus petit d'un facteur 2/3, centré sur le centre de la bille
//                    g.fillOval(x + offset, y + offset, r - 2 * offset, r - 2 * offset);
        }
        g.setColor(Color.BLACK);
        if (i > 0) {
            int offset = Math.round(scale * (i < 10 ? 2.0f / 3.0f : 1.0f / 3.0f));
            g.drawString(String.valueOf(i), Math.round(x) + offset, Math.round(y) + Math.round(scale * 3.0f / 2.0f));
        }
    }

    /**
     * Affiche les bordures
     * @param g le graphics
     */
    private void drawBorders(Graphics2D g) {
        for (int i = 0; i < 6; i++) {
            Polygon polygon = borders[i];
            g.setColor(GREEN);
            g.fill(polygon);
            g.setColor(Color.BLACK);
            g.draw(polygon);
        }
    }

    /**
     * Enregistre les formes polygonales des bordures horizontales
     * @param xSignum 1 pour côté gauche, -1 pour côté droit
     * @param ySignum 1 pour en haut, -1 pour en bas
     * @param holeDiameter diamètre du trou
     * @param i indice du polygone
     */
    private void registerHorizontalBorders(int xSignum, int ySignum, float holeDiameter, int i) {
        Polygon polygon = new Polygon();
        polygon.addPoint(middleX + xSignum * Math.round(scale), (int) ((ySignum * (board.getHeight() + holeDiameter)) * scale / 2) + middleY);
        polygon.addPoint(middleX + xSignum * Math.round(2.5f * scale), (int) ((ySignum * board.getHeight()) * scale / 2) + middleY);
        polygon.addPoint((int) ((xSignum * (board.getWidth() - holeDiameter)) * scale / 2) + middleX, (int) ((ySignum * board.getHeight()) * scale / 2) + middleY);
        polygon.addPoint((int) ((xSignum * board.getWidth()) * scale / 2) + middleX, (int) ((ySignum * (board.getHeight() + holeDiameter)) * scale / 2) + middleY);
        borders[i] = polygon;
    }

    /**
     * Enregistre les formes polygonales des bordures verticales
     * @param xSignum 1 pour coté gauche, -1 pour coté droit
     * @param holeDiameter diamètre du trou
     * @param i indice du polygone
     */
    private void registerVerticalBorders(int xSignum, float holeDiameter, int i) {
        Polygon polygon = new Polygon();
        polygon.addPoint((int) ((xSignum * board.getWidth()) * scale / 2) + middleX, (int) (((board.getHeight() - holeDiameter)) * scale / 2) + middleY);
        polygon.addPoint((int) ((xSignum * board.getWidth()) * scale / 2) + middleX, (int) (((-board.getHeight() + holeDiameter)) * scale / 2) + middleY);
        polygon.addPoint((int) ((xSignum * (board.getWidth() + holeDiameter)) * scale / 2) + middleX, (int) ((-board.getHeight()) * scale / 2) + middleY);
        polygon.addPoint((int) ((xSignum * (board.getWidth() + holeDiameter)) * scale / 2) + middleX, (int) ((board.getHeight()) * scale / 2) + middleY);
        borders[i] = polygon;
    }

    /**
     * Représente un ensemble de boules positionnées et colorées.
     */
    public interface IBallSet {
        /**
         * @param i l'indice de la boule
         * @return la représentation informatique de la boule i
         */
        float[] getBallInformation(int i);

        /**
         * @param i l'indice de la boule
         * @return la couleur de la boule i
         */
        Color getBallColor(int i);

        /**
         * @return le plateau de jeu
         */
        Board getBoard();
    }

    /**
     * Implémentation par défaut de l'interface IBallSet
     */
    public class DefaultBallSet implements IBallSet {

        @Override
        public float[] getBallInformation(int i) {
            return board.getBallInformation(i);
        }

        @Override
        public Color getBallColor(int i) {
            if (i == 0) {
                return Color.WHITE;
            }
            return BALL_COLORS[(i - 1) % 8];
        }

        @Override
        public Board getBoard() {
            return board;
        }
    }

}
