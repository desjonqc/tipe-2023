package com.cegesoft.app.argument;

import lombok.Getter;

public class ApplicationArgument<T> {

    @Getter
    private final boolean required;
    @Getter
    private final String prefix;
    @Getter
    private final T defaultValue;
    @Getter
    private final String description;

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
