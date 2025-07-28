package br.com.gazintech.orderapp.exception_handler;

import br.com.gazintech.orderapp.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExceptionHandlerListImplTest {

    private ExceptionHandlerListImpl exceptionHandlerList;

    @BeforeEach
    void setUp() {
        exceptionHandlerList = new ExceptionHandlerListImpl();
    }

    @Test
    void findByException_WhenExceptionExists_ShouldReturnCorrectHandler() {
        var orderNotFoundException = new OrderNotFoundException("Order not found");
        var handler = exceptionHandlerList.findByException(orderNotFoundException);

        assertEquals("E008", handler.errorCode());
        assertEquals(OrderNotFoundException.class, handler.exception());
        assertEquals(HttpStatus.NOT_FOUND, handler.status());
    }

    @Test
    void findByException_WhenExceptionDoesNotExist_ShouldReturnDefaultHandler() {
        var unknownException = new RuntimeException("Unknown error");
        var handler = exceptionHandlerList.findByException(unknownException);

        assertEquals("E000", handler.errorCode());
        assertEquals(RuntimeException.class, handler.exception());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, handler.status());
        assertTrue(handler.isShowOriginalMessage());
    }

    @Test
    void findByException_WithHttpRequestMethodNotSupportedException_ShouldReturnCorrectHandler() {
        var methodNotSupportedException = new HttpRequestMethodNotSupportedException("POST");
        var handler = exceptionHandlerList.findByException(methodNotSupportedException);

        assertEquals("E005", handler.errorCode());
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, handler.status());
        assertTrue(handler.isShowOriginalMessage());
    }

    @Test
    void findByException_WithMissingRequestHeaderException_ShouldReturnCorrectHandler() {
        var missingHeaderException = new MissingRequestHeaderException("X-Required-Header", null);
        var handler = exceptionHandlerList.findByException(missingHeaderException);

        assertEquals("E006", handler.errorCode());
        assertEquals(HttpStatus.BAD_REQUEST, handler.status());
        assertTrue(handler.isShowOriginalMessage());
    }
}