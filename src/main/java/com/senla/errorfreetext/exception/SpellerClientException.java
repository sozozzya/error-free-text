package com.senla.errorfreetext.exception;

public class SpellerClientException extends RuntimeException {

    public SpellerClientException(String message) {
        super(message);
    }

    public SpellerClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
