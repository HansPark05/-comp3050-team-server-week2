import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class LocationStringTest {

    @Test
    void parsesPlainGround() {
        LocationString c = LocationString.parse("g");
        assertEquals('g', c.getGround());
        assertTrue(c.getMiddle().isEmpty());
        assertEquals("g", c.render());
    }

    @Test
    void parsesGroundPlusItem() {
        LocationString c = LocationString.parse("_k");
        assertEquals('_', c.getGround());
        assertEquals(Character.valueOf('k'), c.getMiddle().get(0));
    }

    @Test
    void parsesGroundItemAndPlayer() {
        LocationString c = LocationString.parse("gk2");
        assertEquals(Character.valueOf('k'), c.getMiddle().get(0));
        assertEquals(Optional.of('2'), c.getPlayer());
    }

    @Test
    void findFirstItemSkipsTheDoor() {
        assertEquals(Optional.of('k'), LocationString.parse("wDk").findFirstItem());
    }

    @Test
    void findFirstItemEmptyWhenNoItem() {
        assertTrue(LocationString.parse("wD").findFirstItem().isEmpty());
    }

    @Test
    void removingItemKeepsGroundAndPlayer() {
        LocationString c = LocationString.parse("gk2");
        c.removeFromMiddle('k');
        assertEquals("g2", c.render());
    }

    @Test
    void takingFromDirtLeavesDirtNotGrass() {
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
    void rejectsEmptyOrNull() {
        assertThrows(IllegalArgumentException.class, () -> LocationString.parse(""));
        assertThrows(IllegalArgumentException.class, () -> LocationString.parse(null));
    }
}