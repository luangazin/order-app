package br.com.gazintech.orderapp.exception;

public class InvalidOrderStatusException extends OrderAppException {

    public InvalidOrderStatusException() {
        super("Invalid order status");
    }

    public InvalidOrderStatusException(String message) {
        super(message);
    }

    public InvalidOrderStatusException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidOrderStatusException(Throwable cause) {
        super(cause);
    }
}
