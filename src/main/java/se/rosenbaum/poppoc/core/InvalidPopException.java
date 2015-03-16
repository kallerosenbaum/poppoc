package se.rosenbaum.poppoc.core;

public class InvalidPopException extends Exception {
    public InvalidPopException(String message) {
        super(message);
    }

    public InvalidPopException(String message, Throwable cause) {
        super(message, cause);
    }
}
