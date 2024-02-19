package com.cegesoft.app.argument;

import com.cegesoft.app.exception.IllegalApplicationArgumentException;
import com.cegesoft.app.property.PropertyArgument;
import com.cegesoft.app.property.PropertyHandler;
import com.cegesoft.log.Logger;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public abstract class ArgumentsReader {

    @Getter
    protected final List<ApplicationArgument<?>> arguments = new ArrayList<>();

    protected void registerArgument(ApplicationArgument<?> argument) {
        this.arguments.add(argument);
    }

    public boolean readArguments(String[] args) throws IllegalArgumentException {
        for (ApplicationArgument<?> argument : arguments) {
            boolean found = false;
            for (String arg : args) {
                if (arg.startsWith("--" + argument.getPrefix() + "=")) {
                    found = true;
                    String argumentValue = arg.split("=")[1];
                    if (this.readArgumentOrProperty(argument, this.convertArgumentType(argument, argumentValue)))
                        return true;
                    break;
                }
            }
            if (!found && argument.isRequired()) {
                Logger.info("Required application argument missing : " + argument.getPrefix());
                return true;
            }
            if (!found && this.readArgumentOrProperty(argument, argument.getDefaultValue()))
                return true;
        }
        return false;
    }

    private boolean readArgumentOrProperty(ApplicationArgument<?> argument, Object value) {
        if (this instanceof PropertyHandler && argument instanceof PropertyArgument<?>) {
            ((PropertyHandler) this).setProperty(((PropertyArgument<?>) argument).getProperty(), value);
            return false;
        }
        return this.readArgument(argument, value);
    }

    /**
     * Lit un argument de l'application
     *
     * @param argument l'argument à lire
     * @param value    la valeur de l'argument
     * @return true s'il y a une erreur
     */
    protected abstract boolean readArgument(ApplicationArgument<?> argument, Object value);

    private Object convertArgumentType(ApplicationArgument<?> argument, String strValue) throws IllegalArgumentException {
        Class<?> tClass = argument.getDefaultValue().getClass();
        Object value = null;
        try {
            if (tClass.isAssignableFrom(String.class)) {
                value = strValue;
            } else if (tClass.isAssignableFrom(Integer.class)) {
                value = Integer.parseInt(strValue);
            } else if (tClass.isAssignableFrom(Float.class)) {
                value = Float.parseFloat(strValue);
            } else if (tClass.isAssignableFrom(Boolean.class)) {
                value = Boolean.parseBoolean(strValue);
            } else if (tClass.isAssignableFrom(Enum.class)) {
                value = tClass.getDeclaredMethod("valueOf", String.class).invoke(null, strValue.toUpperCase());
            }
        } catch (Exception e) {
            throw new IllegalApplicationArgumentException("Wrong argument value for " + argument.getPrefix() + ". '" + strValue + "' should be " + tClass.getName(), e);
        }

        if (value == null) {
            throw new IllegalArgumentException("Generic class is not supported : " + tClass.getName());
        }
        return value;
    }
}
