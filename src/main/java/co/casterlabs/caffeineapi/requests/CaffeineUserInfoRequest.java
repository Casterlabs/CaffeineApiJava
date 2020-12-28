package co.casterlabs.caffeineapi.requests;

import java.io.IOException;

import com.google.gson.JsonObject;

import co.casterlabs.apiutil.ApiUtil;
import co.casterlabs.apiutil.web.ApiException;
import co.casterlabs.apiutil.web.WebRequest;
import co.casterlabs.caffeineapi.CaffeineApi;
import co.casterlabs.caffeineapi.CaffeineEndpoints;
import co.casterlabs.caffeineapi.HttpUtil;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import okhttp3.Response;

@Setter
@Accessors(chain = true)
public class CaffeineUserInfoRequest extends WebRequest<CaffeineUser> {
    private @NonNull String query;

    public CaffeineUserInfoRequest setUsername(@NonNull String username) {
        this.query = username;
        return this;
    }

    public CaffeineUserInfoRequest setUser(@NonNull CaffeineUser user) {
        this.query = user.getCAID();
        return this;
    }

    public CaffeineUserInfoRequest setCAID(@NonNull String caid) {
        this.query = caid;
        return this;
    }

    @Override
    protected CaffeineUser execute() throws ApiException, IOException {
        String url = String.format(CaffeineEndpoints.USERS, this.query);
        Response response = HttpUtil.sendHttpGet(url, null);
        String body = response.body().string();

        response.close();

        if (response.code() == 404) {
            throw new ApiException("User does not exist: " + body);
        } else {
            try {
                JsonObject json = CaffeineApi.GSON.fromJson(body, JsonObject.class);
                JsonObject user = json.getAsJsonObject("user");

                return CaffeineUser.fromJson(user);
            } catch (Exception e) {
                ApiUtil.getErrorReporter().apiError(url, null, null, body, response.headers().toMultimap(), e);
                throw e;
            }
        }
    }

}
