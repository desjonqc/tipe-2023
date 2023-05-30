package com.cegesoft.game;

import com.cegesoft.opencl.CLConstantField;
import com.cegesoft.opencl.CLField;
import com.cegesoft.opencl.CLFunction;
import com.cegesoft.opencl.CLHandler;
import com.cegesoft.simulation.IJobExecutable;
import com.cegesoft.util.NDArrayUtil;
import com.cegesoft.util.ProgressBar;
import org.bridj.Pointer;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BoardSimulation extends BoardStructure implements IJobExecutable {
    public final static int SIMULATION_TIME = 6600;
    public final static int SEARCH_DEPTH = 1;
    public static int ANGLE_PARTITION = 900 * SEARCH_DEPTH;
    public static int NORM_PARTITION = 100 * SEARCH_DEPTH;

    private final int[] BALL_DATA_SHAPE;
    private final int[] GAME_DATA_SHAPE;
    private final CLConstantField<Integer> anglesField;
    private final CLConstantField<Integer> normField;

    public BoardSimulation(CLHandler handler, float height, float width, int ballsAmount, float alpha) {
        super(handler, height, width, ballsAmount, alpha, ((long) ballsAmount * BoardStructure.BALL_BUFFER_SIZE) * ANGLE_PARTITION * NORM_PARTITION, (long) BoardStructure.GAME_DATA_SIZE * ANGLE_PARTITION * NORM_PARTITION);
        this.anglesField = new CLConstantField<>(this.getHandler(), Integer.class, ANGLE_PARTITION);
        this.normField = new CLConstantField<>(this.getHandler(), Integer.class, NORM_PARTITION);
        this.BALL_DATA_SHAPE = new int[] {BoardStructure.BALL_BUFFER_SIZE, ballsAmount, ANGLE_PARTITION, NORM_PARTITION};
        this.GAME_DATA_SHAPE = new int[] {BoardStructure.GAME_DATA_SIZE, ANGLE_PARTITION, NORM_PARTITION};
    }

    public BoardSimulation(Board board) {
        this(board.getHandler(), board.getHeightField().getArgument(), board.getWidthField().getArgument(), board.getBallsAmountField().getArgument(), board.getAlphaField().getArgument());
    }

    public BoardSimulation(BoardConfiguration configuration, BoardPosition initialPosition) {
        this(configuration.getHandler(), configuration.getHeight(), configuration.getWidth(), configuration.getBallsAmount(), configuration.getAlpha());
        this.initialise(initialPosition);
    }

    @Override
    protected CLFunction createFunction() {
        return new CLFunction(this.file, "move_2", this.ballsField, this.editBallsField, this.ballBufferSizeField, this.ballsAmountField, this.alphaField, this.heightField, this.widthField, this.timeStepField, this.gameInformationField, this.debugField, anglesField, normField, new CLField<>(this.handler, Short.class, (short) 1));
    }

    @Override
    protected void initialise_(BoardPosition boardPosition) {
        CLFunction function1 = new CLFunction(this.file, "copy_buffer", boardPosition.toBufferField(this.handler, this.queue), this.ballsField, this.ballBufferSizeField, this.ballsAmountField, this.anglesField, this.debugField);
        function1.call(this.queue, new int[] {this.ballsAmountField.getArgument(), ANGLE_PARTITION, NORM_PARTITION}).waitFor();
    }
    public float[] getBallInformation(int angle, int norm, int i) {
        Pointer<Float> pointer = this.ballsField.getArgument().read(this.queue);
        float[] info = new float[BoardStructure.BALL_BUFFER_SIZE];
        for (int j = 0; j < BoardStructure.BALL_BUFFER_SIZE; j++) {
            info[j] = pointer.get(NDArrayUtil.getIndex(this.BALL_DATA_SHAPE, j, i, angle, norm));
        }
        return info;
    }


    public float getScore(int bestShot) {
        return this.currentGameInformation[bestShot * BoardStructure.GAME_DATA_SIZE + 1];
    }

    private int[] getIndices(int bestShot) {
        return NDArrayUtil.getIndices(this.GAME_DATA_SHAPE, bestShot * BoardStructure.GAME_DATA_SIZE);
    }

    public float getNorm(int bestShot) {
        return this.getIndices(bestShot)[2] * 300.0f / NORM_PARTITION;
    }

    public float getAngle(int bestShot) {
        return this.getIndices(bestShot)[1] * 360.f / ANGLE_PARTITION;
    }

    @Override
    public void run(int index) {
        if (index == 1) {
            this.function.setArgument(12, new CLField<>(this.handler, Short.class, (short) 0));
        }
        this.function.call(this.queue, new int[] {this.ballsAmountField.getArgument(), ANGLE_PARTITION, NORM_PARTITION}).waitFor();
        this.invertEdit();
        this.function.setArgument(0, this.ballsField);
        this.function.setArgument(1, this.editBallsField);
    }

    @Override
    public void reset() {
        this.function.setArgument(12, new CLField<>(this.handler, Short.class, (short) 1));
        this.updateGameInformation();
    }

    @Override
    public List<Integer> getResults(int score) {
        return IntStream.range(0, ANGLE_PARTITION * NORM_PARTITION).boxed()
                .filter(o -> {
                    float current = this.currentGameInformation[o * BoardStructure.GAME_DATA_SIZE + 1];
                    return score == 0 ? current == 0 : (score == -1 ? current < 0 : current > 0);
                })
                .sorted((o1, o2) -> (score == -1 ? 1 : -1) * Float.compare(this.currentGameInformation[o1 * BoardStructure.GAME_DATA_SIZE + 1], this.currentGameInformation[o2 * BoardStructure.GAME_DATA_SIZE + 1]))
                .collect(Collectors.toList());
    }

    @Override
    public BoardPosition getBoardPosition(int resultIndex) {
        int[] indices = this.getIndices(resultIndex);
        return new BoardPosition(this.ballsField, new NDArrayUtil.SimulationParametrizedIndex(indices[1], indices[2], BALL_DATA_SHAPE), this.queue);
    }
}
