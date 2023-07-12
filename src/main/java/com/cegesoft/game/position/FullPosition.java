package com.cegesoft.game.position;

import com.cegesoft.data.ByteStorable;
import com.cegesoft.data.exception.WrongFileMetadataException;
import com.cegesoft.data.metadata.FileMetadata;
import com.cegesoft.data.metadata.SimulationFileMetadata;
import lombok.Getter;

public class FullPosition implements ByteStorable {

    @Getter
    private BoardPosition position;
    @Getter
    private PositionResult[] results;
    private SimulationFileMetadata metadata;

    public FullPosition(BoardPosition position, PositionResult[] results, SimulationFileMetadata metadata) {
        this.position = position;
        this.results = results;
        this.metadata = metadata;
    }

    private FullPosition() {
    }

    public static FullPosition empty() {
        return new FullPosition();
    }

    @Override
    public byte[] toBytes() {
        byte[] bytes = new byte[this.position.size() + this.results.length * this.results[0].size()];
        System.arraycopy(this.position.toBytes(), 0, bytes, 0, this.position.size());
        for (int i = 0; i < this.results.length; i++) {
            int size = this.results[i].size();
            System.arraycopy(this.results[i].toBytes(), 0, bytes, this.position.size() + i * size, size);
        }
        return bytes;
    }

    @Override
    public void fromBytes(byte[] bytes) {
        this.position = BoardPosition.empty();
        byte[] positionBytes = new byte[this.position.size()];
        System.arraycopy(bytes, 0, positionBytes, 0, this.position.size());
        this.position.fromBytes(positionBytes);
        int unitSize = this.metadata.getInformation().getUnitSize();
        this.results = new PositionResult[(bytes.length - this.position.size()) / unitSize];
        byte[] resultBytes = new byte[bytes.length - this.position.size()];
        System.arraycopy(bytes, this.position.size(), resultBytes, 0, bytes.length - this.position.size());
        for (int i = 0; i < this.results.length; i++) {
            byte[] unitData = new byte[unitSize];
            System.arraycopy(resultBytes, i * unitSize, unitData, 0, unitSize);
            this.results[i] = PositionResult.empty();
            this.results[i].fromBytes(unitData);
        }
    }

    @Override
    public int size() {
        return this.position.size() + this.results.length * this.metadata.getInformation().getUnitSize();
    }

    @Override
    public FileMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(FileMetadata meta) throws WrongFileMetadataException {
        if (!(meta instanceof SimulationFileMetadata))
            throw new WrongFileMetadataException("FullPosition's metadata must be a SimulationFileMetadata");
        this.metadata = (SimulationFileMetadata) meta;
    }
}
