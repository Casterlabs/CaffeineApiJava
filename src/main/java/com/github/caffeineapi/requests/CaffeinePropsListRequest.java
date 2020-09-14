package com.github.caffeineapi.requests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.caffeineapi.CaffeineApi;
import com.github.caffeineapi.CaffeineAuth;
import com.github.caffeineapi.CaffeineEndpoints;
import com.github.caffeineapi.HttpUtil;
import com.github.caffeineapi.exception.CaffeineApiException;
import com.github.caffeineapi.exception.CaffeineAuthenticationException;
import com.github.caffeineapi.requests.CaffeinePropsListRequest.CaffeineProp;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.ToString;
import okhttp3.Response;

public class CaffeinePropsListRequest extends AuthenticatedWebRequest<List<CaffeineProp>> {

    public CaffeinePropsListRequest(CaffeineAuth auth) {
        super(auth);
    }

    @Override
    public List<CaffeineProp> send() throws Exception {
        Map<String, String> headers = new HashMap<>();

        headers.put("x-credential", this.getAuth().getCredential());

        Response response = HttpUtil.sendHttp("{}", CaffeineEndpoints.PROPS_LIST, headers, "application/json"); // Send empty json data, because it's required for some reason.
        String body = response.body().string();

        if (response.code() == 401) {
            throw new CaffeineAuthenticationException("Unable to get props list due to an authentication error", body);
        } else {
            JsonObject json = CaffeineApi.GSON.fromJson(body, JsonObject.class);
            JsonObject payload = json.getAsJsonObject("payload");
            JsonObject digitalItems = payload.getAsJsonObject("digital_items");
            JsonArray state = digitalItems.getAsJsonArray("state");

            List<CaffeineProp> list = new ArrayList<>();

            for (JsonElement element : state) {
                try {
                    CaffeineProp prop = CaffeineApi.GSON.fromJson(element, CaffeineProp.class);

                    prop.previewImagePath = CaffeineEndpoints.ASSETS + prop.previewImagePath;
                    prop.staticImagePath = CaffeineEndpoints.ASSETS + prop.staticImagePath;
                    prop.webAssetPath = CaffeineEndpoints.ASSETS + prop.webAssetPath;
                    prop.sceneKitPath = CaffeineEndpoints.ASSETS + prop.sceneKitPath;

                    prop.credits = prop.goldCost * 3;

                    list.add(prop);
                } catch (JsonSyntaxException e) {
                    throw new CaffeineApiException(e, "Could not parse CaffeineProp", element.toString());
                }
            }

            return list;
        }
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
