package com.cegesoft.util;

import com.cegesoft.util.exception.IndiceDimensionException;

import java.util.ArrayList;
import java.util.List;

public class NDArrayUtil {

    /**
     * Retourne l'indice du tableau à partir des indices locaux et de la forme du tableau
     * @param shape la forme du tableau
     * @param indices les indices du tableau
     * @return l'indice du tableau
     * @throws IndiceDimensionException si les indices ne correspondent pas à la forme du tableau
     */
    public static int getIndex(int[] shape, int... indices) throws IndiceDimensionException {
        int index = 0;
        if (indices.length != shape.length)
            throw new IndiceDimensionException("Array has wrong shape : " + shape.length + " != " + indices.length);
        int offset = 1;
        for (int i = 0; i < indices.length; i++) {
            index += indices[i] * offset;
            offset *= shape[i];
        }
        return index;
    }

    /**
     * Retourne les indices locaux du tableau à partir de l'indices global et de la forme du tableau
     * @param shape la forme du tableau
     * @param index l'indice global du tableau
     * @return les indices locaux du tableau
     */
    public static int[] getIndices(int[] shape, int index) {
        int[] indices = new int[shape.length];
        int offset = 1;
        for (int i = 0; i < indices.length; i++) {
            indices[i] = index / offset % shape[i];
            offset *= shape[i];
        }
        return indices;
    }

    /**
     * Simplifie l'utilisation des indices pour le billard
     */
    public interface ParametrizedIndex {
        int getIndex(int localIndex, int ballIndex) throws IndiceDimensionException;
    }

    /**
     * Cas appliqué de ParametrizedIndex à la simulation de positions
     */
    public static class SimulationParametrizedIndex implements ParametrizedIndex {

        private final int angle, norm;
        private final int[] shape;
        public SimulationParametrizedIndex(int angle, int norm, int[] shape) {
            this.angle = angle;
            this.norm = norm;
            this.shape = shape;
        }
        @Override
        public int getIndex(int localIndex, int ballIndex) throws IndiceDimensionException {
            return NDArrayUtil.getIndex(shape, localIndex, ballIndex, angle, norm);
        }
    }

}
