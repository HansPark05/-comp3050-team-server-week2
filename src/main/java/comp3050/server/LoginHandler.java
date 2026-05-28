package comp3050.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class LoginHandler implements HttpHandler {

    private static final Map<String, String> USER_HASHES = new HashMap<>();

    static {
        USER_HASHES.put("Baelin",  sha256("Baelin;fishing123"));
        USER_HASHES.put("NPC-Man", sha256("NPC-Man;sword456"));
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        addCorsHeaders(he);

       if ("OPTIONS".equalsIgnoreCase(he.getRequestMethod())) {
        addCorsHeaders(he);
        he.sendResponseHeaders(204, -1);
        return;
    }

        if (!"POST".equalsIgnoreCase(he.getRequestMethod())) {
            sendResponse(he, 405, "{\"error\":\"method not allowed\"}");
            return;
        }

        String body = new String(he.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseForm(body);
        String name     = params.get("name");
        String encpswrd = params.get("encpswrd");

        if (name == null || name.isEmpty() || encpswrd == null || encpswrd.isEmpty()) {
            sendResponse(he, 400, "{\"error\":\"missing name or encpswrd\"}");
            return;
        }

        String storedHash = USER_HASHES.get(name);

        if (storedHash == null || !storedHash.equalsIgnoreCase(encpswrd)) {
            sendResponse(he, 401, "{\"error\":\"invalid credentials\"}");
            return;
        }

        // Invalidate existing session if already logged in
        if (SessionManager.getInstance().isLoggedIn(name)) {
            SessionManager.getInstance().invalidateUser(name);
        }

        // Create new session with player state (avatar placeholder, will be replaced by Arindam's PR)
        PlayerState state = new PlayerState(5, 5, '0');
        String token = SessionManager.getInstance().createSession(name, state);
        sendResponse(he, 200, "{\"session\":\"" + token + "\"}");
    }

   private void addCorsHeaders(HttpExchange he) {
    he.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
    he.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    he.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
}

    private void sendResponse(HttpExchange he, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        he.getResponseHeaders().set("Content-Type", "application/json");
        he.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = he.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Map<String, String> parseForm(String body) {
        Map<String, String> params = new HashMap<>();
        if (body == null || body.isEmpty()) return params;
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String val = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                params.put(key, val);
            }
        }
        return params;
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }
}