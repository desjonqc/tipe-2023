package com.cegesoft.game;

import com.cegesoft.opencl.CLHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class BoardConfiguration {

    @Getter
    private final int width;
    @Getter
    private final int height;
    @Getter
    private final int ballsAmount;
    @Getter
    private final float alpha;
    @Getter
    private final CLHandler handler;

}
