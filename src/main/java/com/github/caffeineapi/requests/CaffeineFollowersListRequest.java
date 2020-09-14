package com.github.caffeineapi.requests;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.caffeineapi.CaffeineApi;
import com.github.caffeineapi.CaffeineAuth;
import com.github.caffeineapi.CaffeineEndpoints;
import com.github.caffeineapi.HttpUtil;
import com.github.caffeineapi.exception.CaffeineAuthenticationException;
import com.github.caffeineapi.requests.CaffeineFollowersListRequest.CaffeineFollowersResponse;
import com.github.caffeineapi.requests.CaffeineUserInfoRequest.CaffeineUser;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import okhttp3.Response;

public class CaffeineFollowersListRequest extends AuthenticatedWebRequest<CaffeineFollowersResponse> {
    private String caid;
    private long offset = 0;

    public CaffeineFollowersListRequest(CaffeineAuth auth) {
        super(auth);
    }

    public CaffeineFollowersListRequest setOffset(long offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset cannot be negative.");
        } else {
            this.offset = offset;
            return this;
        }
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
    public CaffeineFollowersResponse send() throws Exception {
        Map<String, String> headers = Collections.singletonMap("Authorization", "Bearer " + this.getAuth().getAccessToken());
        Response response = HttpUtil.sendHttpGet(String.format(CaffeineEndpoints.FOLLOWERS, this.caid, this.offset), headers);
        String body = response.body().string();

        if (response.code() == 404) {
            throw new CaffeineAuthenticationException("User does not exist", body);
        } else {
            return CaffeineApi.GSON.fromJson(body, CaffeineFollowersResponse.class);
        }
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

    @Getter
    @ToString
    public static class CaffeineFollowersResponse {
        private long offset;
        private List<CaffeineFollower> followers;

    }

}
