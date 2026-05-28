import java.io.IOException;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/*
 * Handles GET /take?session=...
 *
 * Picks up the item on the tile the player is standing on (relative 0,0).
 * Only a, c, h and k can be taken. The ground tile and any player avatar on the
 * cell are kept. If the player already holds an item of the same class, the two
 * are swapped: the held item is dropped on the ground and the new one is taken.
 *
 * Replies: 200 if something was taken, 204 if there is nothing to take (or the
 * inventory is full and nothing of the same class can be swapped), 401 if the
 * session is missing or invalid.
 */
public class TakeHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange he) throws IOException {
        Http.cors(he);
        if (Http.isPreflight(he)) {
            Http.empty(he, 204);
            return;
        }

        // Check the session and get this player's inventory (null = not logged in).
        Inventory inventory = PlayerInventories.forSession(Http.param(he, "session"));
        if (inventory == null) {
            Http.empty(he, 401);
            return;
        }

        // Position is still the shared one for now (per-player position is the
        // next step - see notes). Make sure it is on the map.
        int y = Test.playerY;
        int x = Test.playerX;
        if (!GameMap.isInBounds(y, x)) {
            Http.empty(he, 204);
            return;
        }

        LocationString cell = LocationString.parse(GameMap.getCell(y, x));

        // Is there an item on this tile?
        Optional<Character> itemChar = cell.findFirstItem();
        if (itemChar.isEmpty()) {
            Http.empty(he, 204);
            return;
        }

        // findFirstItem only returns a, c, h or k, so this is always present.
        Item incoming = Item.fromSymbol(itemChar.get()).get();

        // Add it to the inventory. take() returns an item of the same class that
        // was swapped out (if any), or throws if the inventory is full.
        Optional<Item> displaced;
        try {
            displaced = inventory.take(incoming);
        } catch (Inventory.InventoryFullException full) {
            Http.empty(he, 204);
            return;
        }

        // Remove the taken item from the tile.
        cell.removeFromMiddle(incoming.symbol());

        // If we swapped, drop the old item where we are standing.
        if (displaced.isPresent()) {
            cell.addToMiddle(displaced.get().symbol());
        }

        GameMap.setCell(y, x, cell.render());
        Http.empty(he, 200);
    }
}