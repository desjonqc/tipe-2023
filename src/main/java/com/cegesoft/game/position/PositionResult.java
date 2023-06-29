package com.cegesoft.game.position;

import com.cegesoft.data.ByteStorable;
import com.cegesoft.game.SimulationInformation;
import com.cegesoft.util.ByteArrayConverter;
import lombok.Getter;

import java.nio.ByteBuffer;

public class PositionResult implements ByteStorable {

    private final SimulationInformation simulationInformation;
    @Getter
    private int angle;
    @Getter
    private int norm;
    @Getter
    private short result;

    public PositionResult(SimulationInformation simulationInformation, int angle, int norm, short result) {
        this.simulationInformation = simulationInformation;
        this.angle = angle;
        this.norm = norm;
        this.result = result;
        if (this.size() > 4) {
            throw new IllegalArgumentException("Result size is too big!");
        }
    }

    public PositionResult(SimulationInformation simulationInformation) {
        this.simulationInformation = simulationInformation;
    }


    @Override
    public byte[] toBytes() {
        byte[] bytes = new byte[this.size()];
        int total = 0;
        int angleStorageSize = this.simulationInformation.getAngleStorageSize();
        int normStorageSize = this.simulationInformation.getNormStorageSize();
        int scoreStorageSize = this.simulationInformation.getScoreStorageSize();
        int offset = 0;
        total |= ByteArrayConverter.maskGenerator(angleStorageSize, offset) & this.angle;
        offset += angleStorageSize;
        total |= ByteArrayConverter.maskGenerator(normStorageSize, offset) & (this.norm << offset);
        offset += normStorageSize;
        total |= ByteArrayConverter.maskGenerator(scoreStorageSize, offset) & (this.simulationInformation.formatScore(this.result) << (offset));
        System.arraycopy(ByteArrayConverter.intsToBytes(new int[] {total}), 4 - bytes.length, bytes, 0, bytes.length);
        return bytes;
    }

    @Override
    public void fromBytes(byte[] bytes) {
        byte[] workingBytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            if (3 - i < bytes.length)
                workingBytes[i] = bytes[i - (4 - bytes.length)];
            else
                workingBytes[i] = 0;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(workingBytes);
        int total = byteBuffer.getInt();
        int angleStorageSize = this.simulationInformation.getAngleStorageSize();
        int normStorageSize = this.simulationInformation.getNormStorageSize();
        int scoreStorageSize = this.simulationInformation.getScoreStorageSize();
        int offset = 0;
        this.angle = ByteArrayConverter.maskGenerator(angleStorageSize, offset) & total;
        offset += angleStorageSize;
        this.norm = (ByteArrayConverter.maskGenerator(normStorageSize, offset) & total) >> offset;
        offset += normStorageSize;
        this.result = (short) ((ByteArrayConverter.maskGenerator(scoreStorageSize, offset) & total) >> (offset));
        this.result = this.simulationInformation.unformatScore(this.result);
    }

    @Override
    public int size() {
        return this.simulationInformation.getUnitSize();
    }

    public static PositionResult empty(SimulationInformation simulationInformation) {
        return new PositionResult(simulationInformation);
    }
}
