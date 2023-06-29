package com.cegesoft.game.position;

import com.cegesoft.Main;
import com.cegesoft.data.ByteStorable;
import com.cegesoft.game.Board;
import com.cegesoft.game.BoardStructure;
import com.cegesoft.game.SimulationInformation;
import com.cegesoft.opencl.CLBufferField;
import com.cegesoft.opencl.CLHandler;
import com.cegesoft.util.ByteArrayConverter;
import com.cegesoft.util.NDArrayUtil;
import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;
import lombok.Getter;
import org.bridj.Pointer;

public class BoardPosition implements ByteStorable {

    @Getter
    private final float[] position;

    public BoardPosition(float[] position, NDArrayUtil.ParametrizedIndex index) {
        this.position = new float[2 * Main.BALL_AMOUNT];
        for (int i = 0; i < Main.BALL_AMOUNT; i++) {
            this.position[2 * i] = position[index.getIndex(0, i)];
            this.position[2 * i + 1] = position[index.getIndex(1, i)];
        }
    }

    public BoardPosition(CLBuffer<Float> ballsBuffer, NDArrayUtil.ParametrizedIndex index, CLQueue queue) {
        this(ballsBuffer.read(queue).getFloats(), index);
    }

    public BoardPosition(CLBufferField<Float> ballsField, NDArrayUtil.ParametrizedIndex index, CLQueue queue) {
        this(ballsField.getArgument(), index, queue);
    }

    public BoardPosition(Board board) {
        this(board.getBallsField(), (i, j) -> i + BoardStructure.BALL_BUFFER_SIZE * j, board.getQueue());
    }

    private BoardPosition() {
        this.position = new float[2 * Main.BALL_AMOUNT];
    }

    public float[] getBallPosition(int ball) {
        return new float[]{position[2 * ball], position[2 * ball + 1]};
    }

    @Override
    public byte[] toBytes() {
        return ByteArrayConverter.floatsToBytes(position);
    }

    @Override
    public void fromBytes(byte[] bytes) {
        ByteArrayConverter.bytesToFloats(bytes, position);
    }

    @Override
    public int size() {
        return 4 * position.length;
    }

    public static BoardPosition empty(SimulationInformation information) {
        return new BoardPosition();
    }

    public CLBufferField<Float> toBufferField(CLHandler handler, CLQueue queue) {
        CLBufferField<Float> floats = new CLBufferField<>(handler, CLMem.Usage.InputOutput, Float.class, (long) BoardStructure.BALL_BUFFER_SIZE * (this.position.length / 2));
        Pointer<Float> pointer = floats.getArgument().read(queue);
        for (int i = 0; i < (this.position.length / 2); i++) {
            pointer.set((long) i * BoardStructure.BALL_BUFFER_SIZE, this.position[2 * i]);
            pointer.set((long) i * BoardStructure.BALL_BUFFER_SIZE + 1, this.position[2 * i + 1]);
        }
        floats.getArgument().write(queue, pointer, false).waitFor();
        return floats;
    }
}