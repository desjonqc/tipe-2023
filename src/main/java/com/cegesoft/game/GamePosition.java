package com.cegesoft.game;

import com.cegesoft.opencl.CLField;
import com.cegesoft.opencl.CLFunction;
import com.cegesoft.opencl.CLHandler;
import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;
import org.bridj.Pointer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GamePosition {
    private final static int ANGLE_PARTITION = 3600;
    private final static int NORM_PARTITION = 100;
    private final CLField<Integer> anglesField;
    private final CLField<Integer> normField;
    private final CLField<Float> ballsField;
    private final CLField<Float> gamesInformationField;
    private float[] currentGameInformation = new float[Board.GAME_DATA_SIZE * ANGLE_PARTITION];
    private final CLQueue queue;
    private final Board board;
    private final CLFunction function;

    public GamePosition(Board board, CLField<Float> ballsField, CLQueue queue) {
        this.board = board;
        this.queue = queue;
        this.anglesField = new CLField<>(board.getHandler(), Integer.class, ANGLE_PARTITION);
        this.normField = new CLField<>(board.getHandler(), Integer.class, NORM_PARTITION);
        this.ballsField = new CLField<>(board.getHandler(), CLMem.Usage.InputOutput, Float.class,  ((long) this.board.getBallsAmount() * Board.BALL_BUFFER_SIZE) * ANGLE_PARTITION * NORM_PARTITION);
        this.gamesInformationField = new CLField<>(board.getHandler(), CLMem.Usage.InputOutput, Float.class, (long) Board.GAME_DATA_SIZE * ANGLE_PARTITION * NORM_PARTITION);
        this.init(ballsField);
        this.function = new CLFunction(board.getFile(), "move_2", this.ballsField, board.getBallBufferSizeField(), board.getBallsAmountField(), board.getAlphaField(), board.getHeightField(), board.getWidthField(), board.getTimeStepField(), this.gamesInformationField, board.getDebugField(), anglesField, normField);
    }

    private void init(CLField<Float> ballsField) {
        CLFunction function1 = new CLFunction(this.board.getFile(), "copy_buffer", ballsField, this.ballsField, this.board.getBallBufferSizeField(), this.board.getBallsAmountField(), this.anglesField);
        function1.call(this.queue, new int[] {this.board.getBallsAmount(), NORM_PARTITION, ANGLE_PARTITION}).waitFor();
    }

    private float[] getGamesInformation() {
        Pointer<Float> pointer = ((CLBuffer<Float>) this.gamesInformationField.getArgument()).read(this.queue);
        return pointer.getFloats();
    }

    public List<Integer> move() {

        for (int i = 0; i < 8000; i++) {
            this.function.call(this.queue, new int[] {this.board.getBallsAmount(), NORM_PARTITION, ANGLE_PARTITION}).waitFor();
        }

        IntStream stream = IntStream.range(0, ANGLE_PARTITION * NORM_PARTITION);
        return stream.boxed().sorted((o1, o2) ->
                        Float.compare(this.currentGameInformation[o2 * Board.GAME_DATA_SIZE + 1], this.currentGameInformation[o1 * Board.GAME_DATA_SIZE + 1]))
                .collect(Collectors.toList());
    }


    public float getScore(int bestAngle) {
        return this.currentGameInformation[bestAngle * Board.GAME_DATA_SIZE + 1];
    }
}
