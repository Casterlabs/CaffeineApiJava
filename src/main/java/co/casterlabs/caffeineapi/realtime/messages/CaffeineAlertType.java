package co.casterlabs.caffeineapi.realtime.messages;

import com.google.gson.JsonElement;

public enum CaffeineAlertType {
    REACTION,
    DIGITAL_ITEM,
    SHARE,
    UNKNOWN;

    public static CaffeineAlertType fromJson(JsonElement element) {
        switch (element.getAsString().toUpperCase()) {
            case "REACTION":
                return REACTION;

            case "DIGITAL_ITEM":
                return DIGITAL_ITEM;

            case "RESCIND": // ?
                return UNKNOWN;

            case "SHARE":
                return SHARE;

            default:
                return UNKNOWN;
        }
    }

}
