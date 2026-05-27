import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parses and rebuilds a map cell string in layer order:
 *   <ground><middle...><player?>
 * - ground: first char (grass, dirt, wall, ...)
 * - middle: zero or more items (a,c,h,k) / doors (D,d) / bridges (b)
 * - player: at most one trailing avatar digit '0'..'9'
 * Examples: "g", "wD", "_k", "gk2". Single source of truth for cell
 * composition, so TAKE/PLACE never do raw string surgery and the ground
 * layer is always preserved.
 */
public final class LocationString {

    private final char ground;
    private final List<Character> middle;
    private Character player; // nullable

    private LocationString(char ground, List<Character> middle, Character player) {
        this.ground = ground;
        this.middle = middle;
        this.player = player;
    }

    public static LocationString parse(String cell) {
        if (cell == null || cell.isEmpty()) {
            throw new IllegalArgumentException("Cell string must be non-empty");
        }
        char ground = cell.charAt(0);
        List<Character> middle = new ArrayList<>();
        Character player = null;
        for (int i = 1; i < cell.length(); i++) {
            char c = cell.charAt(i);
            boolean isLast = (i == cell.length() - 1);
            if (isLast && Character.isDigit(c)) {
                player = c;
            } else {
                middle.add(c);
            }
        }
        return new LocationString(ground, middle, player);
    }

    public char getGround() { return ground; }
    public List<Character> getMiddle() { return new ArrayList<>(middle); }
    public Optional<Character> getPlayer() { return Optional.ofNullable(player); }

    public void setPlayer(Character digit) {
        if (digit != null && !Character.isDigit(digit)) {
            throw new IllegalArgumentException("Player avatar must be a digit, got '" + digit + "'");
        }
        this.player = digit;
    }

    // First takeable item in the middle layer, ignoring doors etc
    public Optional<Character> findFirstItem() {
        for (char c : middle) {
            if (Item.isItemSymbol(c)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    // Remove first occurrence of a symbol from the middle layer
    public boolean removeFromMiddle(char symbol) {
        for (int i = 0; i < middle.size(); i++) {
            if (middle.get(i) == symbol) {
                middle.remove(i);
                return true;
            }
        }
        return false;
    }

    // Add to the end of the middle layer (renders after ground, before player)
    public void addToMiddle(char symbol) {
        middle.add(symbol);
    }

    // Rebuild the canonical cell string: ground + middle... + player?
    public String render() {
        StringBuilder sb = new StringBuilder(1 + middle.size() + 1);
        sb.append(ground);
        for (char c : middle) {
            sb.append(c);
        }
        if (player != null) {
            sb.append(player);
        }
        return sb.toString();
    }

    @Override
    public String toString() { return render(); }
}