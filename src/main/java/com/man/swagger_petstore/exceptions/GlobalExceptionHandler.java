package com.man.swagger_petstore.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, WebRequest request) {
        ErrorResponse err = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getDetails(),
                request.getDescription(false).replace("uri=","")
        );


        return new ResponseEntity<>(err, HttpStatus.valueOf(ex.getErrorCode()));
    }
}
