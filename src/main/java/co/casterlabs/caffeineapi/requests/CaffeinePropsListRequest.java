package co.casterlabs.caffeineapi.requests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import co.casterlabs.apiutil.ApiUtil;
import co.casterlabs.apiutil.auth.ApiAuthException;
import co.casterlabs.apiutil.web.ApiException;
import co.casterlabs.apiutil.web.AuthenticatedWebRequest;
import co.casterlabs.caffeineapi.CaffeineApi;
import co.casterlabs.caffeineapi.CaffeineAuth;
import co.casterlabs.caffeineapi.CaffeineEndpoints;
import co.casterlabs.caffeineapi.HttpUtil;
import co.casterlabs.caffeineapi.requests.CaffeinePropsListRequest.CaffeineProp;
import lombok.Getter;
import lombok.ToString;
import okhttp3.Response;

public class CaffeinePropsListRequest extends AuthenticatedWebRequest<List<CaffeineProp>, CaffeineAuth> {

    public CaffeinePropsListRequest(CaffeineAuth auth) {
        super(auth);
    }

    @Override
    protected List<CaffeineProp> execute() throws ApiException, ApiAuthException, IOException {
        Response response = HttpUtil.sendHttp("{}", CaffeineEndpoints.PROPS_LIST, this.auth, "application/json"); // Send empty json data, because it's required for some reason.
        String body = response.body().string();

        response.close();

        try {
            if (response.code() == 401) {
                throw new ApiAuthException("Auth is invalid: " + body);
            } else {
                JsonObject json = CaffeineApi.GSON.fromJson(body, JsonObject.class);
                JsonObject payload = json.getAsJsonObject("payload");
                JsonObject digitalItems = payload.getAsJsonObject("digital_items");
                JsonArray state = digitalItems.getAsJsonArray("state");

                System.out.println(json);

                List<CaffeineProp> list = new ArrayList<>();

                for (JsonElement element : state) {
                    list.add(fromJson(element));
                }

                return list;
            }
        } catch (Exception e) {
            ApiUtil.getErrorReporter().apiError(CaffeineEndpoints.PROPS_LIST, null, this.auth.getAuthHeaders(), body, response.headers().toMultimap(), e);
            throw e;
        }
    }

    public static CaffeineProp fromJson(JsonElement element) throws ApiException {
        try {
            CaffeineProp prop = CaffeineApi.GSON.fromJson(element, CaffeineProp.class);

            prop.universalVideoPropPath = CaffeineEndpoints.ASSETS + prop.universalVideoPropPath;
            prop.previewImagePath = CaffeineEndpoints.ASSETS + prop.previewImagePath;
            prop.staticImagePath = CaffeineEndpoints.ASSETS + prop.staticImagePath;
            prop.webAssetPath = CaffeineEndpoints.ASSETS + prop.webAssetPath;
            prop.sceneKitPath = CaffeineEndpoints.ASSETS + prop.sceneKitPath;

            prop.credits = prop.goldCost * 3;

            return prop;
        } catch (JsonSyntaxException e) {
            throw new ApiException("Could not parse CaffeineProp: " + element.toString(), e);
        }
    }

    @Getter
    @ToString
    public static class CaffeineProp {
        private String id;
        private String name;
        private int credits;

        @SerializedName("gold_cost")
        private int goldCost;

        @SerializedName("plural_name")
        private String pluralName;

        @SerializedName("universal_video_prop_path")
        private String universalVideoPropPath;

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
