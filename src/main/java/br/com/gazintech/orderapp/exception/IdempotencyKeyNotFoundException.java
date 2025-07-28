package br.com.gazintech.orderapp.exception;

/**
 *
 */
public class IdempotencyKeyNotFoundException extends OrderAppException {
    public IdempotencyKeyNotFoundException() {
        super("Idempotency key not found");
    }

    public IdempotencyKeyNotFoundException(String message) {
        super(message);
    }

    public IdempotencyKeyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdempotencyKeyNotFoundException(Throwable cause) {
        super(cause);
    }
}