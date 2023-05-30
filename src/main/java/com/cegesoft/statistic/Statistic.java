package com.cegesoft.statistic;

import com.cegesoft.util.NDArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Statistic {

    private final Key key;
    private final Value[] values;

    public Statistic(int[] shape) {
        this.key = new Key(shape.length, shape);
        this.values = new Value[key.getTotalSize()];
    }

    public Value getOrCreateValue(int index) {
        if (values[index] == null) {
            values[index] = new Value();
        }
        return values[index];
    }

    public Value getOrCreateValue(int[] indices) {
        return this.getOrCreateValue(key.getIndex(indices));
    }

    public String saveToNumpy() {
        StringBuilder builder = new StringBuilder();
        builder.append("# NUMPY SAVE\n");
        builder.append("# SHAPE: \n");
        for (int i = 0; i < key.getShape().length; i++) {
            builder.append(key.getShape()[i]);
            if (i < key.getShape().length - 1) {
                builder.append(",");
            }
        }
        builder.append("\n");
        builder.append("# VALUES: \n");
        for (int i = 0; i < values.length; i++) {
            builder.append(values[i].mean());
            if (i < values.length - 1) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    @AllArgsConstructor
    public static class Key {
        @Getter
        private final int dimension;
        @Getter
        private final int[] shape;

        public int getIndex(int[] indices) {
            if (indices.length != dimension) {
                throw new IllegalArgumentException("Indices length must be equal to dimension!");
            }
            return NDArrayUtil.getIndex(shape, indices);
        }

        public int getTotalSize() {
            int total = 1;
            for (int j : shape) {
                total *= j;
            }
            return total;
        }

    }

    public static class Value {
        @Getter
        private final List<Float> rawValues;

        public Value() {
            this.rawValues = new ArrayList<>();
        }

        public void addValue(float value) {
            rawValues.add(value);
        }

        public float mean() {
            float sum = 0;
            for (Float rawValue : rawValues) {
                sum += rawValue;
            }
            return sum / rawValues.size();
        }

        public float sum() {
            float sum = 0;
            for (Float rawValue : rawValues) {
                sum += rawValue;
            }
            return sum;
        }
    }
}
