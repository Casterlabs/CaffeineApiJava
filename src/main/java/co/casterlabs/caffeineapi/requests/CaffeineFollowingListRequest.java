package co.casterlabs.caffeineapi.requests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.casterlabs.apiutil.auth.ApiAuthException;
import co.casterlabs.apiutil.web.ApiException;
import co.casterlabs.apiutil.web.AuthenticatedWebRequest;
import co.casterlabs.caffeineapi.CaffeineApi;
import co.casterlabs.caffeineapi.CaffeineAuth;
import co.casterlabs.caffeineapi.CaffeineEndpoints;
import co.casterlabs.caffeineapi.HttpUtil;
import co.casterlabs.caffeineapi.types.CaffeineFollow;
import co.casterlabs.caffeineapi.types.CaffeineUser;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.NonNull;
import lombok.Setter;
import okhttp3.Response;

public class CaffeineFollowingListRequest extends AuthenticatedWebRequest<List<CaffeineFollow>, CaffeineAuth> {
    private boolean retrieveAll = false;
    private String caid;
    private long offset = 0;
    private @Setter long limit = 0;

    public CaffeineFollowingListRequest(CaffeineAuth auth) {
        super(auth);
    }

    public CaffeineFollowingListRequest setOffset(long offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset cannot be negative.");
        } else {
            this.offset = offset;
            return this;
        }
    }

    public CaffeineFollowingListRequest setUser(@NonNull CaffeineUser user) {
        this.caid = user.getCAID();
        return this;
    }

    public CaffeineFollowingListRequest setCAID(@NonNull String caid) {
        this.caid = caid;
        return this;
    }

    @Override
    protected List<CaffeineFollow> execute() throws ApiException, ApiAuthException, IOException {
        List<CaffeineFollow> following = new ArrayList<>();

        do {
            String url = String.format(CaffeineEndpoints.FOLLOWING, this.caid, this.limit, this.offset);

            try (Response response = HttpUtil.sendHttpGet(url, this.auth)) {
                String body = response.body().string();

                if (response.code() == 401) {
                    throw new ApiAuthException("Auth is invalid: " + body);
                } else if (response.code() == 404) {
                    throw new ApiException("User does not exist: " + body);
                } else {
                    JsonObject json = CaffeineApi.RSON.fromJson(body, JsonObject.class);
                    JsonArray array = json.getArray("followers");

                    if (array.size() == 0) {
                        break;
                    }

                    for (JsonElement e : array) {
                        following.add(CaffeineApi.RSON.fromJson(e, CaffeineFollow.class));
                    }
                }

                this.offset += 100;
            } catch (JsonParseException e) {
                throw new ApiException(e);
            }
        } while (this.retrieveAll);

        return following;
    }

}
