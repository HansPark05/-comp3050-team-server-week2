package comp3050.server;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();

    private final Map<String, PlayerState> sessions = new ConcurrentHashMap<>();

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public String createSession(String username, PlayerState state) {
        state.username = username;
        String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, state);
        return token;
    }

    public PlayerState getPlayer(String token) {
        if (token == null) return null;
        return sessions.get(token);
    }

    public String getUser(String token) {
        PlayerState p = getPlayer(token);
        return p == null ? null : p.username;
    }

    public boolean invalidate(String token) {
        if (token == null) return false;
        return sessions.remove(token) != null;
    }

    public boolean isLoggedIn(String username) {
        return sessions.values().stream()
                .anyMatch(p -> username.equals(p.username));
    }

    public void invalidateUser(String username) {
        sessions.entrySet().removeIf(e -> username.equals(e.getValue().username));
    }
}