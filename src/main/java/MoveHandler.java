import java.io.IOException;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import comp3050.server.SessionManager;

public class MoveHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange he) throws IOException {
        he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        he.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");

        if ("OPTIONS".equals(he.getRequestMethod())) {
            he.sendResponseHeaders(204, -1);
            return;
        }

        String query = he.getRequestURI().getQuery();
        int dy = 0;
        int dx = 0;
        String session = null;

        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    if (pair[0].equals("dy")) {
                        dy = Integer.parseInt(pair[1]);
                    } else if (pair[0].equals("dx")) {
                        dx = Integer.parseInt(pair[1]);
                    } else if (pair[0].equals("session")) {
                        session = pair[1];
                    }
                }
            }
        }

        if (session == null || SessionManager.getInstance().getUser(session) == null) {
            he.sendResponseHeaders(401, -1);
            he.close();
            return;
        }

        if (Math.abs(dy) + Math.abs(dx) > 1) {
            send204(he);
            return;
        }

        int targetY = Test.playerY + dy;
        int targetX = Test.playerX + dx;

        if (!GameMap.isInBounds(targetY, targetX)) {
            send204(he);
            return;
        }

        if (GameMap.isBlocking(targetY, targetX)) {
            send204(he);
            return;
        }

        Test.playerY = targetY;
        Test.playerX = targetX;

        String response = String.format("{\"y\": %d, \"x\": %d}", Test.playerY, Test.playerX);
        he.getResponseHeaders().set("Content-Type", "application/json");
        he.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = he.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private void send204(HttpExchange he) throws IOException {
        he.sendResponseHeaders(204, -1);
        he.close();
    }
}
