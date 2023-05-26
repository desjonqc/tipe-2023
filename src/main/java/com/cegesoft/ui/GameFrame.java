package com.cegesoft.ui;

import com.cegesoft.Main;
import com.cegesoft.game.Board;
import com.cegesoft.game.BoardSimulation;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Arc2D;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class GameFrame extends JFrame {

    private final float scale;
    private final int middleX, middleY;
    private final Board board;

    public GameFrame(Board board) {
        this.board = board;

        this.scale = 1000 / board.getWidth();

        this.setSize(1100, 700);
        this.setLayout(null);
        this.middleX = this.getWidth() / 2;
        this.middleY = this.getHeight() / 2 + 30;
        this.setContentPane(new GameFrame.GamePanel());
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public class GamePanel extends JPanel {
        public final Color GREEN = new Color(64, 152, 68);
        public final float holeDiameter = 4.2f;

        private final Image background;

        private Function<Integer, float[]> ballInformationFunction;

        public GamePanel() {
            GameFrame.this.addKeyListener(new java.awt.event.KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                }

                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                        Thread thread = new Thread(() -> {
                            System.out.println("Calculating best shot...");
                            long start = System.currentTimeMillis();
                            BoardSimulation position = new BoardSimulation(board, board.getBallsField(), board.getDefaultQueue());
                            List<Integer> betterAngles = position.move(GamePanel.this, GameFrame.this);
                            int bestShot = betterAngles.get(0);
                            float bestAngle = position.getAngle(bestShot);
                            float bestScore = position.getScore(bestShot);
                            float bestNorm = position.getNorm(bestShot);
                            System.out.println("Angle : " + bestAngle);
                            System.out.println("Norme : " + bestNorm);
                            System.out.println("Score : " + bestScore);
                            System.out.println("Temps : " + (System.currentTimeMillis() - start) + "ms");

                            board.setBallVelocity(0, (float) (Math.cos(Math.toRadians(bestAngle)) * bestNorm), (float) (Math.sin(Math.toRadians(bestAngle)) * bestNorm));
                            Board.bestShot = false;
                        }, "PLAY-RESEARCH");
                        thread.start();
                        Board.bestShot = true;
                    }
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        float bestNorm = 300;
                        float bestAngle = 3;
                        board.setBallVelocity(0, (float) (Math.cos(Math.toRadians(bestAngle)) * bestNorm), (float) (Math.sin(Math.toRadians(bestAngle)) * bestNorm));
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {

                }
            });
            this.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (board.everyBallStopped()) {
                        Main.count = 0;
                        float[] info = board.getBallInformation(0);
                        float vx = (e.getX() - middleX - info[0] * scale) * 5 / scale;
                        float vy = (e.getY() - middleY - info[1] * scale) * 5 / scale;
                        board.setBallVelocity(0, vx, vy);
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });

            registerHorizontalBorders(1, 1, holeDiameter, 0);
            registerHorizontalBorders(1, -1, holeDiameter, 1);
            registerHorizontalBorders(-1, 1, holeDiameter, 2);
            registerHorizontalBorders(-1, -1, holeDiameter, 3);

            registerVerticalBorders(1, holeDiameter, 4);
            registerVerticalBorders(-1, holeDiameter, 5);

            try {
                background = ImageIO.read(ClassLoader.getSystemResource("wood.png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


            g.setColor(new Color(1, 1, 1));
            g.fillRect(-5, -5, this.getWidth() + 5, this.getHeight() + 5);

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

            for (int i = 1; i < board.getBallsAmount(); i++) {
                float radiusOffset = 1.2f;
                Arc2D.Float arc = new Arc2D.Float((i * 3.0f - radiusOffset) * scale + middleX, ((-board.getHeight() - 10 - 2 * radiusOffset) * scale / 2) + middleY, 2*(radiusOffset) * scale, 2*(radiusOffset) * scale, 0, 360, Arc2D.PIE);
                g.setColor(Color.WHITE);
                ((Graphics2D) g).fill(arc);
            }


            Color[] ballColors = new Color[] {
                    new Color(210, 190, 19),
                    new Color(2, 86, 162),
                    new Color(175, 15, 12),
                    new Color(52, 43, 101),
                    new Color(207, 126, 58),
                    new Color(17, 104, 64),
                    new Color(138, 46, 48),
                    new Color(0, 0, 0)
            };
            for (int i = 0; i < board.getBallsAmount(); i++) {
                float[] info = this.ballInformationFunction == null ? board.getBallInformation(i) : this.ballInformationFunction.apply(i);
                if (i == 0)
                    g.setColor(Color.WHITE);
                else
                    g.setColor(info[4] <= 0 ? ballColors[(i - 1) % 8] : Color.BLUE);

                float x = info[0] * scale - scale + middleX;
                float y = info[1] * scale - scale + middleY;
                float r = 2.0f * scale;
                Arc2D.Float positionArc = new Arc2D.Float(x, y, r, r, 0, 360, Arc2D.PIE);
                ((Graphics2D)g).fill(positionArc);
                if (i >= 8) {
                    g.setColor(Color.WHITE);
                    int offset = Math.round(scale / 4.0f);
                    Arc2D.Float arc = new Arc2D.Float(x + offset, y + offset, r - 2 * offset, r - 2 * offset, 0, 360, Arc2D.PIE);
                    ((Graphics2D)g).fill(arc);
                    // On dessine un cercle blanc autour du numéro de rayon plus petit d'un facteur 2/3, centré sur le centre de la bille
//                    g.fillOval(x + offset, y + offset, r - 2 * offset, r - 2 * offset);
                }
                g.setColor(Color.BLACK);
                if (i > 0) {
                    int offset = Math.round(scale * (i < 10 ? 2.0f / 3.0f : 1.0f / 3.0f));
                    g.drawString(String.valueOf(i), Math.round(x) + offset, Math.round(y) + Math.round(scale * 3.0f / 2.0f));
                }
            }
        }

        public void setBallInformationFunction(Function<Integer, float[]> function) {
            this.ballInformationFunction = function;
        }

        private Polygon[] borders = new Polygon[6];

        private void drawBorders(Graphics2D g) {
            for (int i = 0; i < 6; i ++) {
                Polygon polygon = borders[i];
                g.setColor(GREEN);
                g.fill(polygon);
                g.setColor(Color.BLACK);
                g.draw(polygon);
            }
        }

        private void registerHorizontalBorders(int xSignum, int ySignum, float holeDiameter, int i) {
            Polygon polygon = new Polygon();
            polygon.addPoint(middleX + xSignum * Math.round(scale), (int) ((ySignum * (board.getHeight() + holeDiameter)) * scale / 2) + middleY);
            polygon.addPoint(middleX + xSignum * Math.round(2.5f * scale), (int) ((ySignum * board.getHeight()) * scale / 2) + middleY);
            polygon.addPoint((int) ((xSignum * (board.getWidth() - holeDiameter)) * scale / 2) + middleX, (int) ((ySignum * board.getHeight()) * scale / 2) + middleY);
            polygon.addPoint((int) ((xSignum * board.getWidth()) * scale / 2) + middleX, (int) ((ySignum * (board.getHeight() + holeDiameter)) * scale / 2) + middleY);
            borders[i] = polygon;
        }

        private void registerVerticalBorders(int xSignum, float holeDiameter, int i) {
            Polygon polygon = new Polygon();
            polygon.addPoint((int) ((xSignum * board.getWidth()) * scale / 2) + middleX, (int) (((board.getHeight() - holeDiameter)) * scale / 2) + middleY);
            polygon.addPoint((int) ((xSignum * board.getWidth()) * scale / 2) + middleX, (int) (((-board.getHeight() + holeDiameter)) * scale / 2) + middleY);
            polygon.addPoint((int) ((xSignum * (board.getWidth() + holeDiameter)) * scale / 2) + middleX, (int) ((-board.getHeight()) * scale / 2) + middleY);
            polygon.addPoint((int) ((xSignum * (board.getWidth() + holeDiameter)) * scale / 2) + middleX, (int) ((board.getHeight()) * scale / 2) + middleY);
            borders[i] = polygon;
        }
    }
}
