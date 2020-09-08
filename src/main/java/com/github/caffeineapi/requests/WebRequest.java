package com.github.caffeineapi.requests;

import java.util.concurrent.CompletableFuture;

import lombok.SneakyThrows;

public abstract class WebRequest<T> {

    @SneakyThrows
    public final T sendBlocking() {
        return this.send().get();
    }

    public abstract CompletableFuture<T> send();

}
