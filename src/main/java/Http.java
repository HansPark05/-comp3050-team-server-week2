import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;


public final class Http {

    private Http() { }

    public static void cors(HttpExchange he) {
        he.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        he.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
        he.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    public static boolean isPreflight(HttpExchange he) {
        return "OPTIONS".equalsIgnoreCase(he.getRequestMethod());
    }

    public static String param(HttpExchange he, String key) {
        return query(he).get(key);
    }

    public static Map<String, String> query(HttpExchange he) {
        Map<String, String> result = new HashMap<>();
        String raw = he.getRequestURI().getRawQuery();
        if (raw == null || raw.isEmpty()) return result;
        for (String pair : raw.split("&")) {
            int eq = pair.indexOf('=');
            if (eq < 0) {
                result.put(decode(pair), "");
            } else {
                result.put(decode(pair.substring(0, eq)), decode(pair.substring(eq + 1)));
            }
        }
        return result;
    }

    // A response length of -1 tells HttpServer there is no body.
    public static void empty(HttpExchange he, int status) throws IOException {
        he.sendResponseHeaders(status, -1);
        he.close();
    }

    private static String decode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}