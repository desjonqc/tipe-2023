package com.cegesoft.game;

import com.cegesoft.Main;
import com.cegesoft.app.property.Property;
import com.cegesoft.game.position.BoardPosition;
import com.cegesoft.util.ByteArrayConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class SimulationInformation {

    @Getter
    private final short maxScore = 100;
    @Getter
    private final short scoreOffset = 46;
    @Getter
    private final int anglePartition;
    @Getter
    private final int normPartition;
    @Getter
    private final int resultsLimit;

    public int getAngleStorageSize() {
        return ByteArrayConverter.ceilLog2(this.anglePartition);
    }
    public int getNormStorageSize() {
        return ByteArrayConverter.ceilLog2(this.normPartition);
    }

    public int getScoreStorageSize() {
        return ByteArrayConverter.ceilLog2(this.maxScore);
    }

    public int getUnitSize() {
        return (int) Math.ceil((this.getAngleStorageSize() + this.getNormStorageSize() + this.getScoreStorageSize()) / 8.0);
    }

    public short formatScore(short score) {
        return (short) (score + this.scoreOffset);
    }

    public short unformatScore(short score) {
        return (short) (score - this.scoreOffset);
    }

    public int getDataGroupSize() {
        return 8 * Main.getIntProperty(Property.BALL_AMOUNT) + this.getUnitSize() * this.getResultsLimit();
    }
}
