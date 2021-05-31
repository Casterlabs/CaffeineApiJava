package co.casterlabs.caffeineapi.requests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import co.casterlabs.apiutil.auth.ApiAuthException;
import co.casterlabs.apiutil.web.ApiException;
import co.casterlabs.apiutil.web.AuthenticatedWebRequest;
import co.casterlabs.caffeineapi.CaffeineApi;
import co.casterlabs.caffeineapi.CaffeineAuth;
import co.casterlabs.caffeineapi.CaffeineEndpoints;
import co.casterlabs.caffeineapi.HttpUtil;
import co.casterlabs.caffeineapi.types.CaffeineFollow;
import co.casterlabs.caffeineapi.types.CaffeineUser;
import lombok.NonNull;
import lombok.Setter;
import okhttp3.Response;

public class CaffeineFollowersListRequest extends AuthenticatedWebRequest<List<CaffeineFollow>, CaffeineAuth> {
    private boolean retrieveAll = false;
    private long offset = 0;
    private String caid;
    private @Setter long limit = 0;

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
    protected List<CaffeineFollow> execute() throws ApiException, ApiAuthException, IOException {
        List<CaffeineFollow> followers = new ArrayList<>();

        do {
            String url = String.format(CaffeineEndpoints.FOLLOWERS, this.caid, this.limit, this.offset);

            try (Response response = HttpUtil.sendHttpGet(url, this.auth)) {
                String body = response.body().string();

                if (response.code() == 401) {
                    throw new ApiAuthException("Auth is invalid: " + body);
                } else if (response.code() == 404) {
                    throw new ApiException("User does not exist: " + body);
                } else {
                    JsonObject json = CaffeineApi.GSON.fromJson(body, JsonObject.class);
                    JsonArray array = json.getAsJsonArray("followers");

                    if (array.size() == 0) {
                        break;
                    }

                    for (JsonElement e : array) {
                        followers.add(CaffeineApi.GSON.fromJson(e, CaffeineFollow.class));
                    }
                }

                this.offset += 100;
            }
        } while (this.retrieveAll);

        return followers;
    }

    public void setRetrieveAll(boolean retrieveAll) {
        this.retrieveAll = retrieveAll;
    }

}
