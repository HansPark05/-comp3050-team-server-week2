import java.io.IOException;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import comp3050.server.SessionManager;
import comp3050.server.PlayerState;
import comp3050.server.GameMap;

/*
  Handles GET /take?session=...
 
  Picks up the item on the tile the player is standing on (relative 0,0).
  Only a, c, h and k can be taken. The ground tile and any player avatar on the
  cell are kept. If the player already holds an item of the same class, the two
  are swapped: the held item is dropped on the ground and the new one is taken.
 
  Replies: 200 if something was taken, 204 if there is nothing to take (or the
  inventory is full and nothing of the same class can be swapped), 401 if the
  session is missing or invalid.
 */
public class TakeHandler implements HttpHandler {

    private static final int MAX_ITEMS = 5;

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
        if (!GameMap.isInBounds(y, x)) {
            he.sendResponseHeaders(204, -1);
            he.close();
            return;
        }

        synchronized (GameMap.class) {
            LocationString cell = LocationString.parse(GameMap.getTile(y, x));

            // Is there a takeable item on this tile?
            Optional<Character> itemChar = cell.findFirstItem();
            if (itemChar.isEmpty()) {
                he.sendResponseHeaders(204, -1);
                he.close();
                return;
            }

            char incoming = itemChar.get();
            // findFirstItem only returns a, c, h or k, so this is safe
            Item incomingItem = Item.fromSymbol(incoming).get();

            // Same-class swap: if we already hold an item of the same class,
            // replace it and drop the old one where we stand.
            Character displaced = null;
            for (int i = 0; i < player.inventory.size(); i++) {
                Item held = Item.fromSymbol(player.inventory.get(i)).orElse(null);
                if (held != null && held.itemClass() == incomingItem.itemClass()) {
                    displaced = player.inventory.set(i, incoming);
                    break;
                }
            }

            if (displaced == null) {
                // No same-class item to swap, so this is a plain add
                if (player.inventory.size() >= MAX_ITEMS) {
                    // Full and nothing to swap with.
                    he.sendResponseHeaders(204, -1);
                    he.close();
                    return;
                }
                player.inventory.add(incoming);
            }

            // Update the tile: remove the taken item, drop the displaced one
            cell.removeFromMiddle(incoming);
            if (displaced != null) {
                cell.addToMiddle(displaced);
            }
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