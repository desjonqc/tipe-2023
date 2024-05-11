package com.cegesoft.data.exception;

/**
 * Exception lancée lorsqu'une erreur de conversion d'un fichier vers un ByteStorable survient.
 *
 * @see com.cegesoft.data.ByteStorable
 */
public class ParseFromFileException extends Exception {

    public ParseFromFileException(String message) {
        super(message);
    }

    public ParseFromFileException(String message, Exception e) {
        super(message, e);
    }

}
