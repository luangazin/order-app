package br.com.gazintech.orderapp.exception;

/**
 * Exception thrown when there is insufficient balance for an operation.
 */
public class InsufficientBalanceException extends OrderAppException {

    public InsufficientBalanceException() {
        super("Insufficient balance for the operation");
    }

    public InsufficientBalanceException(String message) {
        super(message);
    }

    public InsufficientBalanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientBalanceException(Throwable cause) {
        super(cause);
    }
}
