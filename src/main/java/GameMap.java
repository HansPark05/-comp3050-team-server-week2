package comp3050.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GameMap {
    private static final String[][] map;
    private static final int height;
    private static final int width;

    static {
        String[][] loadedMap;
        int loadedHeight;
        int loadedWidth;

        try {
            InputStream is = GameMap.class.getClassLoader().getResourceAsStream("map.txt");
            if (is == null) {
                is = new FileInputStream("map.txt");
            }

            List<String[]> rows = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    rows.add(line.chars().mapToObj(c -> String.valueOf((char) c)).toArray(String[]::new));
                }
            }

            reader.close();
            loadedHeight = rows.size();
            loadedWidth = rows.isEmpty() ? 0 : rows.get(0).length;
            loadedMap = rows.toArray(new String[0][]);

            // Expand bare item chars (a/c/h/k) to "g<item>" so LocationString
            // can find them in the middle layer. The map format is one char per
            // tile, but items need a ground tile beneath them.
            for (int r = 0; r < loadedHeight; r++) {
                for (int c = 0; c < loadedMap[r].length; c++) {
                    char ch = loadedMap[r][c].charAt(0);
                    if (ch == 'a' || ch == 'c' || ch == 'h' || ch == 'k') {
                        loadedMap[r][c] = "g" + loadedMap[r][c];
                    }
                }
            }

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
        String tile = getTile(y, x);
        if (tile == null || tile.isEmpty()) return false;
        char first = tile.charAt(0);
        return first == 'B' || first == 'S' || first == 'W' || first == 'D';
    }

    public static String getTile(int y, int x) {
        return map[y][x];
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    public static void setTile(int y, int x, String tile) {
        map[y][x] = tile;
    }
}
