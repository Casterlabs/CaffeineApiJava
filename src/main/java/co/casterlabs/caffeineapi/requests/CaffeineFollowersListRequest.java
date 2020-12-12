package co.casterlabs.caffeineapi.requests;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import co.casterlabs.apiutil.ApiUtil;
import co.casterlabs.apiutil.auth.ApiAuthException;
import co.casterlabs.apiutil.web.ApiException;
import co.casterlabs.apiutil.web.AuthenticatedWebRequest;
import co.casterlabs.caffeineapi.CaffeineApi;
import co.casterlabs.caffeineapi.CaffeineAuth;
import co.casterlabs.caffeineapi.CaffeineEndpoints;
import co.casterlabs.caffeineapi.HttpUtil;
import co.casterlabs.caffeineapi.requests.CaffeineFollowersListRequest.CaffeineFollower;
import co.casterlabs.caffeineapi.requests.CaffeineUserInfoRequest.CaffeineUser;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import okhttp3.Response;

public class CaffeineFollowersListRequest extends AuthenticatedWebRequest<List<CaffeineFollower>, CaffeineAuth> {
    private boolean retrieveAll = false;
    private long offset = 0;
    private String caid;

    public CaffeineFollowersListRequest(CaffeineAuth auth) {
        super(auth);
    }

    public CaffeineFollowersListRequest setUser(String username) throws ApiAuthException, ApiException {
        CaffeineUserInfoRequest request = new CaffeineUserInfoRequest();

        request.setQuery(username);

        return this.setUser(request.send());
    }

    public CaffeineFollowersListRequest setUser(@NonNull CaffeineUser user) {
        this.caid = user.getCAID();
        return this;
    }

    public CaffeineFollowersListRequest setCAID(@NonNull String caid) {
        this.caid = caid;
        return this;
    }

    @Override
    protected List<CaffeineFollower> execute() throws ApiException, ApiAuthException, IOException {
        List<CaffeineFollower> followers = new ArrayList<>();

        do {
            String url = String.format(CaffeineEndpoints.FOLLOWERS, this.caid, this.offset);
            Response response = HttpUtil.sendHttpGet(url, this.auth);
            String body = response.body().string();

            response.close();

            if (response.code() == 401) {
                throw new ApiAuthException("Auth is invalid: " + body);
            } else if (response.code() == 404) {
                throw new ApiException("User does not exist: " + body);
            } else {
                try {
                    JsonObject json = CaffeineApi.GSON.fromJson(body, JsonObject.class);
                    JsonArray array = json.getAsJsonArray("followers");

                    if (array.size() == 0) {
                        break;
                    }

                    for (JsonElement e : array) {
                        followers.add(CaffeineApi.GSON.fromJson(e, CaffeineFollower.class));
                    }
                } catch (Exception e) {
                    ApiUtil.getErrorReporter().apiError(url, null, this.auth.getAuthHeaders(), body, response.headers().toMultimap(), e);
                    throw e;
                }
            }

            this.offset += 100;
        } while (this.retrieveAll);

        return followers;
    }

    public void setRetrieveAll(boolean retrieveAll) {
        this.retrieveAll = retrieveAll;
    }

    @Getter
    @ToString
    public static class CaffeineFollower {
        @SerializedName("caid")
        private String CAID;

        @SerializedName("followed_at")
        private Instant followedAt;

        @SneakyThrows
        public CaffeineUser getAsUser() {
            CaffeineUserInfoRequest request = new CaffeineUserInfoRequest();

            request.setCAID(this.CAID);

            return request.send();
        }

    }

}
