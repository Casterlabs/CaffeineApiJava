package co.casterlabs.caffeineapi.types;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import co.casterlabs.caffeineapi.CaffeineApi;
import co.casterlabs.caffeineapi.CaffeineEndpoints;
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

}
