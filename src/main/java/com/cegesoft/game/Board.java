package com.cegesoft.game;

import com.cegesoft.Main;
import com.cegesoft.opencl.CLField;
import com.cegesoft.opencl.CLFile;
import com.cegesoft.opencl.CLFunction;
import com.cegesoft.opencl.CLHandler;
import com.cegesoft.ui.GameFrame;
import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;
import org.bridj.Pointer;

import java.io.IOException;

public class Board {

    public static final float[] INITIAL_POSITION = new float[] {
            -35, 0, 0, 0f,
            20, 0, 0f, 0,

            21.75f, 1.25f, 0, 0,
            21.75f, -1.25f, 0, 0,

            23.5f, 2.5f, -0, 0,
            23.5f, 0, -0, 0,
            23.5f, -2.5f, -0, 0,

            25.25f, 3.75f, 0, 0,
            25.25f, 1.25f, 0, 0,
            25.25f, -1.25f, 0, 0,
            25.25f, -3.75f, 0, 0,

            27, -5, 0, 0,
            27, -2.5f, -0, 0,
            27, 0, 0, 0,
            27, 2.5f, -0, 0,
            27, 5, 0, 0};
    public final static int BALL_BUFFER_SIZE = 5;
    private final CLField<Integer> ballBufferSizeField;
    public final static float TIME_STEP = 0.001f;
    private final CLField<Float> timeStepField;

    private final float alpha = -0.001f;
    private final float height, width;
    private final CLField<Float> alphaField, heightField, widthField;
    private final CLField<Float> ballsField;
    private final CLBuffer<Float> ballsBuffer;
    private final int ballsAmount;
    private final CLField<Integer> ballsAmountField;
    private final CLQueue defaultQueue;
    private final CLHandler handler;
    private final CLFile file;
    private final CLField<Float> debugField;

    public CLFunction moveFunction;

    public Board(CLHandler handler, float height, float width, int ballsAmount) throws IOException {
        this.height = height;
        this.width = width;
        this.handler = handler;
        this.defaultQueue = handler.createQueue();
        this.file = new CLFile("board.cl", handler.getContext());
        this.ballsField = new CLField<>(handler, CLMem.Usage.InputOutput, Float.class, (long) ballsAmount * BALL_BUFFER_SIZE * 3600);
        this.ballsBuffer = (CLBuffer<Float>) this.ballsField.getArgument();
        this.ballsAmount = ballsAmount;
        this.ballBufferSizeField = new CLField<>(this.handler, Integer.class, BALL_BUFFER_SIZE);
        this.timeStepField = new CLField<>(this.handler, Float.class, TIME_STEP);
        this.alphaField = new CLField<>(this.handler, Float.class, this.alpha);
        this.heightField = new CLField<>(this.handler, Float.class, this.height);
        this.widthField = new CLField<>(this.handler, Float.class, this.width);
        this.ballsAmountField = new CLField<>(this.handler, Integer.class, this.ballsAmount);
        this.debugField = new CLField<>(handler, CLMem.Usage.InputOutput, Float.class, 10);
    }

    public void initialise(float[] positions) {
        Pointer<Float> pointer = this.ballsBuffer.read(defaultQueue);
        for (int i = 0; i < ballsAmount; i ++) {
            pointer.set((long) i * BALL_BUFFER_SIZE, positions[4 * i]);
            pointer.set((long) i * BALL_BUFFER_SIZE + 1, positions[4 * i + 1]);
            pointer.set((long) i * BALL_BUFFER_SIZE + 2, positions[4 * i + 2]);
            pointer.set((long) i * BALL_BUFFER_SIZE + 3, positions[4 * i + 3]);
        }
        this.ballsBuffer.write(defaultQueue, pointer, false).waitFor();

        this.moveFunction = new CLFunction(this.file, "move", this.ballsField, this.ballBufferSizeField, this.ballsAmountField,
                this.alphaField, this.heightField, this.widthField, this.timeStepField, this.debugField);
    }

    public float[] getBallInformation(int i) {
        Pointer<Float> pointer = this.ballsBuffer.read(defaultQueue);
        float[] info = new float[BALL_BUFFER_SIZE];
        for (int j = 0; j < BALL_BUFFER_SIZE; j++) {
            info[j] = pointer.get((long) i * BALL_BUFFER_SIZE + j);
        }
        return info;
    }

    public boolean everyBallStopped() {
        CLField<Boolean> result = new CLField<>(this.handler, CLMem.Usage.Output, Boolean.class, 1);
        CLFunction function = new CLFunction(this.file, "everyBallStopped", this.ballsField, this.ballBufferSizeField, result);
        function.call(this.defaultQueue, new int[] {this.ballsAmount});
        return !function.<Boolean>getOutput(2, this.defaultQueue).get(0);
    }

    public void tick(GameFrame frame) {
        if (!everyBallStopped()) {
            Main.count ++;
            this.moveFunction.call(this.defaultQueue, new int[] {this.ballsAmount}).waitFor();
            frame.repaint();
        } else {
            System.out.println("Count : " + Main.count);
            float[] info = getBallInformation(0);
            if (info[1] < -height / 2 - 1) {
                ballsField.setValue(defaultQueue, 1, 0.0f);
                ballsField.setValue(defaultQueue, 0, INITIAL_POSITION[0]);
            }
        }
    }

    public int getBallsAmount() {
        return ballsAmount;
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }

    public CLBuffer<Float> getBallsBuffer() {
        return ballsBuffer;
    }

    public CLQueue getDefaultQueue() {
        return defaultQueue;
    }

    public CLHandler getHandler() {
        return handler;
    }

    public CLField<Float> getAlphaField() {
        return alphaField;
    }

    public CLField<Float> getHeightField() {
        return heightField;
    }

    public CLField<Float> getWidthField() {
        return widthField;
    }

    public CLField<Float> getTimeStepField() {
        return timeStepField;
    }

    public CLField<Float> getDebugField() {
        return debugField;
    }

    public CLField<Integer> getBallBufferSizeField() {
        return ballBufferSizeField;
    }

    public CLField<Integer> getBallsAmountField() {
        return ballsAmountField;
    }

    public CLFile getFile() {
        return file;
    }

    public CLField<Float> getBallsField() {
        return ballsField;
    }
}
