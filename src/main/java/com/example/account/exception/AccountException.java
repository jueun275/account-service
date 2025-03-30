package com.example.account.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AccountException extends RuntimeException {
    private ErrorCode errorCode;
    private String message;

    public AccountException(ErrorCode errorCode) {
        this.message = errorCode.getDescription();
        this.errorCode = errorCode;
    }
}
