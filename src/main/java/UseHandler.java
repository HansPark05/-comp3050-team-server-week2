import java.io.IOException;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import comp3050.server.SessionManager;

public class UseHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange he) throws IOException {
        he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        he.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");

        // Handle preflight request
        if ("OPTIONS".equals(he.getRequestMethod())) {
            he.sendResponseHeaders(204, -1);
            return;
        }

        // Validate session
        String session = extractParam(he, "session");
        if (session == null || SessionManager.getInstance().getUser(session) == null) {
            he.sendResponseHeaders(401, -1);
            he.close();
            return;
        }

        // Get dy and dx from query params (default 0)
        int dy = 0;
        int dx = 0;
        String dyStr = extractParam(he, "dy");
        String dxStr = extractParam(he, "dx");
        if (dyStr != null) dy = Integer.parseInt(dyStr);
        if (dxStr != null) dx = Integer.parseInt(dxStr);

        // Must be exactly one step in one direction (no diagonal)
        if (Math.abs(dy) + Math.abs(dx) > 1) {
            he.sendResponseHeaders(204, -1);
            he.close();
            return;
        }

        // Get the target tile position
        int targetY = Test.playerY + dy;
        int targetX = Test.playerX + dx;

        // Check bounds
        if (!GameMap.isInBounds(targetY, targetX)) {
            he.sendResponseHeaders(204, -1);
            he.close();
            return;
        }

        synchronized (GameMap.class) {
            char tile = GameMap.getTile(targetY, targetX);

            if (tile != 'D' && tile != 'd') {
                he.sendResponseHeaders(204, -1);
                he.close();
                return;
            }

            char newTile = (tile == 'D') ? 'd' : 'D';
            GameMap.setTile(targetY, targetX, newTile);

            String response = "{\"y\":" + targetY + ",\"x\":" + targetX + ",\"tile\":\"" + newTile + "\"}";
            he.getResponseHeaders().set("Content-Type", "application/json");
            he.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    // Extract a query parameter value by key from the request URL
    private String extractParam(HttpExchange he, String key) {
        String query = he.getRequestURI().getQuery();
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && key.equals(kv[0])) return kv[1];
        }
        return null;
    }
}