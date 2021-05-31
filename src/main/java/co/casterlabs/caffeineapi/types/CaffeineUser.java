package co.casterlabs.caffeineapi.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import co.casterlabs.caffeineapi.CaffeineApi;
import co.casterlabs.caffeineapi.CaffeineEndpoints;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CaffeineUser {
    private String bio;

    private String username;

    private UserBadge badge;

    @SerializedName("name")
    private String displayname;

    @SerializedName("stage_id")
    private String stageID;

    @SerializedName("broadcast_id")
    private String broadcastID;

    @SerializedName("caid")
    private String CAID;

    @SerializedName("followers_count")
    private long followersCount;

    @SerializedName("following_count")
    private long followingCount;

    @SerializedName("avatar_image_path")
    private String imageLink;

    public static CaffeineUser fromJson(JsonObject user) {
        CaffeineUser result = CaffeineApi.GSON.fromJson(user, CaffeineUser.class);

        // Caffeine's "no badge" is always null
        if (result.badge == null) {
            result.badge = UserBadge.NONE;
        }

        result.imageLink = CaffeineEndpoints.IMAGES + result.imageLink;  // Prepend the images endpoint to the link.

        return result;
    }

    @AllArgsConstructor
    public static enum UserBadge {
        NONE("https://raw.githubusercontent.com/Casterlabs/CaffeineApiJava/master/badges/none.png"),
        CASTER("https://raw.githubusercontent.com/Casterlabs/CaffeineApiJava/master/badges/caster.png"),
        CYAN("https://raw.githubusercontent.com/Casterlabs/CaffeineApiJava/master/badges/cyan.png"),
        VERIFIED("https://raw.githubusercontent.com/Casterlabs/CaffeineApiJava/master/badges/verified.png"),
        UNKNOWN("https://raw.githubusercontent.com/Casterlabs/CaffeineApiJava/master/badges/none.png");

        private @Getter String imageLink;

        public static UserBadge from(JsonElement element) {
            if ((element == null) || element.isJsonNull()) {
                return NONE;
            } else {
                switch (element.getAsString()) {
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

}
