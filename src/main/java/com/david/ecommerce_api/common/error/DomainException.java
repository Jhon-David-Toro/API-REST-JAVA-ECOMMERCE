package com.david.ecommerce_api.common.error;

import lombok.Getter;

@Getter
public class DomainException extends RuntimeException{
    private final ErrorCode errorCode;
    private final Object[] args;

    public DomainException(ErrorCode errorCode, Object... args) {
        super(errorCode.name());
        this.errorCode = errorCode;
        this.args = args;
    }

}
