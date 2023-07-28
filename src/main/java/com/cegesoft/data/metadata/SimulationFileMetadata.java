package com.cegesoft.data.metadata;

import com.cegesoft.game.SimulationInformation;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.util.Objects;

public class SimulationFileMetadata extends DefaultFileMetadata {

    @Getter
    private SimulationInformation information;
    public SimulationFileMetadata(SimulationInformation information) {
        super(information.getDataGroupSize());
        this.information = information;
    }

    private SimulationFileMetadata() {}


    @Override
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(this.size());
        buffer.putShort(this.dataGroupSize);
        buffer.putInt(2, this.information.getAnglePartition());
        buffer.putInt(6, this.information.getNormPartition());
        buffer.putInt(10, this.information.getResultsLimit());
        return buffer.array();
    }

    @Override
    public void fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        this.dataGroupSize = buffer.getShort(0);
        this.information = new SimulationInformation(buffer.getInt(2), buffer.getInt(6), buffer.getInt(10));
        super.fromBytes(bytes);
    }

    @Override
    public int size() {
        return 14;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SimulationFileMetadata that = (SimulationFileMetadata) o;
        return Objects.equals(information, that.information);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), information);
    }
}
