package com.cegesoft.statistic;

import com.cegesoft.util.NDArrayUtil;
import com.cegesoft.util.exception.IndiceDimensionException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe permettant de réaliser des études statistiques
 */
public class Statistic {

    private final KeyHandler keyHandler;
    private final Value[] values;

    /**
     * Crée une nouvelle étude statistique
     * @param shape Forme de la clé (nombre d'indices et dimensions)
     */
    public Statistic(int[] shape) {
        this.keyHandler = new KeyHandler(shape.length, shape);
        this.values = new Value[keyHandler.getTotalSize()];
    }

    /**
     * Récupère ou crée (si elle n'existe pas) une valeur statistique à partir d'un indice
     * @param index Indice de la clé
     * @return La valeur statistique
     */
    public Value getOrCreateValue(int index) {
        if (values[index] == null) {
            values[index] = new Value();
        }
        return values[index];
    }

    /**
     * Récupère ou crée (si elle n'existe pas) une valeur statistique à partir d'indices définissant une clé
     * @param indices Indices sous forme d'un n-uplet permettant de retrouver l'indice de la clé
     * @return La valeur statistique
     * @throws IndiceDimensionException Si les indices ne correspondent pas à la 'shape' définie dans keyHandler.
     */
    public Value getOrCreateValue(int[] indices) throws IndiceDimensionException {
        return this.getOrCreateValue(keyHandler.getIndex(indices));
    }

    /**
     * Sauvegarde les statistiques dans un format pouvant être importé dans numpy
     * @param normalize Normaliser les valeurs
     * @param coefficients Coefficients de partitionnement
     * @return Le contenu du fichier
     */
    public String saveToNumpy(boolean normalize, int[] coefficients) {
        StringBuilder builder = new StringBuilder();
        builder.append("# NUMPY SAVE\n");
        builder.append("# SHAPE: \n");
        for (int i = 0; i < keyHandler.getShape().length; i++) {
            builder.append(keyHandler.getShape()[i]);
            if (i < keyHandler.getShape().length - 1) {
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

    /**
     * Sauvegarde les statistiques dans un fichier texte
     * @param simulationId Identifiant de la simulation
     * @param name Nom du fichier
     * @param normalize Normaliser les valeurs
     * @param coefficients Coefficients de partitionnement
     * @throws IOException Si le fichier ne peut être créé
     */
    public void saveFileToNumpy(int simulationId, String name, boolean normalize, int[] coefficients) throws IOException {
        File file = new File("python/datastored/simulation-" + simulationId + "/" + name + ".statistic");
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

    /**
     * Permet de transformer une clé formée par n indices en un seul indice.
     */
    @AllArgsConstructor
    public static class KeyHandler {
        @Getter
        private final int dimension;
        @Getter
        private final int[] shape;

        public int getIndex(int[] indices) throws IndiceDimensionException {
            if (indices.length != dimension) {
                throw new IndiceDimensionException("Indices length must be equal to dimension!");
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

    /**
     * Représente une valeur statistique sur un ensemble d'échantillon
     */
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
