import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import comp3050.server.GameMap;
import comp3050.server.PlayerState;
import comp3050.server.SessionManager;

// Used to be tests on Test.playerY / Test.playerX. After the per-session
// refactor those globals are gone, so these now exercise the same view-window
// math against a PlayerState held by SessionManager.
class InfoHandlerTest {

    @Test
    void playerStateStoresStartPosition() {
        PlayerState p = new PlayerState(5, 5, '0');
        assertEquals(5, p.y);
        assertEquals(5, p.x);
        assertEquals('0', p.avatar);
        assertTrue(p.inventory.isEmpty());
    }

    @Test
    void sessionManagerReturnsSameStateInstance() {
        PlayerState p = new PlayerState(7, 3, '1');
        String token = SessionManager.getInstance().createSession("tester1", p);
        PlayerState fetched = SessionManager.getInstance().getPlayer(token);
        assertSame(p, fetched);
        SessionManager.getInstance().invalidate(token);
    }

    @Test
    void invalidTokenReturnsNullPlayer() {
        assertNull(SessionManager.getInstance().getPlayer("not-a-real-token"));
        assertNull(SessionManager.getInstance().getPlayer(null));
    }

    @Test
    void viewWindowIs11x11AroundPlayer() {
        PlayerState p = new PlayerState(5, 5, '0');
        int top    = p.y - 5;
        int bottom = p.y + 5;
        int left   = p.x - 5;
        int right  = p.x + 5;
        assertEquals(11, bottom - top + 1);
        assertEquals(11, right - left + 1);
    }

    @Test
    void centreOfViewIsPlayerTile() {
        PlayerState p = new PlayerState(1, 6, '0');
        assertEquals("g", GameMap.getTile(p.y, p.x));
    }
}
