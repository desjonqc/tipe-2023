package com.cegesoft.app.exception;

import com.cegesoft.app.argument.ApplicationArgument;

public class IllegalApplicationArgumentException extends IllegalArgumentException {

    public IllegalApplicationArgumentException(String message, Exception e) {
        super(message, e);
    }

}
