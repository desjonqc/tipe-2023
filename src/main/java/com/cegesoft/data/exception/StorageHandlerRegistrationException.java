package com.cegesoft.data.exception;

/**
 * Exception lancée lorsqu'une erreur d'enregistrement de gestionnaire de stockage survient.
 *
 * @see com.cegesoft.data.handlers.StorageHandler
 */
public class StorageHandlerRegistrationException extends Exception {

    public StorageHandlerRegistrationException(String message) {
        super(message);
    }

    public StorageHandlerRegistrationException(String message, Throwable e) {
        super(message, e);
    }

}
