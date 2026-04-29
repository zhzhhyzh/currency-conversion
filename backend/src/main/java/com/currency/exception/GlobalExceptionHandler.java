package com.currency.exception;

import com.currency.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CurrencyException.class)
    public ResponseEntity<ErrorResponseDto> handleCurrencyException(
            CurrencyException ex,
            WebRequest request) {
        
        log.error("Currency exception occurred: {}", ex.getMessage());
        
        ErrorResponseDto errorResponse = new ErrorResponseDto(
            ex.getStatusCode(),
            ex.getMessage(),
            request.getDescription(false),
            System.currentTimeMillis()
        );
        
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingRequestParameter(
            MissingServletRequestParameterException ex,
            WebRequest request) {

        String message = "Required query parameter '" + ex.getParameterName() + "' is missing";
        log.warn("Request validation failed: {}", message);

        ErrorResponseDto errorResponse = new ErrorResponseDto(
            400,
            message,
            request.getDescription(false),
            System.currentTimeMillis()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            WebRequest request) {

        String parameterName = ex.getName();
        String message = "Query parameter '" + parameterName + "' must be a valid number";
        log.warn("Request validation failed: {}", message);

        ErrorResponseDto errorResponse = new ErrorResponseDto(
            400,
            message,
            request.getDescription(false),
            System.currentTimeMillis()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(
            Exception ex,
            WebRequest request) {
        
        log.error("Unexpected exception occurred: ", ex);
        
        ErrorResponseDto errorResponse = new ErrorResponseDto(
            500,
            "Internal Server Error",
            request.getDescription(false),
            System.currentTimeMillis()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
