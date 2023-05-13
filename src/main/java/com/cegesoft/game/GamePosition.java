package com.cegesoft.game;

import com.cegesoft.opencl.CLField;
import com.cegesoft.opencl.CLFunction;
import com.cegesoft.opencl.CLHandler;
import com.cegesoft.ui.GameFrame;
import com.cegesoft.util.NDArrayUtil;
import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;
import org.bridj.Pointer;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GamePosition {
    private final static int ANGLE_PARTITION = 1;
    private final static int NORM_PARTITION = 100;

    private final int[] BALL_DATA_SHAPE;
    private final CLField<Integer> anglesField;
    private final CLField<Integer> normField;
    private final CLField<Float> ballsField;
    private final CLField<Float> gamesInformationField;
    private float[] currentGameInformation = new float[Board.GAME_DATA_SIZE * ANGLE_PARTITION * NORM_PARTITION];
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
        this.function = new CLFunction(board.getFile(), "move_2", this.ballsField, board.getBallBufferSizeField(), board.getBallsAmountField(), board.getAlphaField(), board.getHeightField(), board.getWidthField(), board.getTimeStepField(), this.gamesInformationField, board.getDebugField(), anglesField, normField, new CLField<>(board.getHandler(), Short.class, (short) 1));
        this.BALL_DATA_SHAPE = new int[] {Board.BALL_BUFFER_SIZE, this.board.getBallsAmount(), ANGLE_PARTITION, NORM_PARTITION};
    }

    private void init(CLField<Float> ballsField) {
        CLFunction function1 = new CLFunction(this.board.getFile(), "copy_buffer", ballsField, this.ballsField, this.board.getBallBufferSizeField(), this.board.getBallsAmountField(), this.anglesField, this.board.getDebugField());
        function1.call(this.queue, new int[] {this.board.getBallsAmount(), ANGLE_PARTITION, NORM_PARTITION}).waitFor();
    }

    private float[] getGamesInformation() {
        Pointer<Float> pointer = ((CLBuffer<Float>) this.gamesInformationField.getArgument()).read(this.queue);
        return pointer.getFloats();
    }

    public float[] getBallInformation(int angle, int norm, int i) {
        Pointer<Float> pointer = ((CLBuffer<Float>) this.ballsField.getArgument()).read(this.queue);
        float[] info = new float[Board.BALL_BUFFER_SIZE];
        for (int j = 0; j < Board.BALL_BUFFER_SIZE; j++) {
            info[j] = pointer.get(NDArrayUtil.getIndex(this.BALL_DATA_SHAPE, j, i, angle, norm));
        }
        return info;
    }

    private Stream<Float> streamValues(CLField<?> field) {
        float[] values = ((CLBuffer<Float>) field.getArgument()).read(this.queue).getFloats();
        Float[] pFloat = new Float[values.length];
        for (int i = 0; i < values.length; i++) {
            pFloat[i] = values[i];
        }
        return Arrays.stream(pFloat);
    }

    public List<Integer> move(GameFrame.GamePanel panel, GameFrame frame) {
        panel.setBallInformationFunction(i -> this.getBallInformation(0, 50, i));

        for (int i = 0; i < 6000; i++) {
            if (i == 1) {
                this.function.setArgument(11, new CLField<>(this.board.getHandler(), Short.class, (short) 0));
            }
            this.function.call(this.queue, new int[] {this.board.getBallsAmount(), ANGLE_PARTITION, NORM_PARTITION}).waitFor();
            frame.repaint();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        this.function.setArgument(11, new CLField<>(this.board.getHandler(), Short.class, (short) 1));

        panel.setBallInformationFunction(null);
        this.currentGameInformation = this.getGamesInformation();
//        return IntStream.range(0, ANGLE_PARTITION * NORM_PARTITION).boxed().collect(Collectors.toList());
        IntStream stream = IntStream.range(0, ANGLE_PARTITION * NORM_PARTITION);
        return stream.boxed().sorted((o1, o2) -> {
                        int angle1 = o1 / NORM_PARTITION;
                        int angle2 = o2 / NORM_PARTITION;
                        int norm1 = o1 % NORM_PARTITION;
                        int norm2 = o2 % NORM_PARTITION;
                        return -Float.compare(this.currentGameInformation[(angle1 + ANGLE_PARTITION * norm1) * Board.GAME_DATA_SIZE + 1], this.currentGameInformation[(angle2 + ANGLE_PARTITION * norm2) * Board.GAME_DATA_SIZE + 1]);
                })
                .collect(Collectors.toList());
    }


    public float getScore(int bestAngle) {
        return this.currentGameInformation[(bestAngle / NORM_PARTITION + ANGLE_PARTITION * (bestAngle % NORM_PARTITION)) * Board.GAME_DATA_SIZE + 1];
    }

    public float getNorm(int bestShot) {
        return bestShot % NORM_PARTITION * 300.0f / NORM_PARTITION;
    }

    public float getAngle(int bestShot) {
        return (bestShot / NORM_PARTITION) * 360.f / ANGLE_PARTITION;
    }
}
