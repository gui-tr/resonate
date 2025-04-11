package com.resonate.exception;

public class ResonateException extends RuntimeException {
    public ResonateException(String message) {
        super(message);
    }
    public ResonateException(String message, Throwable cause) {
        super(message, cause);
    }
}
