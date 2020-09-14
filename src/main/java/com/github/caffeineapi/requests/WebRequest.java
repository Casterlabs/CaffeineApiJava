package com.github.caffeineapi.requests;

import java.util.concurrent.CompletableFuture;

import com.github.caffeineapi.ThreadHelper;

public abstract class WebRequest<T> {

    public abstract T send() throws Exception;

    public final CompletableFuture<T> sendAsync() {
        CompletableFuture<T> future = new CompletableFuture<>();

        ThreadHelper.executeAsync(() -> {
            try {
                future.complete(this.send());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

}
