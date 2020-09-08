package com.github.caffeineapi.requests;

import java.util.concurrent.CompletableFuture;

import com.github.caffeineapi.CaffeineAuth;

import lombok.SneakyThrows;

public abstract class AuthenticatedWebRequest<T> {
    private CaffeineAuth auth;

    public AuthenticatedWebRequest(CaffeineAuth auth) {
        this.auth = auth;
    }

    protected CaffeineAuth getAuth() {
        return this.auth;
    }

    @SneakyThrows
    public final T sendBlocking() {
        return this.send().get();
    }

    public abstract CompletableFuture<T> send();

}
