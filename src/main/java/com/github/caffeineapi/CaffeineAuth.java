package com.github.caffeineapi;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import okhttp3.Response;

@Getter
public class CaffeineAuth {
    private static final long REFRESH_TIME = TimeUnit.MINUTES.toMillis(10);

    private CaffeineAuthResponse authResponse;
    private String refreshToken;
    private long loginTimestamp;
    private String accessToken;
    private String signedToken;
    private String credential;
    private String caid;

    @SneakyThrows
    public final CaffeineAuthResponse loginBlocking(@NonNull String username, @NonNull String password, @Nullable String twoFactor) {
        return this.login(username, password, twoFactor).get();
    }

    @SneakyThrows
    public final CaffeineAuthResponse loginBlocking(@NonNull String refreshToken) {
        return this.login(refreshToken).get();
    }

    public CompletableFuture<CaffeineAuthResponse> login(@NonNull String username, @NonNull String password, @Nullable String twoFactor) {
        JsonObject request = new JsonObject();
        JsonObject account = new JsonObject();

        account.addProperty("username", username);
        account.addProperty("password", password);

        request.add("account", account);

        if (twoFactor != null) {
            JsonObject mfa = new JsonObject();

            request.addProperty("opt", twoFactor);

            request.add("mfa", mfa);
        }

        return sendAuth(CaffeineEndpoints.SIGNIN, request);
    }

    public CompletableFuture<CaffeineAuthResponse> login(@NonNull String refreshToken) {
        JsonObject request = new JsonObject();

        request.addProperty("refresh_token", refreshToken);

        return sendAuth(CaffeineEndpoints.TOKEN, request);
    }

    private CompletableFuture<CaffeineAuthResponse> sendAuth(String url, JsonObject requestBody) {
        CompletableFuture<CaffeineAuthResponse> future = new CompletableFuture<>();

        ThreadHelper.executeAsync(() -> {
            try {
                Response authResponse = HttpUtil.sendHttp(requestBody.toString(), url, null, "application/json");

                if (authResponse.code() == 401) {
                    this.authResponse = CaffeineAuthResponse.INVALID;
                } else {
                    String body = authResponse.body().string();
                    JsonObject json = CaffeineApi.GSON.fromJson(body, JsonObject.class);

                    if (json.has("next")) {
                        this.authResponse = CaffeineAuthResponse.AWAIT2FA;
                    } else if (json.has("credentials")) {
                        JsonObject credentials = json.getAsJsonObject("credentials");

                        this.refreshToken = credentials.get("refresh_token").getAsString();
                        this.accessToken = credentials.get("access_token").getAsString();
                        this.caid = credentials.get("caid").getAsString();
                        this.credential = credentials.get("credential").getAsString();

                        Response signedResponse = HttpUtil.sendHttpGet(String.format(CaffeineEndpoints.SIGNED, this.caid), Collections.singletonMap("Authorization", "Bearer " + this.accessToken));
                        JsonObject signed = CaffeineApi.GSON.fromJson(signedResponse.body().string(), JsonObject.class);

                        this.signedToken = signed.get("token").getAsString();

                        final long expectedLoginTimestamp = this.loginTimestamp;
                        ThreadHelper.executeLater(REFRESH_TIME, () -> {
                            // Prevent refresh spam.
                            if (this.loginTimestamp == expectedLoginTimestamp) {
                                this.login(this.refreshToken);
                            }
                        });

                        this.authResponse = CaffeineAuthResponse.SUCCESS;
                    } else {
                        this.authResponse = CaffeineAuthResponse.INVALID;
                    }
                }

                future.complete(this.authResponse);
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    public boolean isValid() {
        return this.authResponse == CaffeineAuthResponse.SUCCESS;
    }

    public static enum CaffeineAuthResponse {
        SUCCESS,
        AWAIT2FA,
        INVALID;
    }

}
