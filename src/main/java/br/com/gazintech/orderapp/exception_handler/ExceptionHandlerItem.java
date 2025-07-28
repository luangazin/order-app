package br.com.gazintech.orderapp.exception_handler;

import org.springframework.http.HttpStatus;

public record ExceptionHandlerItem(String errorCode,
                                   Class<? extends Throwable> exception,
                                   String message,
                                   HttpStatus status,
                                   String description,
                                   boolean isShowOriginalMessage) {
}