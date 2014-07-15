package de.syngenio.decisiontables;

/**
 * A condition for defining a decision table.
 * Implementations must implement equals() and hashCode().
 */
public interface Condition<D>
{
    boolean test(D instance);
}
