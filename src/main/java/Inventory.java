import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/*
 * A single player's inventory. Encapsulates the v3 rules: bounded size,
 * class-based mutual exclusion (same-class take swaps), and capacity-vs-swap
 * (a swap succeeds even when full and a genuine add that would overflow throws).
 * No HTTP/server dependencies, so it is trivial to unit test.
 */
public final class Inventory {

    private final List<Item> items = new ArrayList<>();
    private final int maxSize;

    public Inventory(int maxSize) {
        if (maxSize < 1) {
            throw new IllegalArgumentException("maxSize must be >= 1, got " + maxSize);
        }
        this.maxSize = maxSize;
    }

    public int size() { return items.size(); }
    public int maxSize() { return maxSize; }
    public boolean isEmpty() { return items.isEmpty(); }
    public boolean isFull() { return items.size() >= maxSize; }

    // Immutable snapshot of contents (for tests / debugging)
    public List<Item> view() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    public boolean hasItemOfClass(ItemClass itemClass) {
        for (Item item : items) {
            if (item.itemClass() == itemClass) {
                return true;
            }
        }
        return false;
    }

    // Convenience for the locked-door (USE) feature: holding the artifact (key)
    public boolean hasKey() {
        return hasItemOfClass(ItemClass.ARTIFACT);
    }

    /**
     * Take an item, applying the class-swap rule.
     * @return empty if simply added. Otherwise the displaced same-class item the caller
     *  must drop on the ground.
     * @throws InventoryFullException if full and no same-class item to swap.
     */
    public Optional<Item> take(Item incoming) throws InventoryFullException {
        // Swap path: replace any existing item of the same class (works when full).
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).itemClass() == incoming.itemClass()) {
                Item displaced = items.set(i, incoming);
                return Optional.of(displaced);
            }
        }
        // Add path: only if there is spare capacity.
        if (isFull()) {
            throw new InventoryFullException(maxSize);
        }
        items.add(incoming);
        return Optional.empty();
    }

    // Remove and return the first item, used by PLACE (FIFO)
    public Optional<Item> removeFirst() {
        if (items.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(items.remove(0));
    }

    // Thrown when an add would exceed capacity and no swap is possible
    public static final class InventoryFullException extends Exception {
        public InventoryFullException(int maxSize) {
            super("Inventory is full (max " + maxSize + ")");
        }
    }
}