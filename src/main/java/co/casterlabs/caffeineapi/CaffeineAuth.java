package co.casterlabs.caffeineapi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import co.casterlabs.apiutil.auth.ApiAuthException;
import co.casterlabs.apiutil.auth.AuthProvider;
import lombok.Getter;
import lombok.NonNull;
import okhttp3.Request.Builder;
import okhttp3.Response;

@Getter
public class CaffeineAuth implements AuthProvider, AutoCloseable {
    private static final long REFRESH_TIME = TimeUnit.MINUTES.toMillis(10);

    private static String anonymousCredential;

    private CaffeineAuthResponse authResponse;
    private String refreshToken;
    private long loginTimestamp;
    private @Getter String accessToken;
    private @Getter String signedToken;
    private @Getter String credential;
    private String caid;

    static {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    try (Response response = HttpUtil.sendHttpGet("https://api.caffeine.tv/v1/credentials/anonymous", null)) {
                        JsonObject json = CaffeineApi.GSON.fromJson(response.body().string(), JsonObject.class);

                        anonymousCredential = json.get("credential").getAsString();
                    }

                    Thread.sleep(REFRESH_TIME);
                }
            } catch (JsonSyntaxException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        t.setName("Caffeine Anonymous Credential Refresh");
        t.setDaemon(true);
        t.start();
    }

    public CaffeineAuthResponse login(@NonNull String username, @NonNull String password, @Nullable String twoFactor) throws ApiAuthException {
        JsonObject request = new JsonObject();
        JsonObject account = new JsonObject();

        account.addProperty("username", username);
        account.addProperty("password", password);

        request.add("account", account);

        if (twoFactor != null) {
            JsonObject mfa = new JsonObject();

            mfa.addProperty("opt", twoFactor);

            request.add("mfa", mfa);
        }

        return sendAuth(CaffeineEndpoints.SIGNIN, request);
    }

    public CaffeineAuthResponse login(@NonNull String refreshToken) throws ApiAuthException {
        JsonObject request = new JsonObject();

        request.addProperty("refresh_token", refreshToken);

        return sendAuth(CaffeineEndpoints.TOKEN, request);
    }

    private CaffeineAuthResponse sendAuth(String url, JsonObject payload) throws ApiAuthException {
        try {
            Response authResponse = HttpUtil.sendHttp(payload.toString(), url, null, "application/json");

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

                    Response signedResponse = HttpUtil.sendHttpGet(String.format(CaffeineEndpoints.SIGNED, this.caid), this);
                    JsonObject signed = CaffeineApi.GSON.fromJson(signedResponse.body().string(), JsonObject.class);

                    this.signedToken = signed.get("token").getAsString();

                    final long expectedLoginTimestamp = this.loginTimestamp;
                    ThreadHelper.executeAsyncLater(
                        "Auth Refresh", () -> {
                            // Prevent refresh spam.
                            if (this.loginTimestamp == expectedLoginTimestamp) {
                                try {
                                    this.login(this.refreshToken);
                                } catch (ApiAuthException ignored) {}
                            }
                        }, REFRESH_TIME
                    );

                    this.authResponse = CaffeineAuthResponse.SUCCESS;
                } else {
                    this.authResponse = CaffeineAuthResponse.INVALID;
                }
            }

            return this.authResponse;
        } catch (IOException e) {
            throw new ApiAuthException(e);
        }
    }

    public boolean isValid() {
        return this.authResponse == CaffeineAuthResponse.SUCCESS;
    }

    public static enum CaffeineAuthResponse {
        SUCCESS,
        AWAIT2FA,
        INVALID;
    }

    @Override
    public void authenticateRequest(@NonNull Builder request) {
        request.addHeader("Authorization", "Bearer " + this.accessToken);
        request.addHeader("x-credential", this.credential);
    }

    @Override
    public void refresh() throws ApiAuthException {
        this.login(this.refreshToken);
    }

    public @NonNull Map<String, String> getAuthHeaders() {
        Map<String, String> headers = new HashMap<>();

        headers.put("Authorization", "Bearer " + this.accessToken);
        headers.put("x-credential", this.credential);

        return headers;
    }

    public static String getAnonymousCredential() {
        while (anonymousCredential == null) { // A (bad) way to wait for the internal thread to init
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {}
        }

        return anonymousCredential;
    }

    @Override
    public void close() throws Exception {
        this.loginTimestamp = -1;
        this.refreshToken = null;
    }

}
