package comp3050.server;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();

    // token -> player state. ConcurrentHashMap so multiple request threads
    // can read/write safely without our own locking.
    private final Map<String, PlayerState> sessions = new ConcurrentHashMap<>();

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    // Username is currently only used by LoginHandler for the credential
    // check, we don't store it here. If we need it later we can add a
    // username field to PlayerState.
    public String createSession(String username, PlayerState state) {
        // Strip the dashes so the token is just letters and digits,
        // which is what the v3 spec asks for.
        String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, state);
        return token;
    }

    public PlayerState getPlayer(String token) {
        if (token == null) return null;
        return sessions.get(token);
    }

    public boolean invalidate(String token) {
        if (token == null) return false;
        return sessions.remove(token) != null;
    }
}
