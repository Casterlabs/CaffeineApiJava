package com.github.caffeineapi.requests;

import java.util.ArrayList;
import java.util.List;

import com.github.caffeineapi.CaffeineApi;
import com.github.caffeineapi.CaffeineEndpoints;
import com.github.caffeineapi.HttpUtil;
import com.github.caffeineapi.exception.CaffeineApiException;
import com.github.caffeineapi.requests.CaffeineGamesListRequest.CaffeineGame;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.ToString;
import okhttp3.Response;

public class CaffeineGamesListRequest extends WebRequest<List<CaffeineGame>> {

    @Override
    public List<CaffeineGame> send() throws Exception {
        Response response = HttpUtil.sendHttpGet(CaffeineEndpoints.GAMES_LIST, null);
        String body = response.body().string();
        JsonArray array = CaffeineApi.GSON.fromJson(body, JsonArray.class);

        List<CaffeineGame> list = new ArrayList<>();

        for (JsonElement element : array) {
            try {
                CaffeineGame game = CaffeineApi.GSON.fromJson(element, CaffeineGame.class);

                game.iconImagePath = CaffeineEndpoints.IMAGES + game.iconImagePath;
                game.bannerImagePath = CaffeineEndpoints.IMAGES + game.bannerImagePath;

                list.add(game);
            } catch (JsonSyntaxException e) {
                throw new CaffeineApiException(e, "Could not parse CaffeineGame", element.toString());
            }
        }

        return list;
    }

    @Getter
    @ToString
    public static class CaffeineGame {
        private long id;
        private String name;
        private String description;
        @SerializedName("icon_image_path")
        private String iconImagePath;
        @SerializedName("banner_image_path")
        private String bannerImagePath;
        private String website;
        @SerializedName("process_names")
        private List<String> processNames;
        @SerializedName("executable_name")
        private String excecutableName;
        @SerializedName("window_title")
        private String windowTitle;
        private boolean supported;
        @SerializedName("is_capture_software")
        private boolean isCaptureSoftware;
    }

}
