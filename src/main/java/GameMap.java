import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GameMap {

    private static final char[][] map;
    private static final int height;
    private static final int width;

    static {
        char[][] loadedMap;
        int loadedHeight;
        int loadedWidth;
        try {
            InputStream is = GameMap.class.getClassLoader().getResourceAsStream("map.txt");
            if (is == null) {
                is = new FileInputStream("map.txt");
            }
            List<char[]> rows = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    rows.add(line.toCharArray());
                }
            }
            reader.close();
            loadedHeight = rows.size();
            loadedWidth = rows.isEmpty() ? 0 : rows.get(0).length;
            loadedMap = rows.toArray(new char[0][]);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
        map = loadedMap;
        height = loadedHeight;
        width = loadedWidth;
    }

    public static boolean isInBounds(int y, int x) {
        return y >= 0 && y < height && x >= 0 && x < width;
    }

    public static boolean isBlocking(int y, int x) {
        char tile = getTile(y, x);
        return tile == 'B' || tile == 'S' || tile == 'W' || tile == 'D';
    }

    public static char getTile(int y, int x) {
        return map[y][x];
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    // Allow modifying a tile (e.g. when player picks up an item)
    public static void setTile(int y, int x, char tile) {
        map[y][x] = tile;
    }
}
