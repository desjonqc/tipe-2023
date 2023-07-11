package com.cegesoft.game.position;

import com.cegesoft.data.ByteStorable;
import com.cegesoft.data.FileMetadata;
import com.cegesoft.game.SimulationInformation;
import com.cegesoft.util.ByteArrayConverter;
import lombok.Getter;

public class FullPosition implements ByteStorable {

    @Getter
    private final SimulationInformation simulationInformation;
    @Getter
    private BoardPosition position;
    @Getter
    private PositionResult[] results;

    public FullPosition(SimulationInformation information, BoardPosition position, PositionResult[] results) {
        this.position = position;
        this.results = results;
        this.simulationInformation = information;
    }

    private FullPosition(SimulationInformation simulationInformation) {
        this.simulationInformation = simulationInformation;
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
        this.position = BoardPosition.empty(this.simulationInformation);
        byte[] positionBytes = new byte[this.position.size()];
        System.arraycopy(bytes, 0, positionBytes, 0, this.position.size());
        this.position.fromBytes(positionBytes);
        int unitSize = this.simulationInformation.getUnitSize();
        this.results = new PositionResult[(bytes.length - this.position.size()) / unitSize];
        byte[] resultBytes = new byte[bytes.length - this.position.size()];
        System.arraycopy(bytes, this.position.size(), resultBytes, 0, bytes.length - this.position.size());
        for (int i = 0; i < this.results.length; i++) {
            byte[] unitData = new byte[unitSize];
            System.arraycopy(resultBytes, i * unitSize, unitData, 0, unitSize);
            this.results[i] = PositionResult.empty(this.simulationInformation);
            this.results[i].fromBytes(unitData);
        }
    }

    @Override
    public int size() {
        return this.position.size() + this.results.length * this.simulationInformation.getUnitSize();
    }

    @Override
    public FileMetadata getMetadata() {
        return null;
    }

    public static FullPosition empty(SimulationInformation information) {
        return new FullPosition(information);
    }
}
