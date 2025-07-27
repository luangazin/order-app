package br.com.gazintech.orderapp.exception_handler;

import br.com.gazintech.orderapp.api.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Created by IntelliJ IDEA.<br/>
 * User: luan-gazin<br/>
 * Date: 06/06/2025<br/>
 * Time: 10:55<br/>
 * To change this template use File | Settings | File Templates.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ExceptionHandlerList exceptionHandlerList;


    public GlobalExceptionHandler(ExceptionHandlerList exceptionHandlerList) {
        this.exceptionHandlerList = exceptionHandlerList;
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Throwable exception) {
        log.error("An error occurred: {}", exception.getMessage(), exception);
        var item = exceptionHandlerList.findByException(exception);
        log.trace("Exception handler item found: {}", item);
        return ApiResponse.<Void>builder().error(item.errorCode(), item.isShowOriginalMessage() ? exception.getMessage() : item.message(), item.status()).build();
    }
}
