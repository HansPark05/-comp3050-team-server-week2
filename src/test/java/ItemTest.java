import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Checks the v3 item table (symbol -> item -> class) and the symbol lookup
// that the rest of the item code relies on.
class ItemTest {

    @Test
    void axeIsTool() {
        Item axe = Item.fromSymbol('a').orElseThrow();
        assertEquals('a', axe.symbol());
        assertEquals(ItemClass.TOOL, axe.itemClass());
        assertEquals("axe", axe.displayName());
    }

    @Test
    void cyanPotionIsDrink() {
        assertEquals(ItemClass.DRINK, Item.fromSymbol('c').orElseThrow().itemClass());
    }

    @Test
    void heartPotionIsDrink() {
        assertEquals(ItemClass.DRINK, Item.fromSymbol('h').orElseThrow().itemClass());
    }

    @Test
    void keyIsArtifact() {
        assertEquals(ItemClass.ARTIFACT, Item.fromSymbol('k').orElseThrow().itemClass());
    }

    @Test
    void bothPotionsShareTheDrinkClass() {
        // This is what makes the swap rule fire between the two potions.
        assertEquals(Item.fromSymbol('c').orElseThrow().itemClass(),
                     Item.fromSymbol('h').orElseThrow().itemClass());
    }

    @Test
    void nonItemsAreRejected() {
        assertTrue(Item.fromSymbol('g').isEmpty());   // grass
        assertTrue(Item.fromSymbol('_').isEmpty());   // dirt
        assertTrue(Item.fromSymbol('D').isEmpty());   // closed door
        assertTrue(Item.fromSymbol('.').isEmpty());   // one rock
        assertTrue(Item.fromSymbol('2').isEmpty());   // player avatar
    }

    @Test
    void isItemSymbolAgreesWithFromSymbol() {
        for (char c = 0; c < 128; c++) {
            assertEquals(Item.fromSymbol(c).isPresent(), Item.isItemSymbol(c));
        }
    }
}