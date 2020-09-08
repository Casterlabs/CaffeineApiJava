package com.github.caffeineapi.requests;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.github.caffeineapi.CaffeineAuth;
import com.github.caffeineapi.CaffeineEndpoints;
import com.github.caffeineapi.HttpUtil;
import com.github.caffeineapi.ThreadHelper;
import com.github.caffeineapi.exception.CaffeineAuthenticationException;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import okhttp3.Response;

@Accessors(chain = true)
public class CaffeineSendChatMessageRequest extends AuthenticatedWebRequest<Void> {
    private @Setter @NonNull String stageId;
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

    public CaffeineSendChatMessageRequest setCAID(@NonNull String caid) {
        this.stageId = caid.substring(4);
        return this;
    }

    @Override
    public CompletableFuture<Void> send() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        ThreadHelper.executeAsync(() -> {
            try {
                if ((this.message == null) || (this.stageId == null)) {
                    future.completeExceptionally(new NullPointerException("CAID or message is null."));
                }

                Map<String, String> headers = Collections.singletonMap("Authorization", "Bearer " + this.getAuth().getAccessToken());
                JsonObject post = new JsonObject();
                JsonObject body = new JsonObject();

                body.addProperty("text", this.message);

                post.addProperty("type", "reaction");
                post.addProperty("publisher", this.getAuth().getSignedToken());
                post.add("body", body);

                Response response = HttpUtil.sendHttp(post.toString(), String.format(CaffeineEndpoints.CHAT_MESSAGE, this.stageId), headers, "application/json");

                if (response.code() == 401) {
                    future.completeExceptionally(new CaffeineAuthenticationException("Unable to send a chat message due to an authentication error"));
                }

                future.complete(null);
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });

        return future;
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
