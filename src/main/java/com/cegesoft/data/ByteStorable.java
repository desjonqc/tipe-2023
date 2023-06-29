package com.cegesoft.data;

import com.cegesoft.game.SimulationInformation;

public interface ByteStorable {

    byte[] toBytes();

    void fromBytes(byte[] bytes);

    int size();

    static ByteStorable empty(SimulationInformation simulationInformation) {
        throw new IllegalStateException("empty function not implemented");
    }
}
