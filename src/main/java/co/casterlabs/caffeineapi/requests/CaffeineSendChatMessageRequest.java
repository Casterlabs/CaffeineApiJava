package co.casterlabs.caffeineapi.requests;

import java.io.IOException;

import com.google.gson.JsonObject;
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
public class CaffeineSendChatMessageRequest extends AuthenticatedWebRequest<Void, CaffeineAuth> {
    private @NonNull String stageId;
    private @NonNull String message;

    public CaffeineSendChatMessageRequest(CaffeineAuth auth) {
        super(auth);
    }

    public CaffeineSendChatMessageRequest setMessage(@NonNull String message) {
        if (message.length() > 80) {
            throw new IllegalArgumentException("Message length cannot exceed 80 characters.");
        }

        this.message = message;

        return this;
    }

    public CaffeineSendChatMessageRequest setUser(String username) throws ApiAuthException, ApiException {
        CaffeineUserInfoRequest request = new CaffeineUserInfoRequest();

        request.setQuery(username);

        this.stageId = request.send().getStageID();

        return this;
    }

    public CaffeineSendChatMessageRequest setCAID(@NonNull String caid) {
        this.stageId = caid.substring(4);
        return this;
    }

    @Override
    protected Void execute() throws ApiException, ApiAuthException, IOException {
        if ((this.message == null) || (this.stageId == null)) {
            throw new NullPointerException("CAID or message is null.");
        }

        JsonObject post = new JsonObject();
        JsonObject body = new JsonObject();

        body.addProperty("text", this.message);

        post.addProperty("type", "reaction");
        post.addProperty("publisher", this.auth.getSignedToken());
        post.add("body", body);

        Response response = HttpUtil.sendHttp(post.toString(), String.format(CaffeineEndpoints.CHAT_MESSAGE, this.stageId), this.auth, "application/json");

        response.close();

        if (response.code() == 401) {
            throw new ApiAuthException("Auth is invalid");
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
