import com.sun.net.httpserver.HttpServer;
import comp3050.server.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
// NOTE: we do NOT import org.junit.jupiter.api.Test, because the server's entry
// class is also called "Test". We write the annotation in full instead.

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Tests for TakeHandler. We start a real HttpServer on a free port and send it
 * real /take requests, the same way the supplied client would, then check the
 * status code and what happened to the map and the inventory.
 */
class TakeHandlerTest {

    private HttpServer server;
    private int port;
    private HttpClient client;
    private String token;

    @BeforeEach
    void setUp() throws IOException {
        PlayerInventories.reset();
        Test.playerY = 5;
        Test.playerX = 5;
        token = SessionManager.getInstance().createSession("TakeTester");

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        port = server.getAddress().getPort();
        server.createContext("/take", new TakeHandler());
        server.setExecutor(null);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    // Send a /take request and return the status code.
    private int take(String session) throws Exception {
        String path = (session == null) ? "/take" : "/take?session=" + session;
        return client.send(
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString()).statusCode();
    }

    @org.junit.jupiter.api.Test
    void missingSessionReturns401() throws Exception {
        assertEquals(401, take(null));
    }

    @org.junit.jupiter.api.Test
    void invalidSessionReturns401() throws Exception {
        assertEquals(401, take("not-a-real-token"));
    }

    @org.junit.jupiter.api.Test
    void noItemOnTileReturns204() throws Exception {
        GameMap.setCell(5, 5, "g");
        assertEquals(204, take(token));
    }

    @org.junit.jupiter.api.Test
    void takesItemAndKeepsGround() throws Exception {
        GameMap.setCell(5, 5, "_k");           // key on dirt
        assertEquals(200, take(token));
        assertEquals("_", GameMap.getCell(5, 5));   // dirt stays, key gone
        assertTrue(PlayerInventories.forUser("TakeTester").hasKey());
    }

    @org.junit.jupiter.api.Test
    void takeKeepsThePlayerAvatar() throws Exception {
        GameMap.setCell(5, 5, "gk0");          // grass + key + player 0
        assertEquals(200, take(token));
        assertEquals("g0", GameMap.getCell(5, 5));
    }

    @org.junit.jupiter.api.Test
    void sameClassSwapDropsHeldItem() throws Exception {
        PlayerInventories.forUser("TakeTester").take(Item.CYAN_POTION);
        GameMap.setCell(5, 5, "gh");           // heart potion (also a drink)
        assertEquals(200, take(token));
        assertEquals("gc", GameMap.getCell(5, 5));  // cyan dropped here
        assertEquals(1, PlayerInventories.forUser("TakeTester").size());
        assertEquals(Item.HEART_POTION, PlayerInventories.forUser("TakeTester").view().get(0));
    }

    @org.junit.jupiter.api.Test
    void rocksCannotBeTaken() throws Exception {
        // The old handler wrongly took rocks. They are floor decoration, not items.
        GameMap.setCell(5, 5, "g.");
        assertEquals(204, take(token));
        assertEquals("g.", GameMap.getCell(5, 5));
    }
}