import java.io.IOException;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import comp3050.server.SessionManager;
import comp3050.server.PlayerState;
import comp3050.server.GameMap;

/*
  Handles GET /place?session=...
 
  Drops the player's first inventory item onto the tile they are standing on.
  The ground tile and any player avatar are kept. We only allow one item per
  tile, so if there is already an item here we refuse and leave the inventory
  unchanged.
 
  Replies: 200 if an item was placed, 204 if there is nothing to place or the
  tile already has an item, 401 if the session is missing or invalid
 */
public class PlaceHandler implements HttpHandler {

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

        // Nothing to place
        if (player.inventory.isEmpty()) {
            he.sendResponseHeaders(204, -1);
            he.close();
            return;
        }

        int y = player.y;
        int x = player.x;
        if (!GameMap.isInBounds(y, x)) {
            he.sendResponseHeaders(204, -1);
            he.close();
            return;
        }

        synchronized (GameMap.class) {
            LocationString cell = LocationString.parse(GameMap.getTile(y, x));

            // One item per tile: if something is already here, do nothing
            if (cell.findFirstItem().isPresent()) {
                he.sendResponseHeaders(204, -1);
                he.close();
                return;
            }

            // Take the first item out of the inventory and put it on the tile
            char item = player.inventory.remove(0);
            cell.addToMiddle(item);
            GameMap.setTile(y, x, cell.render());

            he.sendResponseHeaders(200, -1);
            he.close();
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