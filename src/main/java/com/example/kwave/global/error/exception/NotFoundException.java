package com.example.kwave.global.error.exception;

import com.example.kwave.global.error.ErrorCode;
import com.example.kwave.global.error.ExceptionBase;

public class NotFoundException extends ExceptionBase {
    public NotFoundException(String message) {
        super(message, ErrorCode.NOT_FOUND);
    }
}