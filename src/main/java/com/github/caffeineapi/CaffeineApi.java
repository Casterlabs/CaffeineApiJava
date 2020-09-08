package com.github.caffeineapi;

import java.net.Proxy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class CaffeineApi {
    // @formatter:off
    public static final Gson GSON = new GsonBuilder()
            .create();
    // @formatter:on

    private static @NonNull @Getter @Setter Proxy proxy = Proxy.NO_PROXY;

}
