package br.com.gazintech.orderapp.exception_handler;

import br.com.gazintech.orderapp.exception.IdempotencyKeyNotFoundException;
import br.com.gazintech.orderapp.exception.InsufficientBalanceException;
import br.com.gazintech.orderapp.exception.InvalidOrderStatusException;
import br.com.gazintech.orderapp.exception.ReflectionException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("exceptionHandler")
public class ExceptionHandlerListImpl implements ExceptionHandlerList {

    @Override
    public List<ExceptionHandlerItem> getExceptionHandler() {
        return List.of(
                new ExceptionHandlerItem("E001", IdempotencyKeyNotFoundException.class, "Idempotency key not found", HttpStatus.BAD_REQUEST, "Tenant not found"),
                new ExceptionHandlerItem("E002", InsufficientBalanceException.class, "Insufficient Balance", HttpStatus.BAD_REQUEST, "Insufficient balance for the operation"),
                new ExceptionHandlerItem("E003", InvalidOrderStatusException.class, "Invalid Order status", HttpStatus.INTERNAL_SERVER_ERROR, "Invalid order status provided"),
                new ExceptionHandlerItem("E004", ReflectionException.class, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while processing your request")
        );
    }

    @Override
    public ExceptionHandlerItem findByException(Throwable exception) {
        return getExceptionHandler().stream()
                .filter(item -> item.getException().isInstance(exception))
                .findFirst()
                .orElse(new ExceptionHandlerItem(
                        "E000",
                        exception.getClass(),
                        "An unexpected error occurred",
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred while processing your request.")
                );
    }
}
