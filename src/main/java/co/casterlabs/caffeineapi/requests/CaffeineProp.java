package co.casterlabs.caffeineapi.requests;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import co.casterlabs.apiutil.web.ApiException;
import co.casterlabs.caffeineapi.CaffeineApi;
import co.casterlabs.caffeineapi.CaffeineEndpoints;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CaffeineProp {
    private String id;

    private String name;

    @SerializedName("credits_per_item")
    private int credits = -1;

    @SerializedName("gold_cost")
    private int goldCost = -1;

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

    public static CaffeineProp fromJson(JsonElement element) throws ApiException {
        try {
            CaffeineProp prop = CaffeineApi.GSON.fromJson(element, CaffeineProp.class);

            prop.universalVideoPropPath = CaffeineEndpoints.ASSETS + prop.universalVideoPropPath;
            prop.previewImagePath = CaffeineEndpoints.ASSETS + prop.previewImagePath;
            prop.staticImagePath = CaffeineEndpoints.ASSETS + prop.staticImagePath;
            prop.webAssetPath = CaffeineEndpoints.ASSETS + prop.webAssetPath;
            prop.sceneKitPath = CaffeineEndpoints.ASSETS + prop.sceneKitPath;

            // The format from messages is different from the proplist api, so we detect that and adjust to it.
            if (prop.goldCost == -1) {
                prop.goldCost = prop.credits / 3;
            } else {
                prop.credits = prop.goldCost * 3;
            }

            return prop;
        } catch (JsonSyntaxException e) {
            throw new ApiException("Could not parse CaffeineProp: " + element.toString(), e);
        }
    }

}
