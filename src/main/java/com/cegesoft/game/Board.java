package com.cegesoft.game;

import com.cegesoft.Main;
import com.cegesoft.game.position.BoardPosition;
import com.cegesoft.log.Logger;
import com.cegesoft.opencl.CLFunction;
import com.cegesoft.opencl.CLHandler;
import com.cegesoft.ui.GameFrame;
import org.bridj.Pointer;

public class Board extends BoardStructure {

    public static final BoardPosition INITIAL_POSITION = new BoardPosition(new float[]{
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
            27, 5, 0, 0}, (i, j) -> i + 4 * j);
    public static boolean bestShot = false;
    private int stopCounter = 0;
    private boolean firstEmptyTick = true;

    public Board(CLHandler handler, float height, float width, int ballsAmount, float alpha) {
        super(handler, height, width, ballsAmount, alpha, (long) ballsAmount * BoardStructure.BALL_BUFFER_SIZE, BoardStructure.GAME_DATA_SIZE);
    }

    public Board(BoardConfiguration configuration, BoardPosition initialPosition) {
        this(configuration.getHandler(), configuration.getHeight(), configuration.getWidth(), configuration.getBallsAmount(), configuration.getAlpha());
        this.initialise(initialPosition);
    }

    @Override
    protected CLFunction createFunction() {
        return new CLFunction(this.file, "move", this.ballsField, this.editBallsField, this.ballBufferSizeField, this.ballsAmountField,
                this.alphaField, this.heightField, this.widthField, this.timeStepField, this.gameInformationField, this.debugField);
    }

    @Override
    protected void initialise_(BoardPosition position) {
        this.ballsField = position.toBufferField(this.handler, this.queue);
    }

    public float[] getBallInformation(int i) {
        Pointer<Float> pointer = this.ballsField.getArgument().read(queue);
        float[] info = new float[BoardStructure.BALL_BUFFER_SIZE];
        for (int j = 0; j < BoardStructure.BALL_BUFFER_SIZE; j++) {
            info[j] = pointer.get((long) i * BoardStructure.BALL_BUFFER_SIZE + j);
        }
        return info;
    }

    public boolean everyBallStopped() {
        if (currentGameInformation[0] != 0) {
            stopCounter = 0;
        } else {
            stopCounter++;
        }
        return stopCounter > 20;
    }

    public void tick() {
        if (bestShot) {
            return;
        }
        if (!everyBallStopped()) {
            this.gameInformationField.setValue(this.queue, 0L, 0f).waitFor();
            this.function.call(this.queue, new int[]{this.getBallsAmount()}).waitFor();
            invertEdit();
            this.function.setArgument(0, this.ballsField);
            this.function.setArgument(1, this.editBallsField);
            this.updateGameInformation();
            GameFrame.getFrameInstance().repaint();
            firstEmptyTick = true;
        } else if (firstEmptyTick) {
            firstEmptyTick = false;
            this.gameInformationField.setValue(this.queue, 1L, 0f).waitFor();
            float[] info = getBallInformation(0);
            if (info[1] < -this.getHeight() / 2 - 1) {
                ballsField.setValue(queue, 1, 0.0f);
                ballsField.setValue(queue, 0, INITIAL_POSITION.getPosition()[0]);
                ballsField.setValue(queue, 4, 0.0f);
            }
            if (currentGameInformation[1] > 0) {
                Logger.getLogger().println("Player 1 won");
            } else if (currentGameInformation[1] < 0) {
                Logger.getLogger().println("Player 2 won");
            } else {
                Logger.getLogger().println("Draw");
            }
        }
    }


    public void setBallVelocity(int i, float vx, float vy) {
        Pointer<Float> pointer = this.ballsField.getArgument().read(this.getQueue());
        pointer.set((long) i * BoardStructure.BALL_BUFFER_SIZE + 2, vx);
        pointer.set((long) i * BoardStructure.BALL_BUFFER_SIZE + 3, vy);
        this.getCurrentGameInformation()[0] = (float) Math.sqrt(vx * vx + vy * vy);
        this.stopCounter = 0;
        this.ballsField.getArgument().write(this.getQueue(), pointer, false).waitFor();
    }

    public void setPosition(BoardPosition position) {
        Pointer<Float> pointer = this.ballsField.getArgument().read(this.getQueue());
        for (int i = 0; i < this.getBallsAmount(); i++) {
            float[] ballPosition = position.getBallPosition(i);
            pointer.set((long) i * BoardStructure.BALL_BUFFER_SIZE, ballPosition[0]);
            pointer.set((long) i * BoardStructure.BALL_BUFFER_SIZE + 1, ballPosition[1]);
        }
        this.ballsField.getArgument().write(this.getQueue(), pointer, false).waitFor();
        GameFrame.getFrameInstance().repaint();
    }

    public BoardPosition savePosition() {
        return new BoardPosition(this);
    }
}
