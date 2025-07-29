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
        var handler = exceptionHandlerList.findByClass(OrderNotFoundException.class);

        assertEquals("E008", handler.errorCode());
        assertEquals(OrderNotFoundException.class, handler.exception());
        assertEquals(HttpStatus.NOT_FOUND, handler.status());
    }

    @Test
    void findByClass_WhenExceptionDoesNotExist_ShouldReturnDefaultHandler() {
        var handler = exceptionHandlerList.findByClass(RuntimeException.class);

        assertEquals("E000", handler.errorCode());
        assertEquals(RuntimeException.class, handler.exception());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, handler.status());
        assertTrue(handler.isShowOriginalMessage());
    }

    @Test
    void findByClass_WithHttpRequestMethodNotSupportedException_ShouldReturnCorrectHandler() {
        var handler = exceptionHandlerList.findByClass(HttpRequestMethodNotSupportedException.class);

        assertEquals("E005", handler.errorCode());
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, handler.status());
        assertTrue(handler.isShowOriginalMessage());
    }

    @Test
    void findByClass_WithMissingRequestHeaderException_ShouldReturnCorrectHandler() {
        var handler = exceptionHandlerList.findByClass(MissingRequestHeaderException.class);

        assertEquals("E006", handler.errorCode());
        assertEquals(HttpStatus.BAD_REQUEST, handler.status());
        assertTrue(handler.isShowOriginalMessage());
    }
}