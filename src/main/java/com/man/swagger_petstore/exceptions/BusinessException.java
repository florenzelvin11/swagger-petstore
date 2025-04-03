package com.man.swagger_petstore.exceptions;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
@Getter
public class BusinessException extends RuntimeException {
    private final int errorCode;
    private final String details;

    public BusinessException(int errorCode, String message, String details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }
}
