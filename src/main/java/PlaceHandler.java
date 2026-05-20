import java.io.IOException;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import comp3050.server.SessionManager;

public class PlaceHandler implements HttpHandler {

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

        // Check if inventory is empty
        if (Test.inventory.isEmpty()) {
            he.sendResponseHeaders(204, -1);
            he.close();
            return;
        }

        // Get current player position
        int y = Test.playerY;
        int x = Test.playerX;
        char currentTile = GameMap.getTile(y, x);

        // Can only place item on grass (empty ground)
        if (currentTile != 'g') {
            he.sendResponseHeaders(204, -1);
            he.close();
            return;
        }

        // Place item from inventory onto the map
        char item = Test.inventory.remove(0);
        GameMap.setTile(y, x, item);

        String response = "{\"item\":\"" + item + "\",\"y\":" + y + ",\"x\":" + x + "}";
        he.getResponseHeaders().set("Content-Type", "application/json");
        he.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = he.getResponseBody();
        os.write(response.getBytes());
        os.close();
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