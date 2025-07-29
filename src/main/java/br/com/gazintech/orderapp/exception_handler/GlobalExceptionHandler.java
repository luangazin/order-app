package br.com.gazintech.orderapp.exception_handler;

import br.com.gazintech.orderapp.api.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * This class intercepts exceptions thrown by controllers and returns a standardized API response.
 * It uses an ExceptionHandlerList to find the appropriate response for each exception type.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ExceptionHandlerList exceptionHandlerList;


    public GlobalExceptionHandler(ExceptionHandlerList exceptionHandlerList) {
        this.exceptionHandlerList = exceptionHandlerList;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        log.error("Validation error: {}", exception.getMessage(), exception);
        String errorMessage = extractValidationErrors(exception);

        log.trace("Validation error details: {}", errorMessage);
        var item = exceptionHandlerList.findByClass(exception.getClass());
        log.trace("Exception handler item found for validation: {}", item);
        return ApiResponse.<Void>builder().error(item.errorCode(), item.isShowOriginalMessage() ? errorMessage : item.message(), item.status()).build();
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Throwable exception) {

        log.error("An error occurred: {}", exception.getMessage(), exception);
        var item = exceptionHandlerList.findByClass(exception.getClass());
        log.trace("Exception handler item found: {}", item);
        return ApiResponse.<Void>builder().error(item.errorCode(), item.isShowOriginalMessage() ? exception.getMessage() : item.message(), item.status()).build();
    }

    private static String extractValidationErrors(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        exception.getBindingResult().getGlobalErrors().forEach(error ->
                errors.put(error.getObjectName(), error.getDefaultMessage())
        );

        return errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .reduce((first, second) -> first + ", " + second)
                .orElse("Validation error occurred");
    }
}
