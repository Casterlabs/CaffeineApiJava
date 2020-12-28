package co.casterlabs.caffeineapi.realtime.viewers;

import co.casterlabs.caffeineapi.requests.CaffeineUser.UserBadge;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@NonNull
@ToString
@AllArgsConstructor
public class ViewerDetails {
    private String CAID;
    private String imageLink;
    private UserBadge badge;
    private String username;

}
