package co.casterlabs.caffeineapi.realtime.viewers;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import co.casterlabs.caffeineapi.CaffeineApi;
import co.casterlabs.caffeineapi.CaffeineAuth;
import co.casterlabs.caffeineapi.CaffeineEndpoints;
import co.casterlabs.caffeineapi.requests.CaffeineUser.UserBadge;
import lombok.Setter;

public class CaffeineViewers implements Closeable {
    // Client type web, as for some reason this is the only way to bring in the newer viewers format
    private static final String AUTH_LOGIN_HEADER = "{\"Headers\":{\"Authorization\":\"Bearer %s\",\"X-Client-Type\":\"web\"},\"Body\":\"{\\\"user\\\":\\\"%s\\\"}\"}";
    private static final long CAFFEINE_KEEPALIVE = TimeUnit.SECONDS.toMillis(15);

    //@formatter:off
    private static Viewer anonymousViewer = new Viewer(
                new ViewerDetails(
                    "Anonymous", 
                    "https://images.caffeine.tv/defaults/avatar-001.png", 
                    UserBadge.NONE, 
                    "anonymous"
                ), 
                -1
            );
    //@formatter:on

    private CaffeineAuth auth;
    private Connection conn;

    private @Setter @Nullable CaffeineViewersListener listener;

    public CaffeineViewers(CaffeineAuth auth) {
        this.auth = auth;

        try {
            this.conn = new Connection();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public synchronized void connect() {
        this.conn.reconnect();
    }

    public synchronized void connectBlocking() throws InterruptedException {
        this.conn.reconnectBlocking();
    }

    public void disconnect() {
        this.conn.close();
    }

    public void disconnectBlocking() throws InterruptedException {
        this.conn.closeBlocking();
    }

    private class Connection extends WebSocketClient {
        private Map<String, Viewer> viewers = new HashMap<>();
        private int lastAnonymousCount = 0;

        public Connection() throws URISyntaxException {
            super(new URI(String.format(CaffeineEndpoints.VIEWERS, auth.getCaid().substring(4))));
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            this.send(String.format(AUTH_LOGIN_HEADER, auth.getAccessToken(), auth.getSignedToken()));

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

        @Override
        public void onMessage(String raw) {
            if (!raw.equals("\"THANKS\"") && (listener != null)) {
                JsonObject json = CaffeineApi.GSON.fromJson(raw, JsonObject.class);

                if (!json.has("Compatibility-Mode")) {
                    if (json.get("departed_viewer_caids").isJsonArray()) {
                        JsonArray departed = json.getAsJsonArray("departed_viewer_caids");

                        for (JsonElement e : departed) {
                            Viewer viewer = this.viewers.remove(e.getAsString());

                            if ((viewer != null) && (listener != null)) {
                                listener.onLeave(viewer);
                            }
                        }
                    }

                    if (json.get("new_viewers").isJsonArray()) {
                        JsonArray newViewers = json.getAsJsonArray("new_viewers");

                        for (JsonElement e : newViewers) {
                            JsonObject newViewer = e.getAsJsonObject();
                            JsonObject userDetails = newViewer.getAsJsonObject("user_details");

                            String imageLink = CaffeineEndpoints.IMAGES + userDetails.get("avatar_image_path").getAsString();
                            String username = userDetails.get("username").getAsString();
                            UserBadge badge = UserBadge.from(userDetails.get("badge"));
                            String caid = userDetails.get("caid").getAsString();

                            ViewerDetails details = new ViewerDetails(caid, imageLink, badge, username);
                            Viewer viewer = new Viewer(details, newViewer.get("joined_at").getAsLong());

                            this.viewers.put(caid, viewer);

                            if (listener != null) {
                                listener.onJoin(viewer);
                            }
                        }
                    }

                    if (!json.get("replacement_viewers").isJsonNull()) {
                        // TODO ?
                        System.out.printf("Replacement viewers: \n%s\n", json.get("replacement_viewers"));
                    }

                    if (listener != null) {
                        JsonObject viewerCounts = json.getAsJsonObject("viewer_counts");
                        int anonymousCount = viewerCounts.get("anonymous").getAsInt();

                        listener.onAnonymousCount(anonymousCount);
                        listener.onTotalCount(viewerCounts.get("total").getAsInt());

                        List<Viewer> viewersList = new ArrayList<>();

                        // Oh yeah, pure jank.
                        if (this.lastAnonymousCount != anonymousCount) {
                            int difference = anonymousCount - this.lastAnonymousCount;

                            if (difference > 0) {
                                for (int i = 0; i != difference; i++) {
                                    listener.onJoin(anonymousViewer);
                                }
                            } else {
                                for (int i = 0; i != -difference; i++) {
                                    listener.onLeave(anonymousViewer);
                                }
                            }

                            this.lastAnonymousCount = anonymousCount;
                        }

                        for (int i = 0; i != anonymousCount; i++) {
                            viewersList.add(anonymousViewer);
                        }

                        viewersList.addAll(this.viewers.values());

                        viewersList.sort((v1, v2) -> {
                            return (v1.getJoinedAt() > v2.getJoinedAt()) ? 1 : -1;
                        });

                        listener.onViewerlist(viewersList);
                    }
                }
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            if (listener != null) {
                listener.onClose();
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
