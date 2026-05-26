package comp3050.server;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private static class SessionData {
        String username;
        char avatar;
        int y;
        int x;

        SessionData(String username, char avatar, int y, int x) {
            this.username = username;
            this.avatar   = avatar;
            this.y        = y;
            this.x        = x;
        }
    }

    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    // Change: now accepts avatar char, stores position defaulting to (1,1)
    // Change your starting y,x here to match wherever players spawn on your map
    public String createSession(String username, char avatar) {
        String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, new SessionData(username, avatar, 1, 1));
        return token;
    }

    public String getUser(String token) {
        if (token == null) return null;
        SessionData data = sessions.get(token);
        return data == null ? null : data.username;
    }

    public char getAvatar(String token) {
        SessionData data = sessions.get(token);
        return data == null ? '0' : data.avatar;
    }

    public int[] getPosition(String token) {
        SessionData data = sessions.get(token);
        return data == null ? null : new int[]{ data.y, data.x };
    }

    public void setPosition(String token, int y, int x) {
        SessionData data = sessions.get(token);
        if (data != null) {
            data.y = y;
            data.x = x;
        }
    }

    public boolean invalidate(String token) {
        if (token == null) return false;
        return sessions.remove(token) != null;
    }
}
