package com.cegesoft.app.argument;

import lombok.Getter;

/**
 * Argument d'application.
 * @param <T> le type de l'argument.
 */
public class ApplicationArgument<T> {

    @Getter
    private final boolean required;
    @Getter
    private final String prefix;
    @Getter
    private final T defaultValue;
    @Getter
    private final String description;

    /**
     * @param required false si l'argument est optionnel
     * @param prefix le nom de l'argument
     * @param defaultValue la valeur par défaut (optionnel)
     * @param description la description à afficher dans l'aide.
     */
    public ApplicationArgument(boolean required, String prefix, T defaultValue, String description) {
        this.required = required;
        this.prefix = prefix;
        this.defaultValue = defaultValue;
        this.description = description;
    }

    @Override
    public String toString() {
        return this.prefix;
    }
}
