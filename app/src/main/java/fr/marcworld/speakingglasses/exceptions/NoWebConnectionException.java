package fr.marcworld.speakingglasses.exceptions;

import java.io.IOException;

/**
 * Exception thrown when there is no connection to the web.
 *
 * @author Marc Plouhinec
 */
public class NoWebConnectionException extends IOException {

    public NoWebConnectionException() {
    }

    public NoWebConnectionException(String detailMessage) {
        super(detailMessage);
    }

    public NoWebConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoWebConnectionException(Throwable cause) {
        super(cause);
    }
}
