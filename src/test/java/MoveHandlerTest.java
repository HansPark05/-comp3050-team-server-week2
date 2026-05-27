import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import comp3050.server.GameMap;
import comp3050.server.PlayerState;

// These used to mutate Test.playerY / Test.playerX. After PlayerState was
// introduced, position lives on the player object, so the tests now move a
// PlayerState directly while reusing the same blocking/bounds rules from
// GameMap that MoveHandler relies on.
class MoveHandlerTest {

    // Same one-step + no-diagonal rule MoveHandler enforces.
    private boolean tryMove(PlayerState p, int dy, int dx) {
        if (Math.abs(dy) + Math.abs(dx) > 1) return false;
        int targetY = p.y + dy;
        int targetX = p.x + dx;
        if (!GameMap.isInBounds(targetY, targetX)) return false;
        if (GameMap.isBlocking(targetY, targetX)) return false;
        p.y = targetY;
        p.x = targetX;
        return true;
    }

    @Test
    void moveSouthOneStep() {
        PlayerState p = new PlayerState(5, 5, '0');
        assertTrue(tryMove(p, 1, 0));
        assertEquals(6, p.y);
        assertEquals(5, p.x);
    }

    @Test
    void moveNorthOneStep() {
        PlayerState p = new PlayerState(5, 5, '0');
        assertTrue(tryMove(p, -1, 0));
        assertEquals(4, p.y);
        assertEquals(5, p.x);
    }

    @Test
    void diagonalMoveRejected() {
        PlayerState p = new PlayerState(5, 5, '0');
        assertFalse(tryMove(p, 1, 1));
        assertEquals(5, p.y);
        assertEquals(5, p.x);
    }

    @Test
    void multiStepMoveRejected() {
        PlayerState p = new PlayerState(5, 5, '0');
        assertFalse(tryMove(p, 2, 0));
        assertEquals(5, p.y);
    }

    @Test
    void moveIntoStoneWallBlocked() {
        // (5,6) is grass, (5,7) is stone wall ('S')
        PlayerState p = new PlayerState(5, 6, '0');
        assertFalse(tryMove(p, 0, 1));
        assertEquals(6, p.x);
    }

    @Test
    void moveOutOfBoundsBlocked() {
        // Row 0 is the top brick wall row, also at the edge.
        PlayerState p = new PlayerState(0, 5, '0');
        assertFalse(tryMove(p, -1, 0));
        assertEquals(0, p.y);
    }
}
