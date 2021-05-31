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
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Nullable;

import co.casterlabs.apiutil.web.ApiException;
import co.casterlabs.caffeineapi.CaffeineApi;
import co.casterlabs.caffeineapi.CaffeineAuth;
import co.casterlabs.caffeineapi.CaffeineEndpoints;
import co.casterlabs.caffeineapi.requests.CaffeineUserInfoRequest;
import co.casterlabs.caffeineapi.types.CaffeineUser;
import co.casterlabs.caffeineapi.types.UserBadge;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Setter;
import lombok.SneakyThrows;

public class CaffeineViewers implements Closeable {
    private static final String AUTH_LOGIN_HEADER = "{\"Headers\":{\"Authorization\":\"Bearer %s\",\"X-Client-Type\":\"api\"},\"Body\":\"{\\\"user\\\":\\\"%s\\\"}\"}";
    private static final long CAFFEINE_KEEPALIVE = TimeUnit.SECONDS.toMillis(15);

    //@formatter:off
    private static Viewer anonymousViewer = new Viewer(
                new ViewerDetails(
                    "Anonymous", 
                    "https://images.caffeine.tv/defaults/avatar-001.png", 
                    UserBadge.NONE, 
                    "anonymous"
                ), 
                null, 
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

        @SneakyThrows
        @Override
        public void onMessage(String raw) {
            if (!raw.equals("\"THANKS\"") && (listener != null)) {
                JsonObject json = CaffeineApi.RSON.fromJson(raw, JsonObject.class);

                if (!json.containsKey("Compatibility-Mode")) {
                    if (json.containsKey("anonymous_user_count")) {
                        int anonymousCount = json.getNumber("anonymous_user_count").intValue();

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
                    } else if (json.containsKey("user_event")) {
                        JsonObject userEvent = json.getObject("user_event");
                        String caid = userEvent.get("caid").getAsString();

                        if (userEvent.get("is_viewing").getAsBoolean()) {
                            try {
                                CaffeineUserInfoRequest request = new CaffeineUserInfoRequest().setCAID(caid);
                                CaffeineUser user = request.send();

                                //@formatter:off
                                Viewer viewer = new Viewer(
                                            new ViewerDetails(
                                                caid, 
                                                user.getImageLink(), 
                                                user.getBadge(), 
                                                user.getUsername()
                                            ), 
                                            user, 
                                            System.currentTimeMillis() / 1000
                                        );
                                //@formatter:on

                                this.viewers.put(caid, viewer);

                                if (listener != null) {
                                    listener.onJoin(viewer);
                                }
                            } catch (ApiException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Viewer viewer = this.viewers.remove(caid);

                            if (listener != null) {
                                listener.onLeave(viewer);
                            }
                        }
                    }

                    if (listener != null) {
                        listener.onAnonymousCount(this.lastAnonymousCount);
                        listener.onTotalCount(this.viewers.size() + this.lastAnonymousCount);

                        List<Viewer> viewersList = new ArrayList<>();

                        for (int i = 0; i != lastAnonymousCount; i++) {
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
