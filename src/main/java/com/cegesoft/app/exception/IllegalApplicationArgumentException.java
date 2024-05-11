package com.cegesoft.app.exception;

/**
 * Appelée lorsque les arguments sont invalides.
 */
public class IllegalApplicationArgumentException extends IllegalArgumentException {

    public IllegalApplicationArgumentException(String message, Exception e) {
        super(message, e);
    }

}
