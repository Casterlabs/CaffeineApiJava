package com.github.caffeineapi.requests;

import com.github.caffeineapi.CaffeineAuth;

public abstract class AuthenticatedWebRequest<T> extends WebRequest<T> {
    private CaffeineAuth auth;

    public AuthenticatedWebRequest(CaffeineAuth auth) {
        this.auth = auth;
    }

    protected CaffeineAuth getAuth() {
        return this.auth;
    }

}
