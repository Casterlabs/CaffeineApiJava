package co.casterlabs.caffeineapi.requests;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import co.casterlabs.apiutil.ApiUtil;
import co.casterlabs.apiutil.auth.ApiAuthException;
import co.casterlabs.apiutil.web.ApiException;
import co.casterlabs.apiutil.web.AuthenticatedWebRequest;
import co.casterlabs.caffeineapi.CaffeineApi;
import co.casterlabs.caffeineapi.CaffeineAuth;
import co.casterlabs.caffeineapi.CaffeineEndpoints;
import co.casterlabs.caffeineapi.HttpUtil;
import co.casterlabs.caffeineapi.requests.CaffeineFollowingListRequest.CaffeineFollowingResponse;
import co.casterlabs.caffeineapi.requests.CaffeineUserInfoRequest.CaffeineUser;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import okhttp3.Response;

public class CaffeineFollowingListRequest extends AuthenticatedWebRequest<CaffeineFollowingResponse, CaffeineAuth> {
    private String caid;
    private long offset = 0;

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
    protected CaffeineFollowingResponse execute() throws ApiException, ApiAuthException, IOException {
        String url = String.format(CaffeineEndpoints.FOLLOWING, this.caid, this.offset);
        Response response = HttpUtil.sendHttpGet(url, this.auth);
        String body = response.body().string();

        response.close();

        if (response.code() == 401) {
            throw new ApiAuthException("Auth is invalid: " + body);
        } else if (response.code() == 404) {
            throw new ApiException("User does not exist: " + body);
        } else {
            try {
                return CaffeineApi.GSON.fromJson(body, CaffeineFollowingResponse.class);
            } catch (Exception e) {
                ApiUtil.getErrorReporter().apiError(url, null, this.auth.getAuthHeaders(), body, response.headers().toMultimap(), e);
                throw e;
            }
        }
    }

    @Getter
    @ToString
    public static class CaffeineFollow {
        @SerializedName("caid")
        private String CAID;
        @SerializedName("followed_at")
        private Instant followedAt;

        @SneakyThrows
        public CaffeineUser getAsUser() {
            return new CaffeineUserInfoRequest().setCAID(this.CAID).send();
        }

    }

    @Getter
    @ToString
    public static class CaffeineFollowingResponse {
        private long offset;
        private List<CaffeineFollow> following;

    }

}
