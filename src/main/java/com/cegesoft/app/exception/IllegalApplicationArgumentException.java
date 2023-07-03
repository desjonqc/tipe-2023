package com.cegesoft.app.exception;

import com.cegesoft.app.argument.ApplicationArgument;

public class IllegalApplicationArgumentException extends RuntimeException{

    public IllegalApplicationArgumentException(ApplicationArgument<?> argument, Exception e) {
        super("Wrong argument value for " + argument.getPrefix(), e);
    }

}
