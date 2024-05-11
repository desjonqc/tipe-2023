package com.cegesoft.statistic;

import com.cegesoft.Main;
import com.cegesoft.app.SimulationApplication;
import com.cegesoft.app.argument.ApplicationArgument;
import com.cegesoft.app.property.Property;
import com.cegesoft.data.FileStorage;
import com.cegesoft.data.Storage;
import com.cegesoft.data.StorageManager;
import com.cegesoft.data.handlers.StorageHandler;
import com.cegesoft.data.metadata.DefaultFileMetadata;
import com.cegesoft.game.SimulationInformation;
import com.cegesoft.game.position.BoardPosition;
import com.cegesoft.log.Logger;
import com.cegesoft.simulation.implementation.MeasurementJobHandler;

/**
 * Application de simulation de statistiques
 */
public class StatisticApplication extends SimulationApplication {
    private int[] coefficients;
    private int simulationId;
    private int positionAmount;
    private String fileName = "statistic-2.pos";

    /**
     * Initialise l'application et définit les arguments
     */
    public StatisticApplication() {
        super();
        this.registerArgument(new ApplicationArgument<>(true, "id", 0, "Simulation ID"));
        this.registerArgument(new ApplicationArgument<>(false, "coef", "180,205,20,20", "Variation of partitions (I: Initial, S: Slope, Format : 'I[angle],I[norm],S[angle],S[norm]"));
        this.registerArgument(new ApplicationArgument<>(false, "file", fileName, "File containing positions to test"));
        this.registerArgument(new ApplicationArgument<>(false, "position-amount", 10, "Amount of positions in the file 'file'"));
    }

    @Override
    public void start() throws Exception {
        super.start();

        // Initialisation du stockage des positions
        StorageManager.register(StorageManager.StorageTag.STATISTIC_POSITION, new StorageHandler("python/datastored/" + StorageManager.StorageTag.STATISTIC_POSITION.getName() + "/", "pos",
                10, DefaultFileMetadata.class));

        // Lecture des positions
        FileStorage<DefaultFileMetadata> fileStorage = new FileStorage<>(fileName, DefaultFileMetadata.class);
        Storage storage = fileStorage.read();
        BoardPosition[] positions = new BoardPosition[positionAmount];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = storage.getDataGroup(BoardPosition.class, i);
        }

        // Démarrage des simulations pour chaque coefficient
        for (int i = 0; i < StatisticManager.NORM_ANGLE_SHAPE[0]; i++) {
            for (int j = 0; j < StatisticManager.NORM_ANGLE_SHAPE[1]; j++) {
                SimulationInformation information = new SimulationInformation(coefficients[0] + j * coefficients[1], coefficients[2] + i * coefficients[3], 15);
                MeasurementJobHandler handler = new MeasurementJobHandler(positions, i, j, information);
                handler.start();
                handler.join();
                Logger.info("Finished " + i + " " + j);
            }
        }

        // Sauvegarde les résultats
        Logger.info("Finished");
        StatisticManager.getOrCreateStatistic(StatisticManager.StatisticTag.NICE_PLAY_LOSS).saveFileToNumpy(simulationId, "nice_play_loss", true, coefficients);
        StatisticManager.getOrCreateStatistic(StatisticManager.StatisticTag.BAD_PLAY_LOSS).saveFileToNumpy(simulationId, "bad_play_loss", true, coefficients);
        StatisticManager.getOrCreateStatistic(StatisticManager.StatisticTag.EQUAL_PLAY_LOSS).saveFileToNumpy(simulationId, "equal_play_loss", true, coefficients);
        StatisticManager.getOrCreateStatistic(StatisticManager.StatisticTag.COMPUTATION_TIME).saveFileToNumpy(simulationId, "computation_time", true, coefficients);

        this.stop();
        Main.listenCommand();
    }

    @Override
    public void stop() {
        super.stop();
        StorageManager.unregister(StorageManager.StorageTag.STATISTIC_POSITION);
    }

    /**
     * Lit les arguments.
     *
     * L'argument 'coef' correspond aux paramètres de partitionnement.
     *
     * @param argument l'argument à lire
     * @param value    la valeur de l'argument
     * @return
     */
    @Override
    protected boolean readArgument(ApplicationArgument<?> argument, Object value) {
        switch (argument.toString()) {
            case "id":
                this.simulationId = (int) value;
                break;
            case "coef":
                String valueStr = (String) value;
                String[] coeff_str = valueStr.split(",");
                if (coeff_str.length != 4) {
                    Logger.info("There must be 4 coefficients.");
                    return true;
                }
                this.coefficients = new int[]{Integer.parseInt(coeff_str[0]), Integer.parseInt(coeff_str[1]), Integer.parseInt(coeff_str[2]), Integer.parseInt(coeff_str[3])};
                break;
            case "file":
                this.fileName = (String) value;
                break;
            case "position-amount":
                this.positionAmount = (int) value;
                break;
        }
        return false;
    }
}
