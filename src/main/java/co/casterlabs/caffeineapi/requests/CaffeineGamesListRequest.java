package co.casterlabs.caffeineapi.requests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import co.casterlabs.apiutil.web.ApiException;
import co.casterlabs.apiutil.web.WebRequest;
import co.casterlabs.caffeineapi.CaffeineApi;
import co.casterlabs.caffeineapi.CaffeineEndpoints;
import co.casterlabs.caffeineapi.HttpUtil;
import co.casterlabs.caffeineapi.requests.CaffeineGamesListRequest.CaffeineGame;
import lombok.Getter;
import lombok.ToString;
import okhttp3.Response;

public class CaffeineGamesListRequest extends WebRequest<List<CaffeineGame>> {

    @Override
    protected List<CaffeineGame> execute() throws ApiException, IOException {
        Response response = HttpUtil.sendHttpGet(CaffeineEndpoints.GAMES_LIST, null);
        String body = response.body().string();

        response.close();

        JsonArray array = CaffeineApi.GSON.fromJson(body, JsonArray.class);
        List<CaffeineGame> list = new ArrayList<>();

        for (JsonElement element : array) {
            try {
                CaffeineGame game = CaffeineApi.GSON.fromJson(element, CaffeineGame.class);

                game.iconImagePath = CaffeineEndpoints.IMAGES + game.iconImagePath;
                game.bannerImagePath = CaffeineEndpoints.IMAGES + game.bannerImagePath;

                list.add(game);
            } catch (JsonSyntaxException e) {
                throw new ApiException("Could not parse CaffeineGame: " + element.toString(), e);
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
