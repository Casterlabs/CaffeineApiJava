package co.casterlabs.caffeineapi.realtime.messages;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Nullable;

import co.casterlabs.apiutil.web.ApiException;
import co.casterlabs.caffeineapi.CaffeineApi;
import co.casterlabs.caffeineapi.CaffeineAuth;
import co.casterlabs.caffeineapi.CaffeineEndpoints;
import co.casterlabs.caffeineapi.realtime.CaffeineRealtimeHelper;
import co.casterlabs.caffeineapi.types.CaffeineProp;
import co.casterlabs.caffeineapi.types.CaffeineUser;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Setter;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class CaffeineMessages implements Closeable {
    private static final long CAFFEINE_KEEPALIVE = TimeUnit.SECONDS.toMillis(15);

    private @Setter @Nullable CaffeineMessagesListener listener;
    private @Setter @Nullable CaffeineAuth auth;

    private Connection conn;

    public CaffeineMessages(CaffeineUser user) {
        this(user.getStageID());
    }

    public CaffeineMessages(String caidOrStage) {
        try {
            this.conn = new Connection(caidOrStage.replace("CAID", ""));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        if (!this.isOpen()) {
            if (this.conn.getReadyState() == ReadyState.NOT_YET_CONNECTED) {
                this.conn.connect();
            } else {
                this.conn.reconnect();
            }
        }
    }

    public void connectBlocking() throws InterruptedException {
        if (!this.isOpen()) {
            if (this.conn.getReadyState() == ReadyState.NOT_YET_CONNECTED) {
                this.conn.connectBlocking();
            } else {
                this.conn.reconnectBlocking();
            }
        }
    }

    public boolean isOpen() {
        return this.conn.isOpen();
    }

    public void disconnect() {
        if (this.isOpen()) {
            this.conn.close();
        }
    }

    public void disconnectBlocking() throws InterruptedException {
        if (this.isOpen()) {
            this.conn.closeBlocking();
        }
    }

    private class Connection extends WebSocketClient {
        private final FastLogger logger = new FastLogger("CaffeineMessages");

        public Connection(String stageId) throws URISyntaxException {
            super(new URI(String.format(CaffeineEndpoints.CHAT, stageId)));
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            JsonObject loginPayload = new JsonObject();
            JsonObject loginHeaders = new JsonObject();

            loginHeaders
                // .put("X-Caffeine-Vendor-Identifier", "d3370eb6-2e83-4355-b31b-eef0a6813a15")
                // .put("X-Client-Version", "1.2.4")
                .put("X-Client-Type", "web");

            if (auth == null) {
                loginHeaders
                    .put("Authorization", "Anonymous ANON-Fish")
                    .put("X-Credential", CaffeineAuth.getAnonymousCredential());
            } else {
                loginHeaders
                    .put("Authorization", "Bearer " + auth.getAccessToken())
                    .put("X-Credential", auth.getCredential());

                loginPayload.put(
                    "Body",
                    new JsonObject()
                        .put("user", auth.getSignedToken())
                        .toString()
                );
            }

            this.send(
                loginPayload
                    .put("Headers", loginHeaders)
                    .toString()
            );

            Thread t = new Thread(() -> {
                while (this.isOpen()) {
                    try {
                        this.send("\"HEALZ\"");
                        Thread.sleep(CAFFEINE_KEEPALIVE);
                    } catch (Exception ignored) {}
                }
            });

            t.setName("CaffeineMessages KeepAlive");
            t.start();

            if (listener != null) {
                listener.onOpen();
            }
        }

        @SneakyThrows
        @Override
        public void onMessage(String raw) {
            this.logger.log(LogLevel.TRACE, String.format(CaffeineRealtimeHelper.DEBUG_WS_RECIEVE, raw));

            try {
                if (!raw.equals("\"THANKS\"") && (listener != null)) {
                    JsonObject json = CaffeineApi.RSON.fromJson(raw, JsonObject.class);

                    if (!json.containsKey("Compatibility-Mode") && json.containsKey("type")) {
                        CaffeineAlertType type = CaffeineAlertType.fromJson(json.get("type"));

                        if (type != CaffeineAlertType.UNKNOWN) {
                            CaffeineUser sender = CaffeineUser.fromJson(json.getObject("publisher"));
                            JsonObject body = json.getObject("body");
                            String id = getMessageId(json.get("id").getAsString());

                            switch (type) {
                                case REACTION:
                                    ChatEvent chatEvent = new ChatEvent(sender, body.get("text").getAsString(), id);

                                    if (json.containsKey("endorsement_count")) {
                                        listener.onUpvote((new UpvoteEvent(chatEvent, json.getNumber("endorsement_count").intValue())));
                                    } else {
                                        listener.onChat(chatEvent);
                                    }

                                    return;

                                case SHARE:
                                    ShareEvent shareEvent = new ShareEvent(sender, body.get("text").getAsString(), id);

                                    if (json.containsKey("endorsement_count")) {
                                        listener.onUpvote((new UpvoteEvent(shareEvent, json.getNumber("endorsement_count").intValue())));
                                    } else {
                                        listener.onShare(shareEvent);
                                    }

                                    return;

                                case DIGITAL_ITEM:
                                    JsonObject propJson = body.getObject("digital_item");
                                    CaffeineProp prop = CaffeineProp.fromJson(propJson);
                                    PropEvent propEvent = new PropEvent(sender, body.get("text").getAsString(), id, propJson.getNumber("count").intValue(), prop);

                                    if (json.containsKey("endorsement_count")) {
                                        listener.onUpvote((new UpvoteEvent(propEvent, json.getNumber("endorsement_count").intValue())));
                                    } else {
                                        listener.onProp(propEvent);
                                    }

                                    return;

                                case FOLLOW:
                                    listener.onFollow(new FollowEvent(sender));

                                    return;

                                case UNKNOWN: // hush mr compiley
                                    return;
                            }
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

        @Override
        public void send(String payload) {
            payload = payload.replace("\n", "");

            this.logger.log(LogLevel.TRACE, String.format(CaffeineRealtimeHelper.DEBUG_WS_SEND, payload));
            super.send(payload);
        }

    }

    @SneakyThrows
    private static String getMessageId(String b64) {
        byte[] bytes = Base64.getDecoder().decode(b64);
        JsonObject json = CaffeineApi.RSON.fromJson(new String(bytes), JsonObject.class);

        return json.get("u").getAsString();
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
