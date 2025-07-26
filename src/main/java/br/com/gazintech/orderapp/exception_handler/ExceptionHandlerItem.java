package br.com.gazintech.orderapp.exception_handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ExceptionHandlerItem {
    private final String errorCode;
    private final Class<? extends Throwable> exception;
    private final String message;
    private final HttpStatus status;
    private final String description;

    @Override
    public String toString() {
        return "ExceptionHandlerItem(message='" + message + "', code=" + errorCode + ", status=" + status + ")";
    }
}