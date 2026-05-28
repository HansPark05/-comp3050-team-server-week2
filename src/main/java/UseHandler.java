import java.io.IOException;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import comp3050.server.SessionManager;
import comp3050.server.PlayerState;
import comp3050.server.GameMap;

public class UseHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange he) throws IOException {
        he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        he.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");

        if ("OPTIONS".equals(he.getRequestMethod())) {
            he.sendResponseHeaders(204, -1);
            return;
        }

        String session = extractParam(he, "session");
        PlayerState player = SessionManager.getInstance().getPlayer(session);
        if (player == null) {
            he.sendResponseHeaders(401, -1);
            he.close();
            return;
        }

        int dy = 0;
        int dx = 0;
        String dyStr = extractParam(he, "dy");
        String dxStr = extractParam(he, "dx");
        if (dyStr != null) dy = Integer.parseInt(dyStr);
        if (dxStr != null) dx = Integer.parseInt(dxStr);

        // Diagonal or longer-than-one moves are not allowed.
        if (Math.abs(dy) + Math.abs(dx) > 1) {
            he.sendResponseHeaders(204, -1);
            he.close();
            return;
        }

        int targetY = player.y + dy;
        int targetX = player.x + dx;

        if (!GameMap.isInBounds(targetY, targetX)) {
            he.sendResponseHeaders(204, -1);
            he.close();
            return;
        }

        synchronized (GameMap.class) {
            String tileStr = GameMap.getTile(targetY, targetX);
            char tile = tileStr.isEmpty() ? ' ' : tileStr.charAt(0);

            if (tile != 'D' && tile != 'd') {
                he.sendResponseHeaders(204, -1);
                he.close();
                return;
            }

            char newTile = (tile == 'D') ? 'd' : 'D';
            GameMap.setTile(targetY, targetX, String.valueOf(newTile));

            String response = "{\"y\":" + targetY + ",\"x\":" + targetX
                    + ",\"tile\":\"" + newTile + "\"}";
            he.getResponseHeaders().set("Content-Type", "application/json");
            he.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

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
