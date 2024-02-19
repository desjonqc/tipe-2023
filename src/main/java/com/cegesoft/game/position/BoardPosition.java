package com.cegesoft.game.position;

import com.cegesoft.Main;
import com.cegesoft.app.property.Property;
import com.cegesoft.data.ByteStorable;
import com.cegesoft.data.metadata.DefaultFileMetadata;
import com.cegesoft.data.metadata.FileMetadata;
import com.cegesoft.game.Board;
import com.cegesoft.game.BoardConfiguration;
import com.cegesoft.game.BoardStructure;
import com.cegesoft.game.exception.BoardParsingException;
import com.cegesoft.opencl.CLBufferField;
import com.cegesoft.opencl.CLHandler;
import com.cegesoft.util.ByteArrayConverter;
import com.cegesoft.util.NDArrayUtil;
import com.cegesoft.util.exception.IndiceDimensionException;
import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;
import lombok.Getter;
import org.bridj.Pointer;

import java.util.Arrays;

public class BoardPosition implements IPositionContainer {

    @Getter
    private final float[] position;
    private FileMetadata metadata;

    public BoardPosition(float[] position, NDArrayUtil.ParametrizedIndex index) throws BoardParsingException {
        this.position = new float[2 * Main.getIntProperty(Property.BALL_AMOUNT)];
        this.metadata = new DefaultFileMetadata((short) this.size());
        try {
            for (int i = 0; i < Main.getIntProperty(Property.BALL_AMOUNT); i++) {
                this.position[2 * i] = position[index.getIndex(0, i)];
                this.position[2 * i + 1] = position[index.getIndex(1, i)];
            }
        } catch (IndiceDimensionException e) {
            throw new BoardParsingException("Can't parse position array :", e);
        }
    }

    public BoardPosition(CLBuffer<Float> ballsBuffer, NDArrayUtil.ParametrizedIndex index, CLQueue queue) throws BoardParsingException {
        this(ballsBuffer.read(queue).getFloats(), index);
    }

    public BoardPosition(CLBufferField<Float> ballsField, NDArrayUtil.ParametrizedIndex index, CLQueue queue) throws BoardParsingException {
        this(ballsField.getArgument(), index, queue);
    }

    public BoardPosition(Board board) throws BoardParsingException {
        this(board.getBallsField(), (i, j) -> i + BoardStructure.BALL_BUFFER_SIZE * j, board.getQueue());
    }

    public BoardPosition() {
        this.position = new float[2 * Main.getIntProperty(Property.BALL_AMOUNT)];
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

    @Override
    public FileMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(FileMetadata meta) {
        this.metadata = meta;
    }

    public CLBufferField<Float> toBufferField(CLHandler handler, CLQueue queue, BoardConfiguration configuration) {
        CLBufferField<Float> floats = new CLBufferField<>(handler, CLMem.Usage.InputOutput, Float.class, (long) BoardStructure.BALL_BUFFER_SIZE * (this.position.length / 2));
        Pointer<Float> pointer = floats.getArgument().read(queue);
        for (int i = 0; i < (this.position.length / 2); i++) {
            pointer.set((long) i * BoardStructure.BALL_BUFFER_SIZE, this.position[2 * i]);
            pointer.set((long) i * BoardStructure.BALL_BUFFER_SIZE + 1, this.position[2 * i + 1]);
            if (this.position[2 * i + 1] < -configuration.getHeight() / 2.0) {
                pointer.set((long) i * BoardStructure.BALL_BUFFER_SIZE + 4, -1.0f);
            }
        }
        floats.getArgument().write(queue, pointer, false).waitFor();
        return floats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardPosition that = (BoardPosition) o;
        return Arrays.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(position);
    }

    @Override
    public BoardPosition getBoardPosition() {
        return this;
    }

    public BoardPosition setBallCoordinates(int ballId, float x, float y) {
        this.position[2 * ballId] = x;
        this.position[2 * ballId + 1] = y;
        return this;
    }
}
