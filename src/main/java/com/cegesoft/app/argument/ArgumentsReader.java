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

    public boolean readArguments(String[] args) {
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
                Logger.getLogger().println("Required application argument missing : " + argument.getPrefix());
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
     * Read an argument
     * @param argument the argument to read
     * @param value the value typed into the console
     * @return true if an error occurred
     */
    protected abstract boolean readArgument(ApplicationArgument<?> argument, Object value);

    private Object convertArgumentType(ApplicationArgument<?> argument, String strValue) {
        Class<?> tClass = argument.getDefaultValue().getClass();
        Object value = null;
        try {
            if (tClass.isAssignableFrom(String.class)) {
                value = strValue;
            } else if (tClass.isAssignableFrom(Integer.class)) {
                value = Integer.parseInt(strValue);
            } else if (tClass.isAssignableFrom(Float.class)) {
                value = Float.parseFloat(strValue);
            } else if (tClass.isAssignableFrom(Enum.class)) {
                value = tClass.getDeclaredMethod("valueOf", String.class).invoke(null, strValue.toUpperCase());
            }
        }catch (Exception e) {
            throw new IllegalApplicationArgumentException(argument, e);
        }

        if (value == null) {
            throw new IllegalArgumentException("Generic class is not supported : " + tClass.getName());
        }
        return value;
    }
}
