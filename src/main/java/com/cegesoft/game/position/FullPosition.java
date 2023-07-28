package com.cegesoft.game.position;

import com.cegesoft.data.exception.WrongFileMetadataException;
import com.cegesoft.data.metadata.FileMetadata;
import com.cegesoft.data.metadata.SimulationFileMetadata;
import com.cegesoft.log.Logger;
import lombok.Getter;

public class FullPosition implements IPositionContainer {

    @Getter
    private BoardPosition boardPosition;
    @Getter
    private PositionResult[] results;
    private SimulationFileMetadata metadata;

    public FullPosition(BoardPosition position, PositionResult[] results, SimulationFileMetadata metadata) {
        this.boardPosition = position;
        this.results = results;
        this.metadata = metadata;
    }

    private FullPosition() {
    }

    @Override
    public byte[] toBytes() {
        byte[] bytes = new byte[this.boardPosition.size() + this.results.length * this.results[0].size()];
        System.arraycopy(this.boardPosition.toBytes(), 0, bytes, 0, this.boardPosition.size());
        for (int i = 0; i < this.results.length; i++) {
            int size = this.results[i].size();
            System.arraycopy(this.results[i].toBytes(), 0, bytes, this.boardPosition.size() + i * size, size);
        }
        return bytes;
    }

    @Override
    public void fromBytes(byte[] bytes) {
        this.boardPosition = new BoardPosition();
        this.boardPosition.setMetadata(this.metadata);
        byte[] positionBytes = new byte[this.boardPosition.size()];
        System.arraycopy(bytes, 0, positionBytes, 0, this.boardPosition.size());
        this.boardPosition.fromBytes(positionBytes);
        int unitSize = this.metadata.getInformation().getUnitSize();
        this.results = new PositionResult[(bytes.length - this.boardPosition.size()) / unitSize];
        byte[] resultBytes = new byte[bytes.length - this.boardPosition.size()];
        System.arraycopy(bytes, this.boardPosition.size(), resultBytes, 0, bytes.length - this.boardPosition.size());
        for (int i = 0; i < this.results.length; i++) {
            try {
                byte[] unitData = new byte[unitSize];
                System.arraycopy(resultBytes, i * unitSize, unitData, 0, unitSize);
                this.results[i] = new PositionResult();
                this.results[i].setMetadata(this.metadata);
                this.results[i].fromBytes(unitData);
            } catch (WrongFileMetadataException e) {
                Logger.error("Wrong metadata for FullPosition", e);
            }
        }
    }

    @Override
    public int size() {
        return this.boardPosition.size() + this.results.length * this.metadata.getInformation().getUnitSize();
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
