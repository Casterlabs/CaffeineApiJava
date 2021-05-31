package co.casterlabs.caffeineapi.types;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import co.casterlabs.caffeineapi.CaffeineEndpoints;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CaffeineGame {
    private long id;
    private String name;
    private String description;
    private String website;
    private boolean supported;

    @SerializedName("icon_image_path")
    private String iconImagePath;

    @SerializedName("banner_image_path")
    private String bannerImagePath;

    @SerializedName("process_names")
    private List<String> processNames;

    @SerializedName("executable_name")
    private String excecutableName;

    @SerializedName("window_title")
    private String windowTitle;

    @SerializedName("is_capture_software")
    private boolean isCaptureSoftware;

    public String getIconImagePath() {
        return CaffeineEndpoints.IMAGES + this.iconImagePath;
    }

    public String getBannerImagePath() {
        return CaffeineEndpoints.IMAGES + this.bannerImagePath;
    }

}
