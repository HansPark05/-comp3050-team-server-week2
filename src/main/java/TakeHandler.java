import java.io.IOException;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import comp3050.server.SessionManager;
import comp3050.server.PlayerState;
import comp3050.server.GameMap;

public class TakeHandler implements HttpHandler {

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

        int y = player.y;
        int x = player.x;

        synchronized (GameMap.class) {
            // tiles are strings now (could be multi-char like "gk2"), so grab
            // the first character to check what's there. Abdul's PR will turn
            // this into real item lookup + class-swap.
            String tileStr = GameMap.getTile(y, x);
            char tile = tileStr.isEmpty() ? ' ' : tileStr.charAt(0);

            if (tile == 'a' || tile == 'c' || tile == 'h' || tile == 'k') {
                GameMap.setTile(y, x, "g");
                player.inventory.add(tile);
                String response = "{\"item\":\"" + tile + "\",\"y\":" + y + ",\"x\":" + x + "}";
                he.getResponseHeaders().set("Content-Type", "application/json");
                he.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = he.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                he.sendResponseHeaders(204, -1);
                he.close();
            }
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
