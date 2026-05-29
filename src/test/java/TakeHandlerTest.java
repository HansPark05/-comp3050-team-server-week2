import com.sun.net.httpserver.HttpServer;
import comp3050.server.SessionManager;
import comp3050.server.PlayerState;
import comp3050.server.GameMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class TakeHandlerTest {

    private HttpServer server;
    private int port;
    private HttpClient client;
    private String token;
    private PlayerState player;

    @BeforeEach
    void setUp() throws IOException {
        player = new PlayerState(5, 5, '0');
        token = SessionManager.getInstance().createSession("TakeTester", player);
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
        SessionManager.getInstance().invalidateUser("TakeTester");
    }

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
        GameMap.setTile(5, 5, "g");
        assertEquals(204, take(token));
    }

    @org.junit.jupiter.api.Test
    void takesItemAndKeepsGround() throws Exception {
        GameMap.setTile(5, 5, "_k");
        assertEquals(200, take(token));
        assertEquals("_", GameMap.getTile(5, 5));
        assertTrue(player.inventory.contains('k'));
    }

    @org.junit.jupiter.api.Test
    void takeKeepsThePlayerAvatar() throws Exception {
        GameMap.setTile(5, 5, "gk0");
        assertEquals(200, take(token));
        assertEquals("g0", GameMap.getTile(5, 5));
    }

    @org.junit.jupiter.api.Test
    void sameClassSwapDropsHeldItem() throws Exception {
        player.inventory.add('c');
        GameMap.setTile(5, 5, "gh");
        assertEquals(200, take(token));
        assertEquals("gc", GameMap.getTile(5, 5));
        assertEquals(1, player.inventory.size());
        assertEquals(Character.valueOf('h'), player.inventory.get(0));
    }

    @org.junit.jupiter.api.Test
    void rocksCannotBeTaken() throws Exception {
        GameMap.setTile(5, 5, "g.");
        assertEquals(204, take(token));
        assertEquals("g.", GameMap.getTile(5, 5));
    }
}