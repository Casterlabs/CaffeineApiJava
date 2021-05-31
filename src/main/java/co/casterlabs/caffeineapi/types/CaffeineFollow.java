package co.casterlabs.caffeineapi.types;

import java.time.Instant;

import com.google.gson.annotations.SerializedName;

import co.casterlabs.caffeineapi.requests.CaffeineUserInfoRequest;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CaffeineFollow {
    @SerializedName("caid")
    private String CAID;

    @SerializedName("followed_at")
    private Instant followedAt;

    public CaffeineUserInfoRequest getAsUser() {
        return new CaffeineUserInfoRequest()
            .setCAID(this.CAID);
    }

}