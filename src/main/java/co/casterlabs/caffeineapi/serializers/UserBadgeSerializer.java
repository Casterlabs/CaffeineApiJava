package co.casterlabs.caffeineapi.serializers;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import co.casterlabs.caffeineapi.types.CaffeineUser.UserBadge;

public class UserBadgeSerializer implements JsonDeserializer<UserBadge> {

    @Override
    public UserBadge deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return UserBadge.from(json);
        } catch (Exception e) {
            throw new JsonParseException("Unable to parse badge", e);
        }
    }

}
