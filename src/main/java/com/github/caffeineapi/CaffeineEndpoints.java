package com.github.caffeineapi;

public class CaffeineEndpoints {
    // Prepend
    public static final String IMAGES = "https://images.caffeine.tv";
    public static final String ASSETS = "https://assets.caffeine.tv";

    // Formatted
    public static final String USERS = "https://api.caffeine.tv/v1/users/%s";
    public static final String SIGNED = USERS + "/signed";
    public static final String CHAT_MESSAGE = "https://realtime.caffeine.tv/v2/reaper/stages/%s/messages";
    public static final String UPVOTE_MESSAGE = "https://realtime.caffeine.tv/v2/reaper/messages/%s/endorsements";

    // Standalone
    public static final String GAMES_LIST = "https://api.caffeine.tv/v1/games";
    public static final String PROPS_LIST = "https://payments.caffeine.tv/store/get-digital-items";
    public static final String SIGNIN = "https://api.caffeine.tv/v1/account/signin";
    public static final String TOKEN = "https://api.caffeine.tv/v1/account/token";

}
