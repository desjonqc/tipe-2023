package com.cegesoft.util;

import java.util.ArrayList;
import java.util.List;

public class NDArrayUtil {

    public static int getIndex(int[] shape, int... indices) {
        int index = 0;
        if (indices.length != shape.length)
            throw new IllegalArgumentException("Array has wrong shape : " + shape.length + " != " + indices.length);
        int offset = 1;
        for (int i = 0; i < indices.length; i++) {
            index += indices[i] * offset;
            offset *= shape[i];
        }
        return index;
    }

    public static int[] getIndices(int[] shape, int index) {
        int[] indices = new int[shape.length];
        int offset = 1;
        for (int i = 0; i < indices.length; i++) {
            indices[i] = index / offset % shape[i];
            offset *= shape[i];
        }
        return indices;
    }

    public interface ParametrizedIndex {
        int getIndex(int localIndex, int ballIndex);
    }

    public static class SimulationParametrizedIndex implements ParametrizedIndex {

        private final int angle, norm;
        private final int[] shape;
        public SimulationParametrizedIndex(int angle, int norm, int[] shape) {
            this.angle = angle;
            this.norm = norm;
            this.shape = shape;
        }
        @Override
        public int getIndex(int localIndex, int ballIndex) {
            return NDArrayUtil.getIndex(shape, localIndex, ballIndex, angle, norm);
        }
    }

}
