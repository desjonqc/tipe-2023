package com.cegesoft.game;

import com.cegesoft.game.exception.BoardParsingException;
import com.cegesoft.game.position.BoardPosition;
import com.cegesoft.game.position.FullPosition;
import com.cegesoft.game.position.PositionResult;
import com.cegesoft.opencl.CLConstantField;
import com.cegesoft.opencl.CLField;
import com.cegesoft.opencl.CLFunction;
import com.cegesoft.opencl.CLHandler;
import com.cegesoft.simulation.IJobExecutable;
import com.cegesoft.util.NDArrayUtil;
import com.cegesoft.util.exception.IndiceDimensionException;
import com.cegesoft.util.weighting.ScoreWeighting;
import org.bridj.Pointer;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BoardSimulation extends BoardStructure implements IJobExecutable {
    private final int[] BALL_DATA_SHAPE;
    private final int[] GAME_DATA_SHAPE;
    private final CLConstantField<Integer> anglesField;
    private final CLConstantField<Integer> normField;
    private final SimulationInformation information;

    public BoardSimulation(CLHandler handler, float height, float width, int ballsAmount, float alpha, SimulationInformation information) {
        super(handler, height, width, ballsAmount, alpha, ((long) ballsAmount * BoardStructure.BALL_BUFFER_SIZE) * information.getAnglePartition() * information.getNormPartition(), (long) BoardStructure.GAME_DATA_SIZE * information.getAnglePartition() * information.getNormPartition());
        this.information = information;
        this.anglesField = new CLConstantField<>(this.getHandler(), Integer.class, this.information.getAnglePartition());
        this.normField = new CLConstantField<>(this.getHandler(), Integer.class, this.information.getNormPartition());
        this.BALL_DATA_SHAPE = new int[] {BoardStructure.BALL_BUFFER_SIZE, ballsAmount, this.information.getAnglePartition(), this.information.getNormPartition()};
        this.GAME_DATA_SHAPE = new int[] {BoardStructure.GAME_DATA_SIZE, this.information.getAnglePartition(), this.information.getNormPartition()};
    }

    public BoardSimulation(Board board, SimulationInformation information) {
        this(board.getHandler(), board.getHeightField().getArgument(), board.getWidthField().getArgument(), board.getBallsAmountField().getArgument(), board.getAlphaField().getArgument(), information);
    }

    public BoardSimulation(BoardConfiguration configuration, BoardPosition initialPosition, SimulationInformation information) {
        this(configuration.getHandler(), configuration.getHeight(), configuration.getWidth(), configuration.getBallsAmount(), configuration.getAlpha(), information);
        this.initialise(initialPosition);
    }

    @Override
    protected CLFunction createFunction() {
        return new CLFunction(this.file, "move_2", this.ballsField, this.editBallsField, this.ballBufferSizeField, this.ballsAmountField, this.alphaField, this.heightField, this.widthField, this.timeStepField, this.gameInformationField, this.debugField, anglesField, normField, new CLField<>(this.handler, Short.class, (short) 1));
    }

    @Override
    protected void initialise_(BoardPosition boardPosition) {
        CLFunction function1 = new CLFunction(this.file, "copy_buffer", boardPosition.toBufferField(this.handler, this.queue), this.ballsField, this.ballBufferSizeField, this.ballsAmountField, this.anglesField, this.debugField);
        function1.call(this.queue, new int[] {this.ballsAmountField.getArgument(), this.information.getAnglePartition(), this.information.getNormPartition()}).waitFor();
    }
    public float[] getBallInformation(int angle, int norm, int i) throws IndiceDimensionException {
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
        return this.getIndices(bestShot)[2] * 300.0f / this.information.getNormPartition();
    }

    public float getAngle(int bestShot) {
        return this.getIndices(bestShot)[1] * 360.f / this.information.getAnglePartition();
    }

    @Override
    public void run(int index) {
        if (index == 1) {
            this.function.setArgument(12, new CLField<>(this.handler, Short.class, (short) 0));
        }
        this.function.call(this.queue, new int[] {this.ballsAmountField.getArgument(), this.information.getAnglePartition(), this.information.getNormPartition()}).waitFor();
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
        return IntStream.range(0, this.information.getAnglePartition() * this.information.getNormPartition()).boxed()
                .filter(o -> {
                    float current = this.currentGameInformation[o * BoardStructure.GAME_DATA_SIZE + 1];
                    return score == 0 ? current == 0 : (score == -1 ? current < 0 : current > 0);
                })
                .sorted((o1, o2) -> (score == -1 ? 1 : -1) * Float.compare(this.currentGameInformation[o1 * BoardStructure.GAME_DATA_SIZE + 1], this.currentGameInformation[o2 * BoardStructure.GAME_DATA_SIZE + 1]))
                .collect(Collectors.toList());
    }

    @Override
    public BoardPosition getBoardPosition(int resultIndex) throws BoardParsingException {
        int[] indices = this.getIndices(resultIndex);
        return new BoardPosition(this.ballsField, new NDArrayUtil.SimulationParametrizedIndex(indices[1], indices[2], BALL_DATA_SHAPE), this.queue);
    }

    @Override
    public FullPosition getCurrentEvaluation(ScoreWeighting weighting) throws IllegalArgumentException, IllegalStateException {
        if (this.initialPosition == null)
            throw new IllegalStateException("Initial position has never been set");
        if (weighting.getWeighting(-1)[0] + weighting.getWeighting(0)[0] + weighting.getWeighting(1)[0] != this.information.getResultsLimit())
            throw new IllegalArgumentException("Bad ScoreWeighting");
        Predicate<Float>[] filterPredicates = new Predicate[] {
                f -> (float)f > 0,
                f -> (float)f == 0,
                f -> (float)f < 0
        };
        int[] sortCoefficients = new int[] { -1, 1, 1};
        PositionResult[] results = new PositionResult[this.information.getResultsLimit()];
        int currentIndex = 0;
        for (int i = -1; i < 2; i++) {
            final int i_ = i;
            Stream<Integer> result = IntStream.range(0, this.information.getAnglePartition() * this.information.getNormPartition()).boxed()
                    .filter(o -> filterPredicates[i_ + 1].test(this.currentGameInformation[o * BoardStructure.GAME_DATA_SIZE + 1]));
            if (!weighting.isRandom()) {
                result = result.sorted((o1, o2) -> sortCoefficients[i_ + 1] * Float.compare(this.currentGameInformation[o1 * BoardStructure.GAME_DATA_SIZE + 1], this.currentGameInformation[o2 * BoardStructure.GAME_DATA_SIZE + 1]));
            }
            int limit = weighting.getWeighting(i)[0];
            PositionResult[] resultArray = result.map(j -> {
                int[] indices = this.getIndices(j);
                return new PositionResult(this.information, indices[1], indices[2], (short)this.getScore(j));
            }).limit(limit).toArray(PositionResult[]::new);
            if (resultArray.length == 0)
                continue;
            System.arraycopy(resultArray, 0, results, currentIndex, resultArray.length);
            currentIndex += limit;
        }

        return new FullPosition(this.information, this.initialPosition, results);
    }
}
