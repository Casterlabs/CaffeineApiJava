package co.casterlabs.caffeineapi.realtime.messages;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import co.casterlabs.apiutil.web.ApiException;
import co.casterlabs.caffeineapi.CaffeineApi;
import co.casterlabs.caffeineapi.CaffeineEndpoints;
import co.casterlabs.caffeineapi.requests.CaffeinePropsListRequest;
import co.casterlabs.caffeineapi.requests.CaffeinePropsListRequest.CaffeineProp;
import co.casterlabs.caffeineapi.requests.CaffeineUserInfoRequest;
import co.casterlabs.caffeineapi.requests.CaffeineUserInfoRequest.CaffeineUser;
import lombok.Getter;
import lombok.Setter;

public class CaffeineMessages {
    private static final String LOGIN_HEADER = "{\"Headers\":{\"Authorization\":\"Anonymous CaffeineApiJava\",\"X-Client-Type\":\"api\"}}";
    private static final long CAFFEINE_KEEPALIVE = TimeUnit.SECONDS.toMillis(15);

    private final String stageId;
    private Connection conn;

    private @Setter @Nullable CaffeineMessagesListener listener;
    private @Getter boolean connecting = false;
    private @Getter boolean connected = false;

    public CaffeineMessages(String caidOrStage) {
        this.stageId = caidOrStage.replace("CAID", "");
    }

    public CaffeineMessages(CaffeineUser user) {
        this.stageId = user.getStageID();
    }

    public synchronized void connect() {
        if (!this.connected && !this.connecting) {
            try {
                this.conn = new Connection();

                this.connecting = true;
                this.conn.connect();
            } catch (URISyntaxException ignored) {}
        }
    }

    public synchronized void connectBlocking() throws InterruptedException {
        if (!this.connected && !this.connecting) {
            try {
                this.conn = new Connection();

                this.connecting = true;
                this.conn.connectBlocking();
            } catch (URISyntaxException ignored) {}
        }
    }

    public void disconnect() {
        if (this.connected) {
            this.conn.close();
        }
    }

    public void disconnectBlocking() throws InterruptedException {
        if (this.connected) {
            this.conn.closeBlocking();
        }
    }

    private void keepAlive() {
        Thread t = new Thread(() -> {
            while (this.connected) {
                try {
                    this.conn.send("\"HEALZ\"");
                    Thread.sleep(CAFFEINE_KEEPALIVE);
                } catch (Exception ignored) {}
            }
        });

        t.setName("CaffeineMessages KeepAlive - " + this.stageId);
        t.start();
    }

    private class Connection extends WebSocketClient {

        public Connection() throws URISyntaxException {
            super(new URI(String.format(CaffeineEndpoints.CHAT, stageId)));
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            this.send(LOGIN_HEADER);

            connected = true;
            connecting = false;

            keepAlive();

            if (listener != null) {
                listener.onOpen();
            }
        }

        @Override
        public void onMessage(String raw) {
            try {
                if (!raw.equals("\"THANKS\"") && (listener != null)) {
                    JsonObject json = CaffeineApi.GSON.fromJson(raw, JsonObject.class);

                    if (!json.has("Compatibility-Mode") && json.has("type")) {
                        CaffeineAlertType type = CaffeineAlertType.fromJson(json.get("type"));

                        if (type != CaffeineAlertType.UNKNOWN) {
                            CaffeineUser sender = CaffeineUserInfoRequest.fromJson(json.getAsJsonObject("publisher"));
                            JsonObject body = json.getAsJsonObject("body");
                            String id = getId(json.get("id").getAsString());

                            switch (type) {
                                case REACTION:
                                    ChatEvent chatEvent = new ChatEvent(sender, body.get("text").getAsString(), id);

                                    if (json.has("endorsement_count")) {
                                        listener.onUpvote((new UpvoteEvent(chatEvent, json.get("endorsement_count").getAsInt())));
                                    } else {
                                        listener.onChat(chatEvent);
                                    }

                                    return;

                                case SHARE:
                                    ShareEvent shareEvent = new ShareEvent(sender, body.get("text").getAsString(), id);

                                    if (json.has("endorsement_count")) {
                                        listener.onUpvote((new UpvoteEvent(shareEvent, json.get("endorsement_count").getAsInt())));
                                    } else {
                                        listener.onShare(shareEvent);
                                    }

                                    return;

                                case DIGITAL_ITEM:
                                    CaffeineProp prop = CaffeinePropsListRequest.fromJson(body.getAsJsonObject("digital_item"));
                                    PropEvent propEvent = new PropEvent(sender, body.get("text").getAsString(), id, prop);

                                    if (json.has("endorsement_count")) {
                                        listener.onUpvote((new UpvoteEvent(propEvent, json.get("endorsement_count").getAsInt())));
                                    } else {
                                        listener.onProp(propEvent);
                                    }

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
            connected = false;
            conn = null;

            if (listener != null) {
                listener.onClose();
            }
        }

        @Override
        public void onError(Exception e) {
            e.printStackTrace();
        }

    }

    public static interface CaffeineMessagesListener {

        default void onOpen() {}

        public void onChat(ChatEvent event);

        public void onShare(ShareEvent event);

        public void onProp(PropEvent event);

        public void onUpvote(UpvoteEvent event);

        default void onClose() {}

    }

    private static String getId(String b64) {
        byte[] bytes = Base64.getDecoder().decode(b64);
        JsonObject json = CaffeineApi.GSON.fromJson(new String(bytes), JsonObject.class);

        return json.get("u").getAsString();
    }

}
