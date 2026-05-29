import java.io.IOException;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import comp3050.server.SessionManager;
import comp3050.server.PlayerState;
import comp3050.server.GameMap;

public class InfoHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange he) throws IOException {
        he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        he.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");

        if ("OPTIONS".equals(he.getRequestMethod())) {
            he.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            he.sendResponseHeaders(204, -1);
            return;
        }

        String query = he.getRequestURI().getQuery();
        int y = -1;
        int x = -1;
        String session = null;

        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    try {
                        if (pair[0].equals("y")) y = Integer.parseInt(pair[1]);
                        else if (pair[0].equals("x")) x = Integer.parseInt(pair[1]);
                        else if (pair[0].equals("session")) session = pair[1];
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        PlayerState player = SessionManager.getInstance().getPlayer(session);
        if (player == null) {
            he.sendResponseHeaders(401, -1);
            he.close();
            return;
        }

        // Client must ask about its own current location. Otherwise 204.
        if (y != player.y || x != player.x) {
            he.sendResponseHeaders(204, -1);
            he.close();
            return;
        }

        int top    = player.y - 5;
        int left   = player.x - 5;
        int bottom = player.y + 5;
        int right  = player.x + 5;

        StringBuilder info = new StringBuilder("[");
        for (int row = top; row <= bottom; row++) {
            if (row > top) info.append(",");
            info.append("[");
            for (int col = left; col <= right; col++) {
                if (col > left) info.append(",");
                String tile;
                if (GameMap.isInBounds(row, col)) {
                    tile = GameMap.getTile(row, col);
                } else {
                    tile = "g";
                }
                info.append("\"").append(tile).append("\"");
            }
            info.append("]");
        }
        info.append("]");

        String response = String.format(
            "{\"y\":%d,\"x\":%d,\"top\":%d,\"left\":%d,\"bottom\":%d,\"right\":%d,\"info\":%s}",
            player.y, player.x, top, left, bottom, right, info
        );

        he.getResponseHeaders().set("Content-Type", "application/json");
        he.getResponseHeaders().set("Connection", "close");
        he.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = he.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
