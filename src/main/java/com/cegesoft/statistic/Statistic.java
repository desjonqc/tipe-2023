package com.cegesoft.statistic;

import com.cegesoft.util.NDArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    public String saveToNumpy(boolean normalize, int[] coefficients) {
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
        builder.append("# COEFFICIENTS: \n");
        for (int i = 0; i < coefficients.length; i++) {
            builder.append(coefficients[i]);
            if (i < coefficients.length - 1) {
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
        builder.append("\n");
        builder.append("# NORMALIZE: \n");
        builder.append(normalize);
        return builder.toString();
    }

    public void saveFileToNumpy(int simulationId, String name, boolean normalize, int[] coefficients) throws IOException {
        File file = new File("python/data/simulation-" + simulationId + "/" + name + ".statistic");
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs())
                throw new IOException("Could not create parent directories!");
        }
        if (!file.exists()) {
            if (!file.createNewFile())
                throw new IOException("Could not create file!");
        }
        FileWriter writer = new FileWriter(file);
        writer.write(saveToNumpy(normalize, coefficients));
        writer.close();
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
