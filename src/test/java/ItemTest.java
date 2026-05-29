import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(Item.fromSymbol('c').orElseThrow().itemClass(),
                     Item.fromSymbol('h').orElseThrow().itemClass());
    }

    @Test
    void nonItemsAreRejected() {
        assertTrue(Item.fromSymbol('g').isEmpty());
        assertTrue(Item.fromSymbol('.').isEmpty());
        assertTrue(Item.fromSymbol('D').isEmpty());
        assertTrue(Item.fromSymbol('2').isEmpty());
    }

    @Test
    void isItemSymbolAgreesWithFromSymbol() {
        for (char c = 0; c < 128; c++) {
            assertEquals(Item.fromSymbol(c).isPresent(), Item.isItemSymbol(c));
        }
    }
}