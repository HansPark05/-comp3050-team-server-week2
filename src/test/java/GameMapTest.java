import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import comp3050.server.GameMap;

class GameMapTest {

    @Test
    void mapLoadsWithCorrectDimensions() {
        assertEquals(20, GameMap.getHeight());
        assertEquals(20, GameMap.getWidth());
    }

    @Test
    void isInBoundsValidPositions() {
        assertTrue(GameMap.isInBounds(0, 0));
        assertTrue(GameMap.isInBounds(5, 5));
        assertTrue(GameMap.isInBounds(19, 19));
    }

    @Test
    void isInBoundsOutOfBoundsNegative() {
        assertFalse(GameMap.isInBounds(-1, 0));
        assertFalse(GameMap.isInBounds(0, -1));
        assertFalse(GameMap.isInBounds(-1, -1));
    }

    @Test
    void isInBoundsOutOfBoundsPastSize() {
        assertFalse(GameMap.isInBounds(20, 0));
        assertFalse(GameMap.isInBounds(0, 20));
        assertFalse(GameMap.isInBounds(20, 20));
    }

    @Test
    void getTileGrass() {
        // (5, 5) is 'g' in map.txt
        assertEquals("g", GameMap.getTile(1, 1));
    }

    @Test
    void getTileBrickWall() {
        assertEquals("B", GameMap.getTile(0, 0));
    }

    @Test
    void getTileWater() {
        // Row 8, col 10 is water
        assertEquals("W", GameMap.getTile(8, 10));
    }

    @Test
    void getTileStoneWall() {
        assertEquals("S", GameMap.getTile(14, 1));
    }

    @Test
    void isBlockingBrickWall() {
        assertTrue(GameMap.isBlocking(0, 0));
    }

    @Test
    void isBlockingStoneWall() {
        assertTrue(GameMap.isBlocking(14, 1));
    }

    @Test
    void isBlockingWater() {
        assertTrue(GameMap.isBlocking(8, 10));
    }

    @Test
    void isBlockingGrassWalkable() {
        assertFalse(GameMap.isBlocking(1, 1));
    }

    @Test
    void isBlockingDirtWalkable() {
        assertFalse(GameMap.isBlocking(17, 5));
    }

    @Test
    void isBlockingTreeWalkable() {
        assertFalse(GameMap.isBlocking(10, 6));
    }

    @Test
    void playerStartPositionIsWalkable() {
        assertFalse(GameMap.isBlocking(1, 6));
        assertTrue(GameMap.isInBounds(1, 6));
        assertEquals("g", GameMap.getTile(1, 6));
    }
}
