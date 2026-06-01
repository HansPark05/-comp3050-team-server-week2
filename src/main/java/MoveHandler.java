import java.io.IOException;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import comp3050.server.SessionManager;
import comp3050.server.PlayerState;
import comp3050.server.GameMap;

public class MoveHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange he) throws IOException {
        he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        he.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");

        if ("OPTIONS".equals(he.getRequestMethod())) {
            he.sendResponseHeaders(204, -1);
            return;
        }

        String query = he.getRequestURI().getQuery();
        int dy = 0, dx = 0;
        String session = null;

        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    if (pair[0].equals("dy"))      dy      = Integer.parseInt(pair[1]);
                    else if (pair[0].equals("dx")) dx      = Integer.parseInt(pair[1]);
                    else if (pair[0].equals("session")) session = pair[1];
                }
            }
        }

        // Look up the player for this session. Null means token is invalid.
        PlayerState player = SessionManager.getInstance().getPlayer(session);
        if (player == null) {
            he.sendResponseHeaders(401, -1);
            he.close();
            return;
        }

        // Block diagonal and multi-step moves
        if (Math.abs(dy) + Math.abs(dx) > 1) {
            send204(he);
            return;
        }

        int oldY = player.y;
        int oldX = player.x;
        int targetY = oldY + dy;
        int targetX = oldX + dx;

        // dy=0, dx=0 is a valid no-op: return current position immediately
        if (dy == 0 && dx == 0) {
            String response = String.format("{\"y\": %d, \"x\": %d}", oldY, oldX);
            he.getResponseHeaders().set("Content-Type", "application/json");
            he.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
            return;
        }

        // Bounds and structural blocking check
        if (!GameMap.isInBounds(targetY, targetX) || GameMap.isBlocking(targetY, targetX)) {
            send204(he);
            return;
        }

        // Multiplayer: block if another player's avatar digit is already on the target tile
        for (char c : GameMap.getTile(targetY, targetX).toCharArray()) {
            if (Character.isDigit(c)) {
                send204(he);
                return;
            }
        }

        // Remove avatar digit from old tile, append to new tile
        String avatarStr = String.valueOf(player.avatar);
        GameMap.setTile(oldY, oldX, GameMap.getTile(oldY, oldX).replace(avatarStr, ""));
        GameMap.setTile(targetY, targetX, GameMap.getTile(targetY, targetX) + avatarStr);

        // Save new position back into the session
        player.y = targetY;
        player.x = targetX;

        String response = String.format("{\"y\": %d, \"x\": %d}", targetY, targetX);
        he.getResponseHeaders().set("Content-Type", "application/json");
        he.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = he.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private void send204(HttpExchange he) throws IOException {
        he.sendResponseHeaders(204, -1);
        he.close();
    }
}
