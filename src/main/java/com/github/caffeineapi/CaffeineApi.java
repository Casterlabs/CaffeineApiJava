package com.github.caffeineapi;

import java.net.Proxy;
import java.time.Instant;

import com.github.caffeineapi.serializers.InstantSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class CaffeineApi {
    // @formatter:off
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantSerializer())
            .create();
    // @formatter:on

    private static @NonNull @Getter @Setter Proxy proxy = Proxy.NO_PROXY;

}
