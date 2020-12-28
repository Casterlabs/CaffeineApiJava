package co.casterlabs.caffeineapi.realtime.viewers;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.apiutil.web.ApiException;
import co.casterlabs.caffeineapi.requests.CaffeineUser;
import co.casterlabs.caffeineapi.requests.CaffeineUserInfoRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class Viewer {
    private @NonNull ViewerDetails userDetails;
    private long joinedAt;

    public @Nullable String getCAID() {
        return this.userDetails.getCAID();
    }

    public boolean isAnonymous() {
        return this.userDetails.getCAID().equals("Anonymous");
    }

    public CaffeineUser getAsUser() throws ApiException {
        if (this.isAnonymous()) {
            throw new ApiException("Cannot convert anonymous viewer to CaffeineUser.");
        } else {
            CaffeineUserInfoRequest request = new CaffeineUserInfoRequest();

            request.setCAID(this.userDetails.getCAID());

            return request.send();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Viewer) {
            Viewer other = (Viewer) obj;

            if (!other.isAnonymous()) {
                return other.getCAID().equals(this.getCAID());
            }
        }

        return false;
    }

}
