package com.cegesoft.ui;

import com.cegesoft.Main;
import com.cegesoft.game.Board;
import com.cegesoft.game.GamePosition;
import org.bridj.Pointer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class GameFrame extends JFrame {

    private final float scale;
    private final int middleX, middleY;
    private final Board board;

    public GameFrame(Board board) {
        this.board = board;

        this.scale = 1000 / board.getWidth();

        this.setSize(1070, (int) (this.scale * board.getHeight()) + 79);
        this.setLayout(null);
        this.middleX = this.getWidth() / 2;
        this.middleY = this.getHeight() / 2;
        this.setContentPane(new GameFrame.GamePanel());
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public class GamePanel extends JPanel {

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
                            GamePosition position = new GamePosition(board, board.getBallsField(), board.getDefaultQueue());
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
                        float bestNorm = 249;
                        float bestAngle = 305;
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

        }
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.setColor(new Color(1, 1, 1, 0.8f));
            g.fillRect(0, 0, this.getWidth(), this.getHeight());

            g.setColor(new Color(24, 132, 3));
            g.fillRect((int) (-board.getWidth() * scale / 2) + middleX, (int) (-board.getHeight() * scale / 2) + middleY, (int) (board.getWidth() * scale), (int) (board.getHeight() * scale));

            g.setColor(Color.BLACK);
            g.fillArc((int) ((-board.getWidth() - 5.5) * scale / 2) + middleX, (int) ((-board.getHeight() - 5.5) * scale / 2) + middleY, (int) (5.5 * scale), (int) (5.5 * scale), 270, 90);
            g.fillArc((int) ((board.getWidth() - 5.5) * scale / 2) + middleX, (int) ((-board.getHeight() - 5.5) * scale / 2) + middleY, (int) (5.5 * scale), (int) (5.5 * scale), 180, 90);
            g.fillArc((int) ((-board.getWidth() - 5.5) * scale / 2) + middleX, (int) ((board.getHeight() - 5.5) * scale / 2) + middleY, (int) (5.5 * scale), (int) (5.5 * scale), 0, 90);
            g.fillArc((int) ((board.getWidth() - 5.5) * scale / 2) + middleX, (int) ((board.getHeight() - 5.5) * scale / 2) + middleY, (int) (5.5 * scale), (int) (5.5 * scale), 90, 90);
            g.fillArc((int) (middleX - 2.75 * scale), (int) ((board.getHeight() - 5.5) * scale / 2) + middleY, (int) (5.5 * scale), (int) (5.5 * scale), 0, 180);
            g.fillArc((int) (middleX - 2.75 * scale), (int) ((-board.getHeight() - 5.5) * scale / 2) + middleY, (int) (5.5 * scale), (int) (5.5 * scale), 180, 180);

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
                g.fillOval((int) (info[0] * scale - scale) + middleX, (int)(info[1] * scale - scale) + middleY, (int)(2 * scale), (int) (2 * scale));
                if (i >= 8) {
                    g.setColor(Color.WHITE);
                    // On dessine un cercle blanc autour du numéro de rayon plus petit d'un facteur 2/3, centré sur le centre de la bille
                    g.fillOval((int) (info[0] * scale - scale * 2 / 3) + middleX, (int) (info[1] * scale - scale * 2 / 3) + middleY, (int) (scale * 4 / 3), (int) (scale * 4 / 3));
                }
                g.setColor(Color.BLACK);
                if (i > 0)
                    g.drawString(String.valueOf(i), (int) (info[0] * scale - scale / (i < 10 ? 3 : 1.5)) + middleX, (int) (info[1] * scale + scale / 2) + middleY);
            }
        }

        public void setBallInformationFunction(Function<Integer, float[]> function) {
            this.ballInformationFunction = function;
        }
    }
}
