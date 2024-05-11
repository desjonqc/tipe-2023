package com.cegesoft.game.exception;

/**
 * Appelée lorsqu'il y a une erreur de format des positions de billard.
 */
public class BoardParsingException extends Exception {

    public BoardParsingException(String message) {
        super(message);
    }

    public BoardParsingException(String message, Throwable t) {
        super(message, t);
    }

}
