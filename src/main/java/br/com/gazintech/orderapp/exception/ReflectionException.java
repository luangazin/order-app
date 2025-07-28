package br.com.gazintech.orderapp.exception;

public class ReflectionException extends OrderAppException {

    public ReflectionException() {
        super("Insufficient balance for the operation");
    }

    public ReflectionException(String message) {
        super(message);
    }

    public ReflectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReflectionException(Throwable cause) {
        super(cause);
    }
}
