package co.casterlabs.caffeineapi.realtime.messages;

import co.casterlabs.caffeineapi.requests.CaffeineUserInfoRequest.CaffeineUser;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class ShareEvent extends ChatEvent {

    public ShareEvent(@NonNull CaffeineUser sender, @NonNull String id, @NonNull String message) {
        super(sender, message, id);
    }

}
