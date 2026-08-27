package eu.europa.ted.eforms.xpath;

/**
 * Where a path starts from. A path is written either relative to the current context, or anchored
 * at the root of the document, or as a search from the root at any depth.
 *
 * <p>The steps of a parsed path do not carry this, as the separator that expresses it is not a
 * step. It is recorded separately so that a path can be composed back from its steps.
 */
public enum XPathAnchor {

  /** The path starts from the current context: {@code a/b}. */
  RELATIVE(""),

  /** The path starts at the root of the document: {@code /a/b}. */
  ROOT("/"),

  /** The path searches from the root, matching at any depth: {@code //a/b}. */
  DESCENDANT_FROM_ROOT("//");

  private final String separator;

  XPathAnchor(final String separator) {
    this.separator = separator;
  }

  /** The separator that a path with this anchor begins with. */
  public String getSeparator() {
    return separator;
  }

  public boolean isAbsolute() {
    return this != RELATIVE;
  }
}
