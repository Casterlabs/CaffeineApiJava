package com.github.caffeineapi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import lombok.NonNull;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {

    public static Response sendHttpGet(@NonNull String address, @Nullable Map<String, String> headers) throws IOException {
        return sendHttp(null, null, address, headers, null);
    }

    public static Response sendHttp(@NonNull String body, @NonNull String address, @Nullable Map<String, String> headers, @Nullable String mime) throws IOException {
        return sendHttp(RequestBody.create(body.getBytes(StandardCharsets.UTF_8)), "POST", address, headers, mime);
    }

    public static Response sendHttp(@Nullable RequestBody body, @Nullable String type, @NonNull String address, @Nullable Map<String, String> headers, @Nullable String mime) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().proxy(CaffeineApi.getProxy()).build();
        Request.Builder builder = new Request.Builder().url(address);

        if ((body != null) && (type != null)) {
            switch (type.toUpperCase()) {
                case "POST": {
                    builder.post(body);
                    break;
                }

                case "PUT": {
                    builder.put(body);
                    break;
                }

                case "PATCH": {
                    builder.patch(body);
                    break;
                }
            }
        }

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        if (mime != null) {
            builder.addHeader("Content-Type", mime);
        }

        Request request = builder.build();
        Response response = client.newCall(request).execute();

        return response;
    }

}
