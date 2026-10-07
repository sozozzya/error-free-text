package com.senla.errorfreetext.controller;

import com.senla.errorfreetext.controller.dto.ErrorResponse;
import com.senla.errorfreetext.exception.ErrorCodes;
import com.senla.errorfreetext.exception.TaskNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TaskNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, ErrorCodes.TASK_NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = "Request validation failed";
        }
        return error(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        String message = "Malformed request body";
        if (ex.getMessage() != null && ex.getMessage().contains("Language")) {
            message = "language must be RU or EN";
        }
        return error(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION, message, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        return error(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.VALIDATION,
                "Invalid value for parameter '" + ex.getName() + "'",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error while processing {}", request.getRequestURI(), ex);
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCodes.INTERNAL,
                "Internal server error",
                request
        );
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    private ResponseEntity<ErrorResponse> error(
            HttpStatus status,
            int errorCode,
            String message,
            HttpServletRequest request
    ) {
        ErrorResponse body = new ErrorResponse(
                message,
                errorCode,
                Instant.now(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}
