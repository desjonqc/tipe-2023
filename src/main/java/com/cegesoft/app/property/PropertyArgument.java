package com.cegesoft.app.property;

import com.cegesoft.app.argument.ApplicationArgument;
import lombok.Getter;

/**
 * Argument d'application définissant directement la valeur de la propriété associée.
 * @param <T> Type de la valeur de l'argument.
 */
public class PropertyArgument<T> extends ApplicationArgument<T> {
    @Getter
    private final Property property;

    public PropertyArgument(boolean required, String prefix, T defaultValue, String description, Property property) {
        super(required, prefix, defaultValue, description);
        this.property = property;
    }
}
