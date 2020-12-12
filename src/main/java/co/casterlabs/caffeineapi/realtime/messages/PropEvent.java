package co.casterlabs.caffeineapi.realtime.messages;

import co.casterlabs.caffeineapi.requests.CaffeinePropsListRequest.CaffeineProp;
import co.casterlabs.caffeineapi.requests.CaffeineUserInfoRequest.CaffeineUser;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class PropEvent extends ChatEvent {
    private CaffeineProp prop;

    public PropEvent(@NonNull CaffeineUser sender, @NonNull String message, @NonNull String id, @NonNull CaffeineProp prop) {
        super(sender, message, id);

        this.prop = prop;
    }

}
