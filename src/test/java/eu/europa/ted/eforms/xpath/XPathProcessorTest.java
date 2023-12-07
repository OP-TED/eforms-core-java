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
    assertEquals(".[d = e][f = g]", contextualize("/a/b/c[d = e]", "/a/b/c[d = e][f = g]"));
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
    assertEquals("..[e][f]/c/d", contextualize("/a/b[e]/c", "/a/b[e][f]/c/d"));
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
  void testJoin() {
    assertEquals("a/b/c/d", XPathProcessor.join("a/b", "c/d"));
    assertEquals("a/x/y", XPathProcessor.join("a/b/c", "../../x/y"));
  }
}
