package com.cegesoft.util.exception;

/**
 * Erreur appelée lorsqu'un indice est de dimension incorrecte.
 */
public class IndiceDimensionException extends Exception {

    public IndiceDimensionException(String message) {
        super(message);
    }

    public IndiceDimensionException(String message, Exception e) {
        super(message, e);
    }

}
