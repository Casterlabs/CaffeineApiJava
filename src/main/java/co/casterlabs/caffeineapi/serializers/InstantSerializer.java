package co.casterlabs.caffeineapi.serializers;

import java.lang.reflect.Type;
import java.time.Instant;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class InstantSerializer implements JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            String value = json.getAsString();

            return Instant.parse(value);
        } catch (Exception e) {
            throw new JsonParseException("Unable to parse instant", e);
        }
    }

}
