package com.cegesoft.app;

import com.cegesoft.ai.AITrainingApplication;
import com.cegesoft.ai.DataGenerationApplication;
import com.cegesoft.game.GameApplication;
import com.cegesoft.help.HelpApplication;
import com.cegesoft.representation.RepresentationApplication;
import com.cegesoft.statistic.StatisticApplication;
import lombok.Getter;

public enum ApplicationsImpl {

    HELP("help", new HelpApplication(), "Help interface"),
    GAME("game", new GameApplication(), "Classic Pool board with possibility of simulations"),
    STATISTIC("statistic", new StatisticApplication(), "Calculation of performance statistics by varying the angle and norm partitions"),
    DATA_GEN("data-gen", new DataGenerationApplication(), "Data generation for AI training"),
    REPRESENTATION("representation", new RepresentationApplication(), "Representation of data stored in files"),
    AI("ai", new AITrainingApplication(), "Artificial intelligence training");

    @Getter
    private final String tag;
    @Getter
    private final Application application;
    @Getter
    private final String description;

    ApplicationsImpl(String tag, Application application, String description) {
        this.tag = tag;
        this.application = application;
        this.description = description;
    }

    public static ApplicationsImpl getByTag(String tag) {
        for (ApplicationsImpl impl : values())
            if (impl.tag.equals(tag)) return impl;
        return null;
    }
}
