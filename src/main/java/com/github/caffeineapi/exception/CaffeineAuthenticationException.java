package com.github.caffeineapi.exception;

import lombok.Getter;

public class CaffeineAuthenticationException extends Exception {
    private static final long serialVersionUID = 2525811747715888301L;

    private @Getter String response;

    public CaffeineAuthenticationException(String reason, String response) {
        super(reason);

        this.response = response;
    }

    public CaffeineAuthenticationException(Throwable cause, String reason, String response) {
        super(reason, cause);

        this.response = response;
    }

    public CaffeineAuthenticationException(String reason) {
        super(reason);
    }

}
