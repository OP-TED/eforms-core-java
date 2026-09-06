package eu.europa.ted.eforms.xpath;

/**
 * How much a joined path is shortened.
 *
 * <p>
 * Where a path goes down to a step and immediately comes back up, the two steps can be removed:
 * {@code a/b/../c} becomes {@code a/c}. The result is shorter but it does not mean the same thing.
 * The long form selects nothing when {@code b} is absent from the document, whereas the short form
 * selects {@code c} whether {@code b} is there or not.
 *
 * <p>
 * That difference matters to some callers and not to others, so it is theirs to choose.
 */
public enum Simplification {
  /**
   * Every step is kept as it was written. The path stays longer, and it selects nothing unless
   * every step along the way is present in the document.
   */
  NONE,

  /**
   * A step and a following parent step cancel each other out, unless the step carries a predicate.
   * Whatever the predicate says is therefore kept, but the requirement that an unpredicated step be
   * present is lost.
   */
  PRESERVE_PREDICATES,

  /**
   * A step and a following parent step cancel each other out even when the step carries a
   * predicate. The predicate goes with the step, so both the requirement that the step be present
   * and whatever its predicate said are lost.
   */
  FULL
}
