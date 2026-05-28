import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

// Checks the inventory rules: bounded size, the same-class swap, and the
// difference between a swap (count stays the same) and a full add (rejected).
class InventoryTest {

    @Test
    void newInventoryIsEmpty() {
        Inventory inv = new Inventory(3);
        assertTrue(inv.isEmpty());
        assertFalse(inv.isFull());
        assertEquals(0, inv.size());
    }

    @Test
    void takeAddsWhenThereIsRoom() throws Exception {
        Inventory inv = new Inventory(3);
        Optional<Item> displaced = inv.take(Item.AXE);
        assertTrue(displaced.isEmpty());   // nothing swapped out
        assertEquals(1, inv.size());
    }

    @Test
    void sameClassSwapsInsteadOfGrowing() throws Exception {
        Inventory inv = new Inventory(3);
        inv.take(Item.CYAN_POTION);
        Optional<Item> displaced = inv.take(Item.HEART_POTION);
        assertTrue(displaced.isPresent());
        assertEquals(Item.CYAN_POTION, displaced.get());
        assertEquals(1, inv.size());       // swapped, not added
        assertEquals(Item.HEART_POTION, inv.view().get(0));
    }

    @Test
    void swapWorksEvenWhenFull() throws Exception {
        Inventory inv = new Inventory(1);
        inv.take(Item.CYAN_POTION);
        assertTrue(inv.isFull());
        Optional<Item> displaced = inv.take(Item.HEART_POTION);
        assertTrue(displaced.isPresent());
        assertEquals(Item.CYAN_POTION, displaced.get());
        assertEquals(1, inv.size());
    }

    @Test
    void fullAddWithNoSwapThrows() throws Exception {
        Inventory inv = new Inventory(2);
        inv.take(Item.AXE);          // tool
        inv.take(Item.CYAN_POTION);  // drink
        // No artifact held and no room, so the key cannot be taken.
        assertThrows(Inventory.InventoryFullException.class, () -> inv.take(Item.KEY));
        assertEquals(2, inv.size());
    }

    @Test
    void hasKeyReflectsArtifact() throws Exception {
        Inventory inv = new Inventory(3);
        assertFalse(inv.hasKey());
        inv.take(Item.KEY);
        assertTrue(inv.hasKey());
    }

    @Test
    void removeFirstIsFifo() throws Exception {
        Inventory inv = new Inventory(3);
        inv.take(Item.AXE);
        inv.take(Item.CYAN_POTION);
        assertEquals(Item.AXE, inv.removeFirst().orElseThrow());
        assertEquals(1, inv.size());
    }

    @Test
    void removeFirstOnEmptyReturnsEmpty() {
        assertTrue(new Inventory(3).removeFirst().isEmpty());
    }

    @Test
    void constructorRejectsZeroOrLess() {
        assertThrows(IllegalArgumentException.class, () -> new Inventory(0));
    }

    @Test
    void viewCannotBeModified() throws Exception {
        Inventory inv = new Inventory(3);
        inv.take(Item.AXE);
        var snapshot = inv.view();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(Item.KEY));
    }
}