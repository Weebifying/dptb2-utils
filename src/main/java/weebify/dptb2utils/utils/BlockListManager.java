package weebify.dptb2utils.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class BlockListManager {
    public static final Map<String, String> discUsernameCache = new LinkedHashMap<>();
    public static final Map<String, String> wptbUsernameCache = new LinkedHashMap<>();

    public static Map<String, String> getDiscUsername() {
        return discUsernameCache;
    }
    public static String getDiscUsername(String userId) {
        return discUsernameCache.get(userId);
    }
    public static void putDiscUsername(String userId, String username) {
        discUsernameCache.put(userId, username);
    }
    public static void removeDiscUsername(String userId) {
        discUsernameCache.remove(userId);
    }

    public static Map<String, String> getWptbUsername() {
        return wptbUsernameCache;
    }
    public static String getWptbUsername(String userId) {
        return wptbUsernameCache.get(userId);
    }
    public static void putWptbUsername(String userId, String username) {
        wptbUsernameCache.put(userId, username);
    }
    public static void removeWptbUsername(String userId) {
        wptbUsernameCache.remove(userId);
    }
}