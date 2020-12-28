package co.casterlabs.caffeineapi.realtime.messages;

public interface CaffeineMessagesListener {

    default void onOpen() {}

    public void onChat(ChatEvent event);

    public void onShare(ShareEvent event);

    public void onProp(PropEvent event);

    public void onUpvote(UpvoteEvent event);

    default void onClose() {}

}