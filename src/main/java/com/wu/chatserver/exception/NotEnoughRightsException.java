package com.wu.chatserver.exception;

public class NotEnoughRightsException extends RuntimeException{
    public NotEnoughRightsException(String message) {
        super(message);
    }

    public NotEnoughRightsException(String message, Throwable cause) {
        super(message, cause);
    }
}
