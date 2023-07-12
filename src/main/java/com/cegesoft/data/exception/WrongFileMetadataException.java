package com.cegesoft.data.exception;

public class WrongFileMetadataException extends Exception {

    public WrongFileMetadataException(String message) {
        super(message);
    }

    public WrongFileMetadataException(String message, Throwable t) {
        super(message, t);
    }

}
