package com.cegesoft.data.exception;

public class ParseFromFileException extends Exception {

    public ParseFromFileException(String message) {
        super(message);
    }

    public ParseFromFileException(String message, Exception e) {
        super(message, e);
    }

}
