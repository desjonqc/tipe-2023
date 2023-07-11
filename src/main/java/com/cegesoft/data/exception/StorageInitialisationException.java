package com.cegesoft.data.exception;

public class StorageInitialisationException extends Exception {

    public StorageInitialisationException(String message) {
        super(message);
    }

    public StorageInitialisationException(String message, Exception e) {
        super(message, e);
    }

}
