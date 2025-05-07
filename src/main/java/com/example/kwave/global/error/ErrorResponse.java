package com.example.kwave.global.error;

import lombok.Getter;

@Getter
public class ErrorResponse {
    private final String errorType;
    private final String message;
    private final int code;

    public ErrorResponse(ExceptionBase ex) {
        this.errorType = ex.getErrorCode().getErrorType();
        this.message = ex.getMessage();
        this.code = ex.getErrorCode().getCode();
    }
}