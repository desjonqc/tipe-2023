package com.cegesoft.data;

import com.cegesoft.data.exception.WrongFileMetadataException;
import com.cegesoft.data.metadata.FileMetadata;
import com.cegesoft.game.SimulationInformation;

public interface ByteStorable {

    byte[] toBytes();

    void fromBytes(byte[] bytes);

    int size();

    FileMetadata getMetadata();

    void setMetadata(FileMetadata meta) throws WrongFileMetadataException;

    static ByteStorable empty(SimulationInformation simulationInformation) {
        throw new IllegalStateException("empty function not implemented");
    }
}
