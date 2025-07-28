package br.com.gazintech.orderapp.exception_handler;

import org.springframework.http.HttpStatus;

/**
 * Represents an item in the exception handler with details about the error.
 * This record encapsulates the error code, exception class, message, HTTP status,
 * description, and if shows the original message.
 */
public record ExceptionHandlerItem(String errorCode,
                                   Class<? extends Throwable> exception,
                                   String message,
                                   HttpStatus status,
                                   String description,
                                   boolean isShowOriginalMessage) {
}