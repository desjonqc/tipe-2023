package com.cegesoft.util.exception;

public class IndiceDimensionException extends Exception {

    public IndiceDimensionException(String message) {
        super(message);
    }

    public IndiceDimensionException(String message, Exception e) {
        super(message, e);
    }

}
