import java.util.Optional;

/* The four takeable items from the v3 spec. Kept in one enum so the valid
 set lives in a single place (no scattered if/else checks to keep in sync) */
public enum Item {

    AXE('a', ItemClass.TOOL, "axe"),
    CYAN_POTION('c', ItemClass.DRINK, "cyan potion"),
    HEART_POTION('h', ItemClass.DRINK, "heart potion"),
    KEY('k', ItemClass.ARTIFACT, "key");

    private final char symbol;
    private final ItemClass itemClass;
    private final String displayName;

    Item(char symbol, ItemClass itemClass, String displayName) {
        this.symbol = symbol;
        this.itemClass = itemClass;
        this.displayName = displayName;
    }

    public char symbol() { return symbol; }
    public ItemClass itemClass() { return itemClass; }
    public String displayName() { return displayName; }

    // Resolve a map character to its Item, or empty if not takeable.
    public static Optional<Item> fromSymbol(char symbol) {
        for (Item item : values()) {
            if (item.symbol == symbol) return Optional.of(item);
        }
        return Optional.empty();
    }

    public static boolean isItemSymbol(char symbol) {
        return fromSymbol(symbol).isPresent();
    }

    @Override
    public String toString() {
        return displayName + " ('" + symbol + "')";
    }
}