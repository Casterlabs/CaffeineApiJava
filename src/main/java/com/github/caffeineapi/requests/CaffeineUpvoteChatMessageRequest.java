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
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import okhttp3.Response;

@Setter
@Accessors(chain = true)
public class CaffeineUpvoteChatMessageRequest extends AuthenticatedWebRequest<Void> {
    private @NonNull String messageId;

    public CaffeineUpvoteChatMessageRequest(CaffeineAuth auth) {
        super(auth);
    }

    @Override
    public CompletableFuture<Void> send() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        ThreadHelper.executeAsync(() -> {
            try {
                Map<String, String> headers = Collections.singletonMap("Authorization", "Bearer " + this.getAuth().getAccessToken());
                Response response = HttpUtil.sendHttp("{}", String.format(CaffeineEndpoints.UPVOTE_MESSAGE, this.messageId), headers, "application/json");

                if (response.code() == 401) {
                    future.completeExceptionally(new CaffeineAuthenticationException("Unable to upvote a chat message due to an authentication error"));
                } else if (response.code() == 400) {
                    future.completeExceptionally(new IllegalArgumentException("Message id is invalid"));
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
