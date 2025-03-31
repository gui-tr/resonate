package com.resonate.exception;

public class ResonateException extends RuntimeException {
    private final int errorCode;

    public ResonateException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ResonateException(String message, Throwable cause, int errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
