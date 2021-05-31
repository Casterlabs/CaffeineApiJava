package co.casterlabs.caffeineapi.types;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonSerializer;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@JsonClass(serializer = Serializer.class)
public enum UserBadge {
    NONE("https://raw.githubusercontent.com/Casterlabs/CaffeineApiJava/master/badges/none.png"),
    CASTER("https://raw.githubusercontent.com/Casterlabs/CaffeineApiJava/master/badges/caster.png"),
    CYAN("https://raw.githubusercontent.com/Casterlabs/CaffeineApiJava/master/badges/cyan.png"),
    VERIFIED("https://raw.githubusercontent.com/Casterlabs/CaffeineApiJava/master/badges/verified.png"),
    UNKNOWN("https://raw.githubusercontent.com/Casterlabs/CaffeineApiJava/master/badges/none.png");

    private @Getter String imageLink;

    public static UserBadge from(String str) {
        if (str == null) {
            return NONE;
        } else {
            switch (str) {
                case "CASTER":
                    return CASTER;

                case "PARTNER1":
                    return CYAN;

                // Caffeine has repurposed these badges for something else entirely.
                // However, I am not going to include them because they're probably
                // going to change them again in the future anyways ¯\_(ツ)_/¯

                // case "PARTNER2":

                // case "PARTNER3":

                case "VERIFIED":
                    return VERIFIED;

                default:
                    return UNKNOWN;
            }
        }
    }

}

class Serializer implements JsonSerializer<UserBadge> {

    @Override
    public @Nullable UserBadge deserialize(@NonNull JsonElement value, @NonNull Class<?> type, @NonNull Rson rson) throws JsonParseException {
        try {
            return UserBadge.from(value.isJsonNull() ? null : value.getAsString());
        } catch (Exception e) {
            throw new JsonParseException(e);
        }
    }

}
