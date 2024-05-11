package com.cegesoft.ui;

import com.cegesoft.Main;
import com.cegesoft.game.Board;
import com.cegesoft.ui.panels.ClassicGamePanel;
import lombok.Getter;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Function;

/**
 * Fenêtre abstraite configurable avec un AbstractGamePanel
 * @see AbstractGamePanel
 */
public class GameFrame extends JFrame {

    @Getter
    private static GameFrame frameInstance;

    protected final float scale;
    protected final int middleX, middleY;
    private final AbstractGamePanel classicGamePanel;

    /**
     * Crée une fenêtre de jeu avec un affichage de jeu paramétré
     * @param board le plateau de jeu
     * @param defaultPanel l'affichage de jeu par défaut. Sa création dépend de l'instance de GameFrame, donc celui-ci est créé par le GameFrame au moment de l'initialisation.
     * @see AbstractGamePanel
     */
    public GameFrame(Board board, Function<GameFrame, AbstractGamePanel> defaultPanel) {
        frameInstance = this;

        this.scale = 1000 / board.getWidth();

        this.setSize(1100, 700);
        this.setLayout(null);
        this.middleX = this.getWidth() / 2;
        this.middleY = this.getHeight() / 2 + 30;
        this.setContentPane(this.classicGamePanel = defaultPanel.apply(this));
        this.classicGamePanel.registerListeners();

        if (Main.CONSOLE_RUN) {
            this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    Main.CURRENT_APPLICATION.stop();
                    Main.listenCommand();
                }
            });
        } else {
            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        }


        this.setVisible(true);
    }

    /**
     * Crée une fenêtre de jeu avec un affichage de jeu par défaut
     * @param board le plateau de jeu
     */
    public GameFrame(Board board) {
        this(board, frame -> new ClassicGamePanel(frame, board));
    }

    /**
     * Change l'interface de la fenêtre
     * @param panel, la nouvelle interface à afficher
     */
    public void setCurrentPanel(AbstractGamePanel panel) {
        ((AbstractGamePanel) this.getContentPane()).unregisterListeners();
        this.setContentPane(panel);
        panel.registerListeners();
        this.validate();
    }

    /**
     * Réinitialise la fenêtre à sa première interface
     */
    public void resetPanel() {
        ((AbstractGamePanel) this.getContentPane()).unregisterListeners();
        this.setContentPane(this.classicGamePanel);
        this.classicGamePanel.registerListeners();
    }
}
