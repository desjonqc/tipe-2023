package com.cegesoft.ui;

import com.cegesoft.game.Board;
import com.cegesoft.ui.panels.ClassicGamePanel;
import lombok.Getter;

import javax.swing.*;

public class GameFrame extends JFrame {

    @Getter
    private static GameFrame frameInstance;

    protected final float scale;
    protected final int middleX, middleY;
    private ClassicGamePanel classicGamePanel;

    public GameFrame(Board board) {
        frameInstance = this;

        this.scale = 1000 / board.getWidth();

        this.setSize(1100, 700);
        this.setLayout(null);
        this.middleX = this.getWidth() / 2;
        this.middleY = this.getHeight() / 2 + 30;
        this.setContentPane(this.classicGamePanel = new ClassicGamePanel(this, board));
        this.classicGamePanel.registerListeners();
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public void setCurrentPanel(AbstractGamePanel panel) {
        ((AbstractGamePanel) this.getContentPane()).unregisterListeners();
        this.setContentPane(panel);
        panel.registerListeners();
        this.validate();
    }

    public void resetPanel() {
        ((AbstractGamePanel) this.getContentPane()).unregisterListeners();
        this.setContentPane(this.classicGamePanel);
        this.classicGamePanel.registerListeners();
    }
}
