package com.cegesoft.game.exception;

public class BoardParsingException extends Exception {

    public BoardParsingException(String message) {
        super(message);
    }

    public BoardParsingException(String message, Throwable t) {
        super(message, t);
    }

}
