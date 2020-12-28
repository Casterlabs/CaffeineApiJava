package co.casterlabs.caffeineapi.requests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import co.casterlabs.apiutil.ApiUtil;
import co.casterlabs.apiutil.auth.ApiAuthException;
import co.casterlabs.apiutil.web.ApiException;
import co.casterlabs.apiutil.web.AuthenticatedWebRequest;
import co.casterlabs.caffeineapi.CaffeineApi;
import co.casterlabs.caffeineapi.CaffeineAuth;
import co.casterlabs.caffeineapi.CaffeineEndpoints;
import co.casterlabs.caffeineapi.HttpUtil;
import okhttp3.Response;

public class CaffeinePropsListRequest extends AuthenticatedWebRequest<List<CaffeineProp>, CaffeineAuth> {

    public CaffeinePropsListRequest(CaffeineAuth auth) {
        super(auth);
    }

    @Override
    protected List<CaffeineProp> execute() throws ApiException, ApiAuthException, IOException {
        Response response = HttpUtil.sendHttp("{}", CaffeineEndpoints.PROPS_LIST, this.auth, "application/json"); // Send empty json data, because it's required for some reason.
        String body = response.body().string();

        response.close();

        try {
            if (response.code() == 401) {
                throw new ApiAuthException("Auth is invalid: " + body);
            } else {
                JsonObject json = CaffeineApi.GSON.fromJson(body, JsonObject.class);
                JsonObject payload = json.getAsJsonObject("payload");
                JsonObject digitalItems = payload.getAsJsonObject("digital_items");
                JsonArray state = digitalItems.getAsJsonArray("state");

                System.out.println(json);

                List<CaffeineProp> list = new ArrayList<>();

                for (JsonElement element : state) {
                    list.add(CaffeineProp.fromJson(element));
                }

                return list;
            }
        } catch (Exception e) {
            ApiUtil.getErrorReporter().apiError(CaffeineEndpoints.PROPS_LIST, null, this.auth.getAuthHeaders(), body, response.headers().toMultimap(), e);
            throw e;
        }
    }

}
