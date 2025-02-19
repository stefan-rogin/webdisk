package com.example.webdisk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * GlobalExceptionHandler is a controller advice class that handles exceptions
 * thrown by any controller in the application. It logs the error details and 
 * returns a generic "Internal Server Error" response.
 * 
 * <p>This class uses the {@link ControllerAdvice} annotation to allow it to 
 * handle exceptions globally across the whole application.</p>
 * 
 * <p>The {@link #handleGlobalException(Exception, WebRequest)} method is 
 * annotated with {@link ExceptionHandler} to handle all types of exceptions. 
 * It logs the error message and the request description, then returns a 
 * response entity with an HTTP status of 500 (Internal Server Error).</p>
 * 
 * @see ControllerAdvice
 * @see ExceptionHandler
 * @see ResponseEntity
 * @see HttpStatus
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    /**
     * Logger instance for logging messages and exceptions in the GlobalExceptionHandler class.
     */
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handle all other exceptions
    /**
     * Handles all exceptions that are not explicitly handled by other exception handlers.
     *
     * @param e the exception that was thrown
     * @param request the web request during which the exception was thrown
     * @return a ResponseEntity containing an error message and an HTTP status code of 500 (Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception e, WebRequest request) {
        logger.error("Error. @Request:{} @Cause:{}",request.getDescription(false), e.getMessage());
        return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
