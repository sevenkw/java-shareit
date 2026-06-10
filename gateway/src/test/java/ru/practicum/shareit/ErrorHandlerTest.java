package ru.practicum.shareit;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.exception.ErrorResponse;
import ru.practicum.shareit.exception.IllegalArgumentException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ErrorHandlerTest {

    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void handleConstraintViolationException() {
        ConstraintViolationException exception = new ConstraintViolationException("validation error", null);

        ErrorResponse response = errorHandler.handleMethodArgumentNotValidException(exception);

        assertEquals("validation error", response.getError());
    }

    @Test
    void handleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("wrong state");

        ErrorResponse response = errorHandler.handleIllegalArgumentException(exception);

        assertEquals("wrong state", response.getError());
    }

    @Test
    void handleException() {
        Exception exception = new Exception("server error");

        ErrorResponse response = errorHandler.handleException(exception);

        assertEquals("server error", response.getError());
    }

    @Test
    void errorResponse() {
        ErrorResponse response = new ErrorResponse("error");

        assertEquals("error", response.getError());
    }

    @Test
    void illegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("message");

        assertEquals("message", exception.getMessage());
    }

    private static class TestController {
        public void test(String value) {
        }
    }
}
