package co.casterlabs.caffeineapi;

import java.time.Instant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.casterlabs.caffeineapi.serializers.InstantSerializer;

public class CaffeineApi {
    // @formatter:off
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantSerializer())
            .create();
    // @formatter:on

}
