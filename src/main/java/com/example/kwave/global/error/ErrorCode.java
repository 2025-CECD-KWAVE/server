package com.example.kwave.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    UNAUTHORIZED(4030, "UNAUTHORIZED"),

    NOT_FOUND(4040, "NOT_FOUND"),

    ALL_SOLD_OUT(4090,"ALL_SOLD_OUT"),
    INVALID_INVITATION(4091,"INVALID_INVITATION");

    private final int code;
    private final String errorType;

}