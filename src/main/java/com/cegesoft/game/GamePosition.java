package com.cegesoft.game;

import com.cegesoft.opencl.CLField;
import com.cegesoft.opencl.CLFunction;
import com.cegesoft.opencl.CLHandler;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLQueue;

public class GamePosition {

    private final CLField<Float> ballsField;
    private final CLQueue queue;
    private final Board board;
    private final CLFunction function;

    public GamePosition(Board board, CLField<Float> ballsField, CLQueue queue, int norme) {
        this.board = board;
        this.queue = queue;
        this.ballsField = ballsField.duplicate(this.queue);
        this.function = new CLFunction(board.getFile(), "move_2", this.ballsField, board.getBallBufferSizeField(), board.getBallsAmountField(), board.getAlphaField(), board.getHeightField(), board.getWidthField(), board.getTimeStepField(), board.getDebugField(), new CLField<>(board.getHandler(), Integer.class, norme));
    }

    public CLEvent move() {
        return this.function.call(this.queue, new int[] {this.board.getBallsAmount(), 7000, 3600});
    }


}
