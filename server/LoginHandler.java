package comp3050.server;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class LoginHandler implements HttpHandler {

    private static final String VALID_USER = System.getenv("APP_USER");
    private static final String VALID_PASS = System.getenv("APP_PASS");
    private static final AtomicInteger avatarCounter = new AtomicInteger(0);

    @Override
    public void handle(HttpExchange he) throws IOException {
        if (!"POST".equalsIgnoreCase(he.getRequestMethod())) {
            sendResponse(he, 405, "{\"error\":\"method not allowed\"}");
            return;
        }

        String body = new String(he.getRequestBody().readAllBytes());
        Map<String, String> params = parseForm(body);
        String user = params.get("user");
        String pass = params.get("pass");

        if (VALID_USER != null && VALID_PASS != null
                && VALID_USER.equals(user) && VALID_PASS.equals(pass)) {

            char avatar = (char)('0' + (avatarCounter.getAndIncrement() % 10));
            String token = SessionManager.getInstance().createSession(user, avatar);

            // Place avatar digit on the map at the player's starting tile
            int[] startPos = SessionManager.getInstance().getPosition(token);
            String avatarStr = String.valueOf(avatar);
            GameMap.setTile(startPos[0], startPos[1],
                    GameMap.getTile(startPos[0], startPos[1]) + avatarStr);

            sendResponse(he, 200, "{\"token\":\"" + token + "\"}");
        } else {
            sendResponse(he, 401, "{\"error\":\"invalid credentials\"}");
        }
    }

    private void sendResponse(HttpExchange he, int status, String body)
            throws IOException {
        he.getResponseHeaders().set("Content-Type", "application/json");
        he.sendResponseHeaders(status, body.getBytes().length);
        OutputStream os = he.getResponseBody();
        os.write(body.getBytes());
        os.close();
    }

    private Map<String, String> parseForm(String body) {
        Map<String, String> params = new HashMap<>();
        if (body == null || body.isEmpty()) return params;
        for (String param : body.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2) params.put(pair[0], pair[1]);
        }
        return params;
    }
}
