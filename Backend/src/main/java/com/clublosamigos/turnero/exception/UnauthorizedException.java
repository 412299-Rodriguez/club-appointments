package com.clublosamigos.turnero.exception;

/**
 * Exception thrown when authentication fails or user is not authorized
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
