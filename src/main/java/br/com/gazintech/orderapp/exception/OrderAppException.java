package br.com.gazintech.orderapp.exception;

/**
 * Custom exception class for the Order Application.
 * This class extends RuntimeException and provides several constructors
 * to create exceptions with different messages and causes.
 */
public class OrderAppException extends RuntimeException {
    public OrderAppException() {
        super("Generic Auction Exception");
    }

    public OrderAppException(String message) {
        super(message);
    }

    public OrderAppException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderAppException(Throwable cause) {
        super(cause);
    }
}
