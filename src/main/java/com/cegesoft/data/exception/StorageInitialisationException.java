package com.cegesoft.data.exception;

/**
 * Exception lancée lorsqu'une erreur d'initialisation du stockage survient.
 */
public class StorageInitialisationException extends Exception {

    public StorageInitialisationException(String message) {
        super(message);
    }

    public StorageInitialisationException(String message, Exception e) {
        super(message, e);
    }

}
