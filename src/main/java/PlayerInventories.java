import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/* Holds a separate inventory for each player, keyed by username.
 The old server kept ONE shared inventory for everyone, which is wrong once
 more than one player is logged in. SessionManager maps a token to a username,
 so we go token -> username -> inventory */
public final class PlayerInventories {

    public static final int DEFAULT_MAX_ITEMS = 5;

    // Concurrent map because the HTTP server answers requests on several threads.
    private static final Map<String, Inventory> inventories = new ConcurrentHashMap<>();

    private PlayerInventories() { }

    public static Inventory forUser(String username) {
        return inventories.computeIfAbsent(username, u -> new Inventory(DEFAULT_MAX_ITEMS));
    }

    /* Returns null when the session is missing or unknown, so the handler can
     reply 401. This is the only spot that knows how player state is stored:
     once PlayerState exists it becomes getPlayer(session).getInventory() */
    public static Inventory forSession(String session) {
        if (session == null || session.isEmpty()) return null;
        String username = comp3050.server.SessionManager.getInstance().getUser(session);
        if (username == null) return null;
        return forUser(username);
    }

    public static void remove(String username) {
        inventories.remove(username);
    }

    // Lets a test start from an empty state.
    public static void reset() {
        inventories.clear();
    }
}