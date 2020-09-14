package com.github.caffeineapi.requests;

import com.github.caffeineapi.CaffeineApi;
import com.github.caffeineapi.CaffeineEndpoints;
import com.github.caffeineapi.HttpUtil;
import com.github.caffeineapi.exception.CaffeineApiException;
import com.github.caffeineapi.requests.CaffeineUserInfoRequest.CaffeineUser;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import okhttp3.Response;

@Setter
@Accessors(chain = true)
public class CaffeineUserInfoRequest extends WebRequest<CaffeineUser> {
    private @NonNull String query;

    public CaffeineUserInfoRequest setUsername(@NonNull String username) {
        this.query = username;
        return this;
    }

    public CaffeineUserInfoRequest setUser(@NonNull CaffeineUser user) {
        this.query = user.getCAID();
        return this;
    }

    public CaffeineUserInfoRequest setCAID(@NonNull String caid) {
        this.query = caid;
        return this;
    }

    @Override
    public CaffeineUser send() throws Exception {
        Response response = HttpUtil.sendHttpGet(String.format(CaffeineEndpoints.USERS, this.query), null);
        String body = response.body().string();

        if (response.code() == 404) {
            throw new CaffeineApiException("User does not exist", body);
        } else {
            JsonObject json = CaffeineApi.GSON.fromJson(body, JsonObject.class);
            JsonObject user = json.getAsJsonObject("user");

            user.addProperty("avatar_image_path", CaffeineEndpoints.IMAGES + user.get("avatar_image_path").getAsString()); // Prepend the images endpoint to the link.

            CaffeineUser result = CaffeineApi.GSON.fromJson(user, CaffeineUser.class);

            return result;
        }
    }

    @Getter
    @ToString
    public static class CaffeineUser {
        private String username;
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

    }

    @Getter
    @ToString
    public static class CaffeineProp {
        private String id;
        private String name;
        @SerializedName("gold_cost")
        private int goldCost;
        private int credits;
        @SerializedName("plural_name")
        private String pluralName;
        @SerializedName("preview_image_path")
        private String previewImagePath;
        @SerializedName("static_image_path")
        private String staticImagePath;
        @SerializedName("web_asset_path")
        private String webAssetPath;
        @SerializedName("scene_kit_path")
        private String sceneKitPath;
    }

}
