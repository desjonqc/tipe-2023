package com.cegesoft.data.exception;

/**
 * Exception lancée lorsqu'une erreur de métadonnées de fichier survient.
 */
public class WrongFileMetadataException extends Exception {

    public WrongFileMetadataException(String message) {
        super(message);
    }

    public WrongFileMetadataException(String message, Throwable t) {
        super(message, t);
    }

}
