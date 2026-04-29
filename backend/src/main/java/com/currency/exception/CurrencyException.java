package com.currency.exception;

public class CurrencyException extends RuntimeException {
    private final int statusCode;

    public CurrencyException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public CurrencyException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
