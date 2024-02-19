package com.cegesoft.game;

import com.cegesoft.equations.EquationSolvingFunction;
import com.cegesoft.game.position.BoardPosition;
import com.cegesoft.opencl.*;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;
import lombok.Getter;
import lombok.Setter;
import org.bridj.Pointer;

public abstract class BoardStructure {

    public final static int BALL_BUFFER_SIZE = 5;
    public final static int GAME_DATA_SIZE = 2;

    public static float TIME_STEP = 0.001f;
    @Getter
    protected final CLConstantField<Float> timeStepField;
    @Getter
    protected final CLConstantField<Integer> ballBufferSizeField;
    @Getter
    protected final CLConstantField<Float> alphaField, heightField, widthField;
    @Getter
    protected CLBufferField<Float> ballsField;
    @Getter
    protected CLBufferField<Float> editBallsField;
    @Getter
    protected final CLConstantField<Integer> ballsAmountField;
    @Getter
    protected final CLQueue queue;
    @Getter
    protected final CLHandler handler;
    @Getter
    protected final CLFile file;
    @Getter
    protected final CLBufferField<Float> debugField;

    @Getter
    @Setter
    protected EquationSolvingFunction function;

    @Getter
    protected final CLBufferField<Float> gameInformationField;
    @Getter
    protected float[] currentGameInformation;
    @Getter
    protected BoardPosition initialPosition;
    @Getter
    protected BoardConfiguration configuration;

    public BoardStructure(CLHandler handler, float height, float width, int ballsAmount, float alpha, long ballFieldSize, long gameFieldSize) {
        this.configuration = new BoardConfiguration((int) width, (int) height, ballsAmount, alpha, handler);
        this.alphaField = new CLConstantField<>(handler, Float.class, alpha);
        this.heightField = new CLConstantField<>(handler, Float.class, height);
        this.widthField = new CLConstantField<>(handler, Float.class, width);
        this.ballsAmountField = new CLConstantField<>(handler, Integer.class, ballsAmount);
        this.ballBufferSizeField = new CLConstantField<>(handler, Integer.class, BALL_BUFFER_SIZE);
        this.handler = handler;
        this.queue = handler.createQueue();
        this.file = handler.getBoardFile();
        this.ballsField = new CLBufferField<>(handler, CLMem.Usage.InputOutput, Float.class, ballFieldSize);
        this.editBallsField = new CLBufferField<>(handler, CLMem.Usage.InputOutput, Float.class, ballFieldSize);
        this.debugField = new CLBufferField<>(handler, CLMem.Usage.InputOutput, Float.class, 20);
        this.gameInformationField = new CLBufferField<>(handler, CLMem.Usage.InputOutput, Float.class, gameFieldSize);
        this.currentGameInformation = new float[(int) gameFieldSize];
        this.timeStepField = new CLConstantField<>(this.handler, Float.class, TIME_STEP);
    }

    protected abstract CLFunction createFunction();

    protected void updateGameInformation() {
        Pointer<Float> pointer = this.gameInformationField.getArgument().read(queue);
        this.currentGameInformation = pointer.getFloats();
    }

    protected void invertEdit() {
        CLBufferField<Float> temp = this.ballsField;
        this.ballsField = this.editBallsField;
        this.editBallsField = temp;
    }

    protected abstract void initialise_(BoardPosition position);

    public void initialise(BoardPosition position) {
        this.initialise_(position);
        this.initialPosition = position;
        this.function = this.createFunction();
    }

    public float getWidth() {
        return this.widthField.getArgument();
    }

    public float getHeight() {
        return this.heightField.getArgument();
    }

    public int getBallsAmount() {
        return this.ballsAmountField.getArgument();
    }

}
