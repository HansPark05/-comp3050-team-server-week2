import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

// Checks the layered-cell parser. The important ones are the cases TAKE/PLACE
// rely on: keeping the ground tile and the player avatar when an item changes.
class LocationStringTest {

    @Test
    void parsesPlainGround() {
        LocationString c = LocationString.parse("g");
        assertEquals('g', c.getGround());
        assertTrue(c.getMiddle().isEmpty());
        assertTrue(c.getPlayer().isEmpty());
        assertEquals("g", c.render());
    }

    @Test
    void parsesGroundPlusItem() {
        LocationString c = LocationString.parse("_k");
        assertEquals('_', c.getGround());
        assertEquals(Character.valueOf('k'), c.getMiddle().get(0));
        assertTrue(c.getPlayer().isEmpty());
    }

    @Test
    void parsesGroundItemAndPlayer() {
        LocationString c = LocationString.parse("gk2");
        assertEquals('g', c.getGround());
        assertEquals(Character.valueOf('k'), c.getMiddle().get(0));
        assertEquals(Optional.of('2'), c.getPlayer());
    }

    @Test
    void parsesGroundAndDoor() {
        LocationString c = LocationString.parse("wD");
        assertEquals('w', c.getGround());
        assertEquals(Character.valueOf('D'), c.getMiddle().get(0));
    }

    @Test
    void findFirstItemSkipsTheDoor() {
        // Door is not takeable, the key is.
        assertEquals(Optional.of('k'), LocationString.parse("wDk").findFirstItem());
    }

    @Test
    void findFirstItemEmptyWhenNoItem() {
        assertTrue(LocationString.parse("wD").findFirstItem().isEmpty());
    }

    @Test
    void removingItemKeepsGroundAndPlayer() {
        LocationString c = LocationString.parse("gk2");
        assertTrue(c.removeFromMiddle('k'));
        assertEquals("g2", c.render());
    }

    @Test
    void takingFromDirtLeavesDirtNotGrass() {
        // The old handler wrongly put grass back. Taking a key off dirt = dirt.
        LocationString c = LocationString.parse("_k");
        c.removeFromMiddle('k');
        assertEquals("_", c.render());
    }

    @Test
    void addingItemGoesBeforeThePlayer() {
        LocationString c = LocationString.parse("_0");
        c.addToMiddle('k');
        assertEquals("_k0", c.render());
    }

    @Test
    void removeReturnsFalseWhenNotThere() {
        LocationString c = LocationString.parse("g2");
        assertFalse(c.removeFromMiddle('k'));
        assertEquals("g2", c.render());
    }

    @Test
    void rejectsEmptyOrNull() {
        assertThrows(IllegalArgumentException.class, () -> LocationString.parse(""));
        assertThrows(IllegalArgumentException.class, () -> LocationString.parse(null));
    }
}