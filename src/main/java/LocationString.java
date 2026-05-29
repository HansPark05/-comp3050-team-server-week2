import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/* Parses a map cell string into layers: ground + middle + player
 Examples: "g", "wD", "_k", "gk2"
 TAKE/PLACE use this so the ground tile and player avatar are always kept */
public final class LocationString {

    private final char ground;
    private final List<Character> middle;
    private Character player;

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
            throw new IllegalArgumentException("Player avatar must be a digit");
        }
        this.player = digit;
    }

    // First takeable item in the middle layer, skipping doors etc.
    public Optional<Character> findFirstItem() {
        for (char c : middle) {
            if (Item.isItemSymbol(c)) return Optional.of(c);
        }
        return Optional.empty();
    }

    public boolean removeFromMiddle(char symbol) {
        for (int i = 0; i < middle.size(); i++) {
            if (middle.get(i) == symbol) {
                middle.remove(i);
                return true;
            }
        }
        return false;
    }

    public void addToMiddle(char symbol) {
        middle.add(symbol);
    }

    public String render() {
        StringBuilder sb = new StringBuilder(1 + middle.size() + 1);
        sb.append(ground);
        for (char c : middle) sb.append(c);
        if (player != null) sb.append(player);
        return sb.toString();
    }

    @Override
    public String toString() { return render(); }
}