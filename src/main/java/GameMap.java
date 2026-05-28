import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/*
 * The game map. Loaded once from map.txt when the server starts.
 *
 * v3 change: a cell used to be a single char (char[][]). v3 needs layered cells
 * such as "wD" (boards + closed door) or "gk2" (grass + key + player 2), which a
 * single char cannot hold, so the map is now String[][]. The old methods still
 * work the same: getTile returns the ground layer (first character) so
 * MoveHandler and InfoHandler do not need to change. getCell/setCell are the new
 * layered access used by TAKE and PLACE. map.txt is still one character per cell,
 * so existing maps load unchanged.
 */
public class GameMap {

    private static final String[][] cells;
    private static final int height;
    private static final int width;

    // Tiles that stop movement, from the v3 floor-tile table:
    // brick wall, stone wall, water, closed door.
    private static final String BLOCKING_TILES = "BSWD";

    static {
        String[][] loadedCells;
        int loadedHeight;
        int loadedWidth;
        try {
            InputStream is = GameMap.class.getClassLoader().getResourceAsStream("map.txt");
            if (is == null) {
                is = new FileInputStream("map.txt");
            }
            List<String[]> rows = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                // Split the line into one-character cell strings.
                String[] row = new String[line.length()];
                for (int i = 0; i < line.length(); i++) {
                    row[i] = String.valueOf(line.charAt(i));
                }
                rows.add(row);
            }
            reader.close();
            loadedHeight = rows.size();
            loadedWidth = rows.isEmpty() ? 0 : rows.get(0).length;
            loadedCells = rows.toArray(new String[0][]);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
        cells = loadedCells;
        height = loadedHeight;
        width = loadedWidth;
    }

    private GameMap() { }

    public static boolean isInBounds(int y, int x) {
        return y >= 0 && y < height && x >= 0 && x < width;
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    // The full layered cell, e.g. "g", "wD", "gk2".
    public static String getCell(int y, int x) {
        return cells[y][x];
    }

    // Replace the whole layered cell.
    public static void setCell(int y, int x, String cell) {
        if (cell == null || cell.isEmpty()) {
            throw new IllegalArgumentException("Cell must have at least a ground tile");
        }
        cells[y][x] = cell;
    }

    // Ground layer = first character. Kept so existing callers still work.
    public static char getTile(int y, int x) {
        String cell = cells[y][x];
        return cell.isEmpty() ? ' ' : cell.charAt(0);
    }

    // Old single-character set. TAKE/PLACE use setCell so the layers survive.
    public static void setTile(int y, int x, char tile) {
        cells[y][x] = String.valueOf(tile);
    }

    // Blocked if any layer of the cell is a blocking tile.
    public static boolean isBlocking(int y, int x) {
        String cell = cells[y][x];
        for (int i = 0; i < cell.length(); i++) {
            if (BLOCKING_TILES.indexOf(cell.charAt(i)) >= 0) {
                return true;
            }
        }
        return false;
    }
}