package co.casterlabs.caffeineapi;

import java.time.Instant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.casterlabs.caffeineapi.serializers.InstantSerializer;
import co.casterlabs.caffeineapi.serializers.UserBadgeSerializer;
import co.casterlabs.caffeineapi.types.CaffeineUser.UserBadge;

public class CaffeineApi {
    // @formatter:off
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(UserBadge.class, new UserBadgeSerializer())
            .registerTypeAdapter(Instant.class, new InstantSerializer())
            .create();
    // @formatter:on

}
