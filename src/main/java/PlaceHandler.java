import java.io.IOException;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/*
 * Handles GET /place?session=...
 *
 * Drops the player's first inventory item onto the tile they are standing on.
 * The ground tile and any player avatar are kept. We only allow one item per
 * tile, so if there is already an item here we refuse and leave the inventory
 * unchanged.
 *
 * Replies: 200 if an item was placed, 204 if there is nothing to place or the
 * tile already has an item, 401 if the session is missing or invalid.
 */
public class PlaceHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange he) throws IOException {
        Http.cors(he);
        if (Http.isPreflight(he)) {
            Http.empty(he, 204);
            return;
        }

        Inventory inventory = PlayerInventories.forSession(Http.param(he, "session"));
        if (inventory == null) {
            Http.empty(he, 401);
            return;
        }

        // Nothing to place.
        if (inventory.isEmpty()) {
            Http.empty(he, 204);
            return;
        }

        int y = Test.playerY;
        int x = Test.playerX;
        if (!GameMap.isInBounds(y, x)) {
            Http.empty(he, 204);
            return;
        }

        LocationString cell = LocationString.parse(GameMap.getCell(y, x));

        // One item per tile: if something is already here, do nothing.
        if (cell.findFirstItem().isPresent()) {
            Http.empty(he, 204);
            return;
        }

        // Take the first item out of the inventory and put it on the tile.
        // present() is guaranteed because we checked isEmpty() above.
        Optional<Item> item = inventory.removeFirst();
        cell.addToMiddle(item.get().symbol());
        GameMap.setCell(y, x, cell.render());
        Http.empty(he, 200);
    }
}