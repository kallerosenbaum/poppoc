package se.rosenbaum.poppoc.core;

/**
 * Exception to signal that an error that is not a system error has occurred. This can typically happen
 * when a pop is not valid or when the incoming request is missing a parameter. I.e. something the client can
 * do something about.
 */
public class ClientException extends RuntimeException {
    public ClientException(String message) {
        super(message);
    }

    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
