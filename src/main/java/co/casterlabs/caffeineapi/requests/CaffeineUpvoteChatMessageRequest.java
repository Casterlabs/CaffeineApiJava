package co.casterlabs.caffeineapi.requests;

import java.io.IOException;

import com.google.gson.annotations.SerializedName;

import co.casterlabs.apiutil.auth.ApiAuthException;
import co.casterlabs.apiutil.web.ApiException;
import co.casterlabs.apiutil.web.AuthenticatedWebRequest;
import co.casterlabs.caffeineapi.CaffeineAuth;
import co.casterlabs.caffeineapi.CaffeineEndpoints;
import co.casterlabs.caffeineapi.HttpUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import okhttp3.Response;

@Setter
@Accessors(chain = true)
public class CaffeineUpvoteChatMessageRequest extends AuthenticatedWebRequest<Void, CaffeineAuth> {
    private @NonNull String messageId;

    public CaffeineUpvoteChatMessageRequest(CaffeineAuth auth) {
        super(auth);
    }

    @Override
    protected Void execute() throws ApiException, ApiAuthException, IOException {
        Response response = HttpUtil.sendHttp("{}", String.format(CaffeineEndpoints.UPVOTE_MESSAGE, this.messageId), this.auth, "application/json");

        response.close();

        if (response.code() == 401) {
            throw new ApiAuthException("Auth is invalid");
        } else if (response.code() == 401) {
            throw new ApiException("Unable to upvote a chat message due to an authentication error");
        } else if (response.code() == 400) {
            throw new IllegalArgumentException("Message id is invalid");
        }

        return null;
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
