package br.com.gazintech.orderapp.exception_handler;

import br.com.gazintech.orderapp.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.List;

/**
 * Implementation of ExceptionHandlerList that provides a list of exception handlers.
 * It includes predefined exception handler items for various exceptions and their corresponding HTTP statuses.
 */
@Slf4j
@Component("exceptionHandler")
public class ExceptionHandlerListImpl implements ExceptionHandlerList {

    /**
     * Returns a list of exception handler items.
     * Each item contains details about the exception, including error code, exception class, message, HTTP status,
     * description, and whether to show the original message.
     *
     * @return List of ExceptionHandlerItem
     */
    @Override
    public List<ExceptionHandlerItem> getExceptionHandler() {
        return List.of(
                new ExceptionHandlerItem("E001", IdempotencyKeyNotFoundException.class, "Idempotency key not found", HttpStatus.BAD_REQUEST, "Tenant not found", false),
                new ExceptionHandlerItem("E002", InsufficientBalanceException.class, "Insufficient Balance", HttpStatus.BAD_REQUEST, "Insufficient balance for the operation", false),
                new ExceptionHandlerItem("E003", InvalidOrderStatusException.class, "Invalid Order status", HttpStatus.BAD_REQUEST, "Invalid order status provided", true),
                new ExceptionHandlerItem("E004", ReflectionException.class, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while processing your request", false),
                new ExceptionHandlerItem("E005", HttpRequestMethodNotSupportedException.class, "HTTP Method not supported", HttpStatus.METHOD_NOT_ALLOWED, "The requested HTTP method is not supported for this endpoint.", true),
                new ExceptionHandlerItem("E006", MissingRequestHeaderException.class, "Missing Request Header", HttpStatus.BAD_REQUEST, "Required request header is missing or invalid.", true),
                new ExceptionHandlerItem("E007", PartnerNotFoundException.class, "Partner not found", HttpStatus.NOT_FOUND, "Partner not found", false),
                new ExceptionHandlerItem("E008", OrderNotFoundException.class, "Order not found", HttpStatus.NOT_FOUND, "Order not found", false),
                new ExceptionHandlerItem("E009", InvalidDataAccessApiUsageException.class, "Invalid body request", HttpStatus.BAD_REQUEST, "Invalid Body request", true),
                new ExceptionHandlerItem("E010", MissingServletRequestParameterException.class, "Missing Request Parameter", HttpStatus.BAD_REQUEST, "Missing Request Parameter", true)
        );
    }

    /**
     * Finds an exception handler item by the given exception.
     * If no specific handler is found for the exception, a default handler is returned.
     *
     * @param exception The exception to find the handler for.
     * @return ExceptionHandlerItem corresponding to the exception.
     */
    @Override
    public ExceptionHandlerItem findByException(Throwable exception) {
        log.debug("Exception to be handled: %s".formatted(exception.getClass().getName()));
        return getExceptionHandler().stream()
                .filter(item -> item.exception().isInstance(exception))
                .findFirst()
                .orElse(new ExceptionHandlerItem(
                        "E000",
                        exception.getClass(),
                        "An unexpected error occurred",
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred while processing your request.",
                        true)
                );
    }
}
