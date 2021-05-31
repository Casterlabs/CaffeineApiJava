package co.casterlabs.caffeineapi.requests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import co.casterlabs.apiutil.web.ApiException;
import co.casterlabs.apiutil.web.WebRequest;
import co.casterlabs.caffeineapi.CaffeineApi;
import co.casterlabs.caffeineapi.CaffeineEndpoints;
import co.casterlabs.caffeineapi.HttpUtil;
import co.casterlabs.caffeineapi.types.CaffeineGame;
import okhttp3.Response;

public class CaffeineGamesListRequest extends WebRequest<List<CaffeineGame>> {

    @Override
    protected List<CaffeineGame> execute() throws ApiException, IOException {
        try (Response response = HttpUtil.sendHttpGet(CaffeineEndpoints.GAMES_LIST, null)) {
            String body = response.body().string();

            response.close();

            JsonArray array = CaffeineApi.GSON.fromJson(body, JsonArray.class);
            List<CaffeineGame> list = new ArrayList<>();

            for (JsonElement element : array) {
                try {
                    CaffeineGame game = CaffeineApi.GSON.fromJson(element, CaffeineGame.class);

                    list.add(game);
                } catch (JsonSyntaxException e) {
                    throw new ApiException("Could not parse CaffeineGame: " + element.toString(), e);
                }
            }

            return list;
        }
    }

}
