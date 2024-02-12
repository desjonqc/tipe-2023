package com.cegesoft.util.exception;

/**
 * Erreur appelée lorsqu'un indice est de dimension incorrect.
 */
public class IndiceDimensionException extends Exception {

    public IndiceDimensionException(String message) {
        super(message);
    }

    public IndiceDimensionException(String message, Exception e) {
        super(message, e);
    }

}
