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
import com.github.caffeineapi.requests.CaffeineFollowingListRequest.CaffeineFollowingResponse;
import com.github.caffeineapi.requests.CaffeineUserInfoRequest.CaffeineUser;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import okhttp3.Response;

public class CaffeineFollowingListRequest extends AuthenticatedWebRequest<CaffeineFollowingResponse> {
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
    public CaffeineFollowingResponse send() throws Exception {
        Map<String, String> headers = Collections.singletonMap("Authorization", "Bearer " + this.getAuth().getAccessToken());
        Response response = HttpUtil.sendHttpGet(String.format(CaffeineEndpoints.FOLLOWING, this.caid, this.offset), headers);
        String body = response.body().string();

        if (response.code() == 404) {
            throw new CaffeineAuthenticationException("User does not exist", body);
        } else {
            return CaffeineApi.GSON.fromJson(body, CaffeineFollowingResponse.class);
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
