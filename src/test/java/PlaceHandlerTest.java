import com.sun.net.httpserver.HttpServer;
import comp3050.server.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
// Same note as TakeHandlerTest: the server class is named "Test", so we write
// the JUnit annotation in full rather than importing it.

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Tests for PlaceHandler, again over a real HttpServer on a free port.
 */
class PlaceHandlerTest {

    private HttpServer server;
    private int port;
    private HttpClient client;
    private String token;

    @BeforeEach
    void setUp() throws IOException {
        PlayerInventories.reset();
        Test.playerY = 5;
        Test.playerX = 5;
        token = SessionManager.getInstance().createSession("PlaceTester");

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        port = server.getAddress().getPort();
        server.createContext("/place", new PlaceHandler());
        server.setExecutor(null);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    private int place(String session) throws Exception {
        String path = (session == null) ? "/place" : "/place?session=" + session;
        return client.send(
                HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString()).statusCode();
    }

    @org.junit.jupiter.api.Test
    void missingSessionReturns401() throws Exception {
        assertEquals(401, place(null));
    }

    @org.junit.jupiter.api.Test
    void invalidSessionReturns401() throws Exception {
        assertEquals(401, place("not-a-real-token"));
    }

    @org.junit.jupiter.api.Test
    void emptyInventoryReturns204() throws Exception {
        GameMap.setCell(5, 5, "g");
        assertEquals(204, place(token));
    }

    @org.junit.jupiter.api.Test
    void placesItemAndKeepsGround() throws Exception {
        PlayerInventories.forUser("PlaceTester").take(Item.KEY);
        GameMap.setCell(5, 5, "_");            // standing on dirt
        assertEquals(200, place(token));
        assertEquals("_k", GameMap.getCell(5, 5));
        assertTrue(PlayerInventories.forUser("PlaceTester").isEmpty());
    }

    @org.junit.jupiter.api.Test
    void placeKeepsThePlayerAvatar() throws Exception {
        PlayerInventories.forUser("PlaceTester").take(Item.AXE);
        GameMap.setCell(5, 5, "g0");           // grass + player 0
        assertEquals(200, place(token));
        assertEquals("ga0", GameMap.getCell(5, 5));
    }

    @org.junit.jupiter.api.Test
    void refusesWhenTileAlreadyHasItem() throws Exception {
        PlayerInventories.forUser("PlaceTester").take(Item.KEY);
        GameMap.setCell(5, 5, "gh");           // already a heart potion here
        assertEquals(204, place(token));
        assertEquals("gh", GameMap.getCell(5, 5));                 // unchanged
        assertEquals(1, PlayerInventories.forUser("PlaceTester").size());  // not used up
    }
}