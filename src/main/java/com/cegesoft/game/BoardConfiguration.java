package com.cegesoft.game;

import com.cegesoft.opencl.CLHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Configuration d'une table de jeu.
 */
@Getter
@AllArgsConstructor
public class BoardConfiguration {

    private final int width;
    private final int height;
    private final int ballsAmount;
    private final float alpha;
    private final CLHandler handler;

}
