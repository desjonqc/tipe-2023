package com.cegesoft.data.handlers;

import com.cegesoft.game.SimulationInformation;
import com.cegesoft.util.weighting.ScoreWeighting;
import lombok.Getter;

public class FullStorageHandler extends StorageHandler {
    @Getter
    private final ScoreWeighting weighting;
    public FullStorageHandler(String baseDirectoryPath, String extension, int maxStorableInAFile, int dataGroupSize, SimulationInformation information, ScoreWeighting weighting) {
        super(baseDirectoryPath, extension, maxStorableInAFile, dataGroupSize);
        this.information = information;
        this.weighting = weighting;
    }
}
