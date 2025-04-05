package com.example.webdisk;

import org.springframework.lang.Nullable;
import org.springframework.lang.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * GlobalExceptionHandler is a controller advice class that handles exceptions
 * thrown by any controller in the application. It logs the error details and
 * returns a generic "Internal Server Error" response.
 * 
 * <p>
 * This class uses the {@link ControllerAdvice} annotation to allow it to
 * handle exceptions globally across the whole application.
 * </p>
 * 
 * <p>
 * The {@link #handleGlobalException(Exception, WebRequest)} method is
 * annotated with {@link ExceptionHandler} to handle all types of exceptions.
 * It logs the error message and the request description, then returns a
 * response entity with an HTTP status of 500 (Internal Server Error).
 * </p>
 * 
 * @see ControllerAdvice
 * @see ExceptionHandler
 * @see ResponseEntity
 * @see HttpStatus
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    /**
     * Logger instance for logging messages and exceptions in the
     * GlobalExceptionHandler class.
     */
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles all exceptions that are not explicitly handled by other exception
     * handlers.
     *
     * @param e       the exception that was thrown
     * @param request the web request during which the exception was thrown
     * @return a ResponseEntity containing an error message and an HTTP status code
     *         of 500 (Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception e, WebRequest request) {
        logger.error("Error. @Request:{} @Cause:{}", request.getDescription(false), e.getMessage());
        return new ResponseEntity<>(ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error."), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Overrides internal handler to include logging
     * 
     * @param ex
     * @param body
     * @param headers
     * @param statusCode
     * @param request
     * @return
     */
    protected ResponseEntity<Object> handleExceptionInternal(
            @NonNull Exception ex,
            @Nullable Object body,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode statusCode,
            @NonNull WebRequest request) {
        logger.warn(ex.getMessage());
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }
}
