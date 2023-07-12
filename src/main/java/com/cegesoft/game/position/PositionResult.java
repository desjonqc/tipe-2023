package com.cegesoft.game.position;

import com.cegesoft.data.ByteStorable;
import com.cegesoft.data.exception.WrongFileMetadataException;
import com.cegesoft.data.metadata.FileMetadata;
import com.cegesoft.data.metadata.SimulationFileMetadata;
import com.cegesoft.game.SimulationInformation;
import com.cegesoft.log.Logger;
import com.cegesoft.util.ByteArrayConverter;
import lombok.Getter;

import java.nio.ByteBuffer;

public class PositionResult implements ByteStorable {

    @Getter
    private int angle;
    @Getter
    private int norm;
    @Getter
    private short result;
    private SimulationFileMetadata metadata;

    public PositionResult(int angle, int norm, short result) {
        this.angle = angle;
        this.norm = norm;
        this.result = result;
        this.metadata = new SimulationFileMetadata(new SimulationInformation(angle, norm, result));
        if (this.size() > 4) {
            Logger.error("Position Result size is too big !");
            System.exit(-1);
        }
    }

    public PositionResult() {
    }


    @Override
    public byte[] toBytes() {
        byte[] bytes = new byte[this.size()];
        int total = 0;
        int angleStorageSize = this.metadata.getInformation().getAngleStorageSize();
        int normStorageSize = this.metadata.getInformation().getNormStorageSize();
        int scoreStorageSize = this.metadata.getInformation().getScoreStorageSize();
        int offset = 0;
        total |= ByteArrayConverter.maskGenerator(angleStorageSize, offset) & this.angle;
        offset += angleStorageSize;
        total |= ByteArrayConverter.maskGenerator(normStorageSize, offset) & (this.norm << offset);
        offset += normStorageSize;
        total |= ByteArrayConverter.maskGenerator(scoreStorageSize, offset) & (this.metadata.getInformation().formatScore(this.result) << (offset));
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
        int angleStorageSize = this.metadata.getInformation().getAngleStorageSize();
        int normStorageSize = this.metadata.getInformation().getNormStorageSize();
        int scoreStorageSize = this.metadata.getInformation().getScoreStorageSize();
        int offset = 0;
        this.angle = ByteArrayConverter.maskGenerator(angleStorageSize, offset) & total;
        offset += angleStorageSize;
        this.norm = (ByteArrayConverter.maskGenerator(normStorageSize, offset) & total) >> offset;
        offset += normStorageSize;
        this.result = (short) ((ByteArrayConverter.maskGenerator(scoreStorageSize, offset) & total) >> (offset));
        this.result = this.metadata.getInformation().unformatScore(this.result);
    }

    @Override
    public int size() {
        return this.metadata.getInformation().getUnitSize();
    }

    @Override
    public FileMetadata getMetadata() {
        return this.metadata;
    }

    @Override
    public void setMetadata(FileMetadata meta) throws WrongFileMetadataException {
        if (!(meta instanceof SimulationFileMetadata))
            throw new WrongFileMetadataException("PositionResult's metadata must be a SimulationFileMetadata");
        this.metadata = (SimulationFileMetadata) meta;
    }

    public static PositionResult empty() {
        return new PositionResult();
    }
}
