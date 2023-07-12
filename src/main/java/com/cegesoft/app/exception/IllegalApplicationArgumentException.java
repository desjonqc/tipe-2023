package com.cegesoft.app.exception;

public class IllegalApplicationArgumentException extends IllegalArgumentException {

    public IllegalApplicationArgumentException(String message, Exception e) {
        super(message, e);
    }

}
