package co.casterlabs.caffeineapi.realtime.query;

public interface CaffeineQueryListener {

    default void onOpen() {}

    public void onStreamStateChanged(boolean isLive, String title);

    default void onClose() {}

}