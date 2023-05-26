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
import java.util.List;

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
    public final static int GAME_DATA_SIZE = 2;
    private final CLField<Integer> ballBufferSizeField;
    public final static float TIME_STEP = 0.001f;
    private final CLField<Float> timeStepField;

    private final float alpha = -0.0012f;
    private final float height, width;
    private final CLField<Float> alphaField, heightField, widthField;
    private CLField<Float> ballsField;
    private CLField<Float> editBallsField;
    private final int ballsAmount;
    private final CLField<Integer> ballsAmountField;
    private final CLQueue defaultQueue;
    private final CLHandler handler;
    private final CLFile file;
    private final CLField<Float> debugField;

    public CLFunction moveFunction;

    private final CLField<Float> gameInformationField;
    private float[] currentGameInformation = new float[GAME_DATA_SIZE];

    public Board(CLHandler handler, float height, float width, int ballsAmount) throws IOException {
        this.height = height;
        this.width = width;
        this.handler = handler;
        this.defaultQueue = handler.createQueue();
        this.file = new CLFile("board.cl", handler.getContext());
        this.ballsField = new CLField<>(handler, CLMem.Usage.InputOutput, Float.class, (long) ballsAmount * BALL_BUFFER_SIZE);
        this.editBallsField = new CLField<>(handler, CLMem.Usage.InputOutput, Float.class, (long) ballsAmount * BALL_BUFFER_SIZE);
        this.ballsAmount = ballsAmount;
        this.ballBufferSizeField = new CLField<>(this.handler, Integer.class, BALL_BUFFER_SIZE);
        this.timeStepField = new CLField<>(this.handler, Float.class, TIME_STEP);
        this.alphaField = new CLField<>(this.handler, Float.class, this.alpha);
        this.heightField = new CLField<>(this.handler, Float.class, this.height);
        this.widthField = new CLField<>(this.handler, Float.class, this.width);
        this.ballsAmountField = new CLField<>(this.handler, Integer.class, this.ballsAmount);
        this.debugField = new CLField<>(handler, CLMem.Usage.InputOutput, Float.class, 20);
        this.gameInformationField = new CLField<>(handler, CLMem.Usage.InputOutput, Float.class, GAME_DATA_SIZE);
    }

    public void initialise(float[] positions) {
        Pointer<Float> pointer = ((CLBuffer<Float>) this.ballsField.getArgument()).read(defaultQueue);
        for (int i = 0; i < ballsAmount; i ++) {
            pointer.set((long) i * BALL_BUFFER_SIZE, positions[4 * i]);
            pointer.set((long) i * BALL_BUFFER_SIZE + 1, positions[4 * i + 1]);
            pointer.set((long) i * BALL_BUFFER_SIZE + 2, positions[4 * i + 2]);
            pointer.set((long) i * BALL_BUFFER_SIZE + 3, positions[4 * i + 3]);
        }
        ((CLBuffer<Float>) this.ballsField.getArgument()).write(defaultQueue, pointer, false).waitFor();

        this.moveFunction = new CLFunction(this.file, "move", this.ballsField, this.editBallsField, this.ballBufferSizeField, this.ballsAmountField,
                this.alphaField, this.heightField, this.widthField, this.timeStepField, this.gameInformationField, this.debugField);
    }

    public float[] getBallInformation(int i) {
        Pointer<Float> pointer = ((CLBuffer<Float>) this.ballsField.getArgument()).read(defaultQueue);
        float[] info = new float[BALL_BUFFER_SIZE];
        for (int j = 0; j < BALL_BUFFER_SIZE; j++) {
            info[j] = pointer.get((long) i * BALL_BUFFER_SIZE + j);
        }
        return info;
    }

    private float[] getGameInformation() {
        Pointer<Float> pointer = ((CLBuffer<Float>)this.gameInformationField.getArgument()).read(defaultQueue);
        return pointer.getFloats();
    }

    private void invertEdit() {
        CLField<Float> temp = this.ballsField;
        this.ballsField = this.editBallsField;
        this.editBallsField = temp;
    }

    private int stopCounter = 0;
    public boolean everyBallStopped() {
        if (currentGameInformation[0] != 0) {
            stopCounter = 0;
        } else {
            stopCounter++;
        }
        return stopCounter > 20;
    }

    private boolean firstEmptyTick = true;
    public static boolean bestShot = false;
    public void tick(GameFrame frame) {
        if (bestShot) {
            return;
        }
        if (!everyBallStopped()) {
            Main.count ++;
            this.gameInformationField.setValue(this.defaultQueue, 0L, 0f).waitFor();
            this.moveFunction.call(this.defaultQueue, new int[] {this.ballsAmount}).waitFor();
            invertEdit();
            this.moveFunction.setArgument(0, this.ballsField);
            this.moveFunction.setArgument(1, this.editBallsField);
            currentGameInformation = getGameInformation();
            frame.repaint();
            firstEmptyTick = true;
        } else if (firstEmptyTick) {
            firstEmptyTick = false;
            this.gameInformationField.setValue(this.defaultQueue, 1L, 0f).waitFor();
            System.out.println("Count : " + Main.count);
            float[] info = getBallInformation(0);
            if (info[1] < -height / 2 - 1) {
                ballsField.setValue(defaultQueue, 1, 0.0f);
                ballsField.setValue(defaultQueue, 0, INITIAL_POSITION[0]);
                ballsField.setValue(defaultQueue, 4, 0.0f);
            }
            if (currentGameInformation[1] > 0) {
                System.out.println("Player 1 won");
            } else if (currentGameInformation[1] < 0) {
                System.out.println("Player 2 won");
            } else {
                System.out.println("Draw");
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

    public float[] getCurrentGameInformation() {
        return currentGameInformation;
    }

    public void setBallVelocity(int i, float vx, float vy) {
        Pointer<Float> pointer = ((CLBuffer<Float>)this.ballsField.getArgument()).read(this.getDefaultQueue());
        pointer.set((long) i * BALL_BUFFER_SIZE + 2, vx);
        pointer.set((long) i * BALL_BUFFER_SIZE + 3, vy);
        this.getCurrentGameInformation()[0] = (float) Math.sqrt(vx * vx + vy * vy);
        this.stopCounter = 0;
        ((CLBuffer<Float>)this.ballsField.getArgument()).write(this.getDefaultQueue(), pointer, false).waitFor();
    }
}
