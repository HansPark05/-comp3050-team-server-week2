/*
 * The class (category) of a takeable item:
 * Items in the same class are mutually exclusive in an inventory (taking one
 * swaps out the other). The spec defines exactly three classes.
 */
public enum ItemClass {
    TOOL,
    DRINK,
    ARTIFACT
}