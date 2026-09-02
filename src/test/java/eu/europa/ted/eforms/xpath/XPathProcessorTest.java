package eu.europa.ted.eforms.xpath;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class XPathProcessorTest {
  private void testAttribute(final String fullPath, final String expectedPath,
      final String expectedAttribute) {
    XPathInfo result = XPathProcessor.parse(fullPath);

    assertEquals(expectedPath, result.getPathToLastElement());
    assertEquals(expectedAttribute, result.getAttributeName());
  }

  private void testSteps(final String xpath, String... steps) {
    XPathInfo result = XPathProcessor.parse(xpath);

    String[] parsedSteps = result.getSteps()
        .stream()
        .map(XPathStep::getStepText)
        .toArray(String[]::new);

    assertArrayEquals(steps, parsedSteps);
  }

  private String contextualize(final String context, final String xpath) {
    return XPathProcessor.contextualize(context, xpath);
  }

  @Test
  void testXPathAttribute_WithAttribute() {
    testAttribute("/a/b/@attribute", "/a/b", "attribute");
  }

  @Test
  void testXPathAttribute_WithMultipleAttributes() {
    testAttribute("/a/b[@otherAttribute = 'text']/@attribute",
        "/a/b[@otherAttribute = 'text']", "attribute");
  }

  @Test
  void testXPathAttribute_WithoutAttribute() {
    testAttribute("/a/b[@otherAttribute = 'text']",
        "/a/b[@otherAttribute = 'text']", null);
  }

  @Test
  void testXPathAttribute_WithoutPath() {
    testAttribute("@attribute", "", "attribute");
  }

  @Test
  void testXPathSteps() {
    testSteps("/a/b/c", "a", "b", "c");
    testSteps("/a/b[u/v='z']/c[x][y]", "a", "b", "c");
  }

  // START tests for contextualize
  @Test
  void testIdentical() {
    assertEquals(".", contextualize("/a/b/c", "/a/b/c"));
  }

  @Test
  void testIdentical_WithPredicates() {
    assertEquals(".[f = g]", contextualize("/a/b/c[d = e]", "/a/b/c[d = e][f = g]"));
  }

  @Test
  void testContextEmpty() {
    assertEquals("/a/b/c", contextualize("", "/a/b/c"));
  }

  @Test
  void testUnderContext() {
    assertEquals("c", contextualize("/a/b", "/a/b/c"));
  }

  @Test
  void testAboveContext() {
    assertEquals("..", contextualize("/a/b/c", "/a/b"));
  }

  @Test
  void testSibling() {
    assertEquals("../d", contextualize("/a/b/c", "/a/b/d"));
  }

  @Test
  void testTwoLevelsDifferent() {
    assertEquals("../../x/y", contextualize("/a/b/c/d", "/a/b/x/y"));
  }

  @Test
  void testAllDifferent() {
    assertEquals("../../../x/y/z", contextualize("/a/b/c/d", "/a/x/y/z"));
  }

  @Test
  void testDifferentRoot() {
    // Not realistic, as XML has a single root, but a valid result
    assertEquals("../../../x/y/z", contextualize("/a/b/c", "/x/y/z"));
  }

  @Test
  void testAttributeInXpath() {
    assertEquals("../c/@attribute", contextualize("/a/b", "/a/c/@attribute"));
  }

  @Test
  void testAttributeInContext() {
    assertEquals("../c/d", contextualize("/a/b/@attribute", "/a/b/c/d"));
  }

  @Test
  void testAttributeInBoth() {
    assertEquals("../@x", contextualize("/a/b/c/@d", "/a/b/c/@x"));
  }

  @Test
  void testAttributeInBothSame() {
    assertEquals(".", contextualize("/a/b/c/@d", "/a/b/c/@d"));
  }

  @Test
  void testPredicateInXpathLeaf() {
    assertEquals("../d[x/y = 'z']", contextualize("/a/b/c", "/a/b/d[x/y = 'z']"));
  }

  @Test
  void testPredicateBeingTheOnlyDifference() {
    assertEquals(".[x/y = 'z']", contextualize("/a/b/c", "/a/b/c[x/y = 'z']"));
  }

  @Test
  void testPredicateInContextBeingTheOnlyDifference() {
    assertEquals(".", contextualize("/a/b/c[e/f = 'z']", "/a/b/c"));
  }

  @Test
  void testPredicatesBeingTheOnlyDifferences() {
    assertEquals("..[u/v = 'w']/c[x/y = 'z']", contextualize("/a/b/c", "/a/b[u/v = 'w']/c[x/y = 'z']"));
  }

  @Test
  void testPredicateInContextLeaf() {
    assertEquals("../d", contextualize("/a/b/c[e/f = 'z']", "/a/b/d"));
  }

  @Test
  void testPredicateInBothLeaf() {
    assertEquals("../d[x = 'y']", contextualize("/a/b/c[e = 'f']", "/a/b/d[x = 'y']"));
  }

  @Test
  void testPredicateInXpathMiddle() {
    assertEquals("..[x/y = 'z']/d", contextualize("/a/b/c", "/a/b[x/y = 'z']/d"));
  }

  @Test
  void testPredicateInContextMiddle() {
    assertEquals("../d", contextualize("/a/b[e/f = 'z']/c", "/a/b/d"));
  }

  @Test
  void testPredicateSameInBoth() {
    assertEquals("../d", contextualize("/a/b[e/f = 'z']/c", "/a/b[e/f = 'z']/d"));
  }

  @Test
  void testPredicateDifferentOnSameElement() {
    assertEquals("../../b[x = 'y']/d", contextualize("/a/b[e = 'f']/c", "/a/b[x = 'y']/d"));
  }

  @Test
  void testPredicateDifferent() {
    assertEquals(".[x = 'y']/d", contextualize("/a/b[e = 'f']/c", "/a/b/c[x = 'y']/d"));
  }

  @Test
  void testPredicateMoreInXpath() {
    assertEquals("..[f]/c/d", contextualize("/a/b[e]/c", "/a/b[e][f]/c/d"));
  }

  @Test
  void testPredicateMoreInContext() {
    assertEquals("d", contextualize("/a/b[e][f]/c", "/a/b[e]/c/d"));
  }

  @Test
  void testSeveralPredicatesIdentical() {
    assertEquals("d", contextualize("/a/b[e][f]/c", "/a/b[e][f]/c/d"));
  }

  @Test
  void testSeveralPredicatesOneDifferent() {
    assertEquals("../../b[e][x]/c/d", contextualize("/a/b[e][f]/c", "/a/b[e][x]/c/d"));
  }
  // END tests for contextualize

  @Test
  void testAddAxis() {
    assertEquals("preceding::b/c", XPathProcessor.addAxis("preceding", "b/c"));
    assertEquals("descendant::b/c", XPathProcessor.addAxis("descendant", "../../b/c"));
  }

  @Test
  void testAddAxis_MustPreserveThePredicates() {
    assertEquals("preceding::b[x = 'y']/c",
        XPathProcessor.addAxis("preceding", "b[x = 'y']/c"));
    assertEquals("preceding::b/c[x = 'y']",
        XPathProcessor.addAxis("preceding", "b/c[x = 'y']"));
    assertEquals("preceding::b[e][f]/c[g]",
        XPathProcessor.addAxis("preceding", "b[e][f]/c[g]"));
    assertEquals("descendant::b[x = 'y']/c",
        XPathProcessor.addAxis("descendant", "../../b[x = 'y']/c"));
  }

  @Test
  void testAddAxis_MustAimTheStepTheAxisLandsOn() {
    assertEquals("preceding::node()", XPathProcessor.addAxis("preceding", "."));
    assertEquals("preceding::node()", XPathProcessor.addAxis("preceding", ".."));
    assertEquals("preceding::node()", XPathProcessor.addAxis("preceding", "../.."));
    assertEquals("preceding::b", XPathProcessor.addAxis("preceding", "./b"));
    assertEquals("preceding::node()[x]/b", XPathProcessor.addAxis("preceding", ".[x]/b"));
    assertEquals("preceding::node()[x]/b", XPathProcessor.addAxis("preceding", "..[x]/b"));
    assertEquals("preceding::b/c", XPathProcessor.addAxis("preceding", "child::b/c"));
    assertEquals("preceding::b/c", XPathProcessor.addAxis("preceding", "following::b/c"));
    assertEquals("preceding::text()/b", XPathProcessor.addAxis("preceding", "text()/b"));
  }

  @Test
  void testAddAxis_MustKeepAStepThatCannotBeAimed() {
    assertEquals("preceding::node()/@x", XPathProcessor.addAxis("preceding", "@x"));
    assertEquals("preceding::node()/$var/b", XPathProcessor.addAxis("preceding", "$var/b"));
    assertEquals("preceding::node()/doc('x')/b", XPathProcessor.addAxis("preceding", "doc('x')/b"));
    assertEquals("preceding::node()/id('x')/b", XPathProcessor.addAxis("preceding", "id('x')/b"));
    assertEquals("preceding::node()/(a | b)/c", XPathProcessor.addAxis("preceding", "(a | b)/c"));
    assertEquals("preceding::node()/namespace::x",
        XPathProcessor.addAxis("preceding", "namespace::x"));
  }

  @Test
  void testJoin_MustNotCancelStepsThatCarryPredicates() {
    // A step going somewhere and a step coming back cancel out, but a predicate on either of them
    // is a condition on the result, so the two have to stand as they were written.
    assertEquals("a/..[x]/b", XPathProcessor.join("a", "..[x]/b"));
    assertEquals("a[x]/../b", XPathProcessor.join("a[x]", "../b"));
    assertEquals("a[x]/..[y]/b", XPathProcessor.join("a[x]", "..[y]/b"));
    assertEquals("a/b[x]/../c", XPathProcessor.join("a/b[x]", "../c"));

    // Where the cancelling pair carries no predicate, it still cancels, whatever the steps around
    // it are carrying.
    assertEquals("a[x]/c", XPathProcessor.join("a[x]/b", "../c"));
    assertEquals("b", XPathProcessor.join("a", "../b"));
    assertEquals("c", XPathProcessor.join("a/b", "../../c"));
  }

  @Test
  void testJoin_MustNotRewriteTheStepsItWasGiven() {
    assertEquals("child::a/attribute::x", XPathProcessor.join("child::a", "attribute::x"));
    assertEquals("self::node()/b", XPathProcessor.join("self::node()", "b"));
  }

  @Test
  void testAddAxis_MustReadAnAbsolutePathFromTheContext() {
    assertEquals("preceding::a/b", XPathProcessor.addAxis("preceding", "/a/b"));
    assertEquals("preceding::a/b", XPathProcessor.addAxis("preceding", "//a/b"));
    assertEquals("preceding::a[x]/b", XPathProcessor.addAxis("preceding", "/a[x]/b"));
  }

  @Test
  void testJoin() {
    assertEquals("a/b/c/d", XPathProcessor.join("a/b", "c/d"));
    assertEquals("a/x/y", XPathProcessor.join("a/b/c", "../../x/y"));
  }

  @Test
  void testJoinPreservesPredicates() {
    assertEquals("a/b/c[x = 'y']/d", XPathProcessor.join("a/b", "c[x = 'y']/d"));
    assertEquals("a[x = 'y']/b/c", XPathProcessor.join("a[x = 'y']/b", "c"));
    assertEquals("a[x = 'y']/b[p]/c[q]", XPathProcessor.join("a[x = 'y']", "b[p]/c[q]"));
  }

  @Test
  void testJoinPreservesTheLeadingSeparator() {
    assertEquals("a/b/c", XPathProcessor.join("a/b", "c"));
    assertEquals("/a/b/c", XPathProcessor.join("/a/b", "c"));
    assertEquals("/a/b[x = 'y']/c", XPathProcessor.join("/a/b[x = 'y']", "c"));

    // "/a" and "//a" do not select the same thing, so the separator is kept as it was written.
    assertEquals("//a/b", XPathProcessor.join("//a", "b"));

    // When the back-steps consume the whole of the first part, the result is still anchored at the
    // root, and must not become a descendant search.
    assertEquals("/b", XPathProcessor.join("/", "b"));
    assertEquals("/b", XPathProcessor.join("/a", "../b"));
    assertEquals("b", XPathProcessor.join("a", "../b"));
  }

  /**
   * A path beginning with "//" matches at any depth, so its first step cannot be cancelled against
   * a parent step: "//a/.." selects the parents of every a element, which is not the root.
   */
  @Test
  void testJoinKeepsTheFirstStepOfADescendantSearch() {
    assertEquals("//a/..", XPathProcessor.join("//a", ".."));
    assertEquals("//a/../b", XPathProcessor.join("//a", "../b"));
    assertEquals("//a/b", XPathProcessor.join("//a", "b"));
  }

  /**
   * The anchor is read from the parse tree, not from the start of the input, so anything the lexer
   * skips before the path does not hide it. A comment is valid XPath and is skipped.
   */
  @Test
  void testJoinSeesTheAnchorPastAComment() {
    assertEquals("/a/b", XPathProcessor.join("(: a comment :) /a", "b"));
    assertEquals("//a/b", XPathProcessor.join("(: a comment :) //a", "b"));
    assertEquals("a/b", XPathProcessor.join("(: a comment :) a", "b"));
  }

  /** A path inside a predicate has its own anchor, which is not the anchor of the path. */
  @Test
  void testJoinTakesTheAnchorOfTheOuterPath() {
    assertEquals("a[/b]/c", XPathProcessor.join("a[/b]", "c"));
    assertEquals("/a[b]/c", XPathProcessor.join("/a[b]", "c"));
  }

  @Test
  void testJoinResolvingToWhereItStarted() {
    assertEquals("/", XPathProcessor.join("/a", ".."));
    assertEquals("/", XPathProcessor.join("/a/b", "../.."));
    assertEquals(".", XPathProcessor.join("a", ".."));
    assertEquals(".", XPathProcessor.join("a/b", "../.."));
  }
}
