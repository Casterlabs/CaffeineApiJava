package co.casterlabs.caffeineapi.requests;

import java.io.IOException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import co.casterlabs.apiutil.web.ApiException;
import co.casterlabs.apiutil.web.WebRequest;
import co.casterlabs.caffeineapi.CaffeineApi;
import co.casterlabs.caffeineapi.CaffeineEndpoints;
import co.casterlabs.caffeineapi.HttpUtil;
import co.casterlabs.caffeineapi.requests.CaffeineUserInfoRequest.CaffeineUser;
import lombok.AllArgsConstructor;
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
    protected CaffeineUser execute() throws ApiException, IOException {
        Response response = HttpUtil.sendHttpGet(String.format(CaffeineEndpoints.USERS, this.query), null);
        String body = response.body().string();

        response.close();

        if (response.code() == 404) {
            throw new ApiException("User does not exist: " + body);
        } else {
            JsonObject json = CaffeineApi.GSON.fromJson(body, JsonObject.class);
            JsonObject user = json.getAsJsonObject("user");

            return fromJson(user);
        }
    }

    public static CaffeineUser fromJson(JsonObject user) {
        CaffeineUser result = CaffeineApi.GSON.fromJson(user, CaffeineUser.class);

        if (result.badge == null) {
            result.badge = UserBadge.NONE;
        }

        result.imageLink = CaffeineEndpoints.IMAGES + result.imageLink;  // Prepend the images endpoint to the link.

        return result;
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
        @SerializedName("avatar_image_path")
        private String imageLink;
        private UserBadge badge;

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

                    // Nobody is of this partner level yet.

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
