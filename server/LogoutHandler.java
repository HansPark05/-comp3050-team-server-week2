package comp3050.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class LogoutHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange he) throws IOException {
        addCorsHeaders(he);

        if ("OPTIONS".equalsIgnoreCase(he.getRequestMethod())) {
            he.sendResponseHeaders(204, -1);
            return;
        }

        if (!"GET".equalsIgnoreCase(he.getRequestMethod())) {
            sendResponse(he, 405, "{\"error\":\"method not allowed\"}");
            return;
        }

        String token = extractSessionParam(he);

        if (token == null || token.isEmpty()) {
            sendResponse(he, 401, "{\"error\":\"missing session token\"}");
            return;
        }

        if (!SessionManager.getInstance().invalidate(token)) {
            sendResponse(he, 401, "{\"error\":\"invalid session token\"}");
            return;
        }

        sendResponse(he, 200, "{\"message\":\"logged out\"}");
    }

    // Parses ?session=TOKEN from the query string.
    private String extractSessionParam(HttpExchange he) {
        String query = he.getRequestURI().getQuery();
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && "session".equals(kv[0])) {
                return kv[1];
            }
        }
        return null;
    }

    private void addCorsHeaders(HttpExchange he) {
        he.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        he.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }

    private void sendResponse(HttpExchange he, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        he.getResponseHeaders().set("Content-Type", "application/json");
        he.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = he.getResponseBody()) {
            os.write(bytes);
        }
    }
}
