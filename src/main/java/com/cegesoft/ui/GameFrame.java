package com.cegesoft.ui;

import com.cegesoft.Main;
import com.cegesoft.game.Board;
import com.cegesoft.ui.panels.ClassicGamePanel;
import lombok.Getter;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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

        if (System.console() != null) {
            this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    try {
                        Main.listenCommand();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        } else {
            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        }


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
