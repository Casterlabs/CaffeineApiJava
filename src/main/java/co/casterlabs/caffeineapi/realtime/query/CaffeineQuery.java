package co.casterlabs.caffeineapi.realtime.query;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.protocols.Protocol;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import co.casterlabs.apiutil.web.ApiException;
import co.casterlabs.caffeineapi.CaffeineApi;
import co.casterlabs.caffeineapi.CaffeineAuth;
import co.casterlabs.caffeineapi.CaffeineEndpoints;
import co.casterlabs.caffeineapi.types.CaffeineUser;
import lombok.Setter;

public class CaffeineQuery implements Closeable {
    private static final Draft_6455 DRAFT = new Draft_6455(Collections.emptyList(), Collections.singletonList(new Protocol("graphql-ws")));
    private static final String query = "{\"id\":\"1\",\"type\":\"start\",\"payload\":{\"variables\":{\"clientId\":\"Anonymous\",\"clientType\":\"WEB\",\"constrainedBaseline\":false,\"username\":\"%USERNAME%\",\"viewerStreams\":[]},\"extensions\":{},\"operationName\":\"Stage\",\"query\":\"subscription Stage($clientId: ID!, $clientType: ClientType!, $constrainedBaseline: Boolean, $username: String!, $viewerStreams: [StageSubscriptionViewerStreamInput!]) {\\n  stage(clientId: $clientId, clientType: $clientType, clientTypeForMetrics: \\\"WEB\\\", constrainedBaseline: $constrainedBaseline, username: $username, viewerStreams: $viewerStreams) {\\n    error {\\n      __typename\\n      title\\n      message\\n    }\\n    stage {\\n      id\\n      username\\n      title\\n      broadcastId\\n      contentRating\\n      live\\n      feeds {\\n        id\\n        clientId\\n        clientType\\n        gameId\\n        liveHost {\\n          __typename\\n          ... on LiveHostable {\\n            address\\n          }\\n          ... on LiveHosting {\\n            address\\n            volume\\n            ownerId\\n            ownerUsername\\n          }\\n        }\\n        sourceConnectionQuality\\n        capabilities\\n        role\\n        restrictions\\n        stream {\\n          __typename\\n          ... on BroadcasterStream {\\n            id\\n            sdpAnswer\\n            url\\n          }\\n          ... on ViewerStream {\\n            id\\n            sdpOffer\\n            url\\n          }\\n        }\\n      }\\n    }\\n  }\\n}\\n\"}}";
    private static final String auth = "{\"type\":\"connection_init\",\"payload\":{\"X-Credential\":\"%CREDENTIAL%\"}}";

    private @Setter @Nullable CaffeineQueryListener listener;

    private final String username;
    private Connection conn;

    public CaffeineQuery(CaffeineUser user) {
        this(user.getUsername());
    }

    public CaffeineQuery(String username) {
        this.username = username;

        try {
            this.conn = new Connection();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        if (this.conn.getReadyState() == ReadyState.NOT_YET_CONNECTED) {
            this.conn.connect();
        } else {
            this.conn.reconnect();
        }
    }

    public void connectBlocking() throws InterruptedException {
        if (this.conn.getReadyState() == ReadyState.NOT_YET_CONNECTED) {
            this.conn.connectBlocking();
        } else {
            this.conn.reconnectBlocking();
        }
    }

    public void disconnect() {
        this.conn.close();
    }

    public void disconnectBlocking() throws InterruptedException {
        this.conn.closeBlocking();
    }

    private class Connection extends WebSocketClient {

        public Connection() throws URISyntaxException {
            super(new URI(CaffeineEndpoints.QUERY), DRAFT);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            this.send(auth.replace("%CREDENTIAL%", CaffeineAuth.getAnonymousCredential()));
            this.send(query.replace("%USERNAME%", username));

            if (listener != null) {
                listener.onOpen();
            }
        }

        @Override
        public void onMessage(String raw) {
            try {
                if (listener != null) {
                    JsonObject message = CaffeineApi.GSON.fromJson(raw, JsonObject.class);

                    if (message.get("type").getAsString().equalsIgnoreCase("data")) {
                        JsonObject payload = message.getAsJsonObject("payload");

                        if (payload.get("data").isJsonNull()) {
                            throw new ApiException(payload.getAsJsonArray("errors").toString());
                        } else {
                            JsonObject data = payload.getAsJsonObject("data");
                            JsonObject stageContainer = data.getAsJsonObject("stage");
                            JsonObject stage = stageContainer.getAsJsonObject("stage");

                            boolean isLive = stage.get("live").getAsBoolean();
                            String title = stage.get("title").getAsString();

                            listener.onStreamStateChanged(isLive, title);
                        }
                    }
                }
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            if (listener != null) {
                listener.onClose(remote);
            }
        }

        @Override
        public void onError(Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        try {
            this.disconnectBlocking();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

}
