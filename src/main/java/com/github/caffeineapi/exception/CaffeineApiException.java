package com.github.caffeineapi.exception;

import lombok.Getter;

public class CaffeineApiException extends Exception {
    private static final long serialVersionUID = 3158706470903957776L;

    private @Getter String response;

    public CaffeineApiException(String reason, String response) {
        super(reason);

        this.response = response;
    }

    public CaffeineApiException(Throwable cause, String reason, String response) {
        super(reason, cause);

        this.response = response;
    }

}
