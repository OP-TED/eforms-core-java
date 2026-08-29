package eu.europa.ted.eforms.xpath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class XPathStepTest {
  @Test
  void testEquals() {
    XPathStep a = buildStep("foo", "x=1", "y=2");
    XPathStep b = buildStep("foo", "y=2", "x=1");

    assertTrue(a.equals(b));
    assertTrue(b.equals(a));
  }

  @Test
  void testComparison_Equals() {
    XPathStep a = buildStep("foo", "x=1", "y=2");
    XPathStep b = buildStep("foo", "y=2", "x=1");

    assertTrue(a.isTheSameAs(b));
    assertTrue(b.isTheSameAs(a));
  }

  @Test
  void testComparison_DifferentElement() {
    XPathStep a = buildStep("foo", "a");
    XPathStep b = buildStep("bar", "a");

    assertFalse(a.isTheSameAs(b));

    assertFalse(a.isSameAsOrNarrowerThan(b));
  }

  @Test
  void testComparison_MorePredicates() {
    XPathStep a = buildStep("foo", "x=1");
    XPathStep b = buildStep("foo", "x=1", "y=2");

    assertTrue(a.isTheSameAs(b));

    assertTrue(b.isSameAsOrNarrowerThan(a));
  }

  @Test
  void testComparison_LessPredicates() {
    XPathStep a = buildStep("foo", "x=1", "y=2");
    XPathStep b = buildStep("foo", "x=1");

    assertFalse(a.isTheSameAs(b));

    assertFalse(b.isSameAsOrNarrowerThan(a));
  }

  @Test
  void testComparison_DifferentPredicate() {
    XPathStep a = buildStep("foo", "x=1", "y=2");
    XPathStep b = buildStep("foo", "x=1", "zzz");

    assertFalse(a.isTheSameAs(b));

    assertFalse(a.isSameAsOrNarrowerThan(b));
  }

  @Test
  void testComparison_NoPredicates() {
    XPathStep a = buildStep("foo", "x=1", "y=2");
    XPathStep b = buildStep("foo");

    assertFalse(a.isTheSameAs(b));

    assertTrue(a.isSameAsOrNarrowerThan(b));
  }

  @Test
  void testComparison_AddPredicates() {
    XPathStep a = buildStep("foo");
    XPathStep b = buildStep("foo", "x=1", "y=2");

    assertTrue(a.isTheSameAs(b));

    assertTrue(b.isSameAsOrNarrowerThan(a));
  }

  @Test
  void testSpellingsAreReadAsStepsOnAnAxis() {
    // The spelling is not rewritten, but the step knows the axis behind it, which is what lets it
    // be looked for along another one.
    assertEquals("preceding::b/c", XPathProcessor.addAxis("preceding", "b/c"));
    assertEquals("preceding::b/c", XPathProcessor.addAxis("preceding", "child::b/c"));
    assertEquals("preceding::text()/b", XPathProcessor.addAxis("preceding", "text()/b"));
    assertEquals("preceding::node()", XPathProcessor.addAxis("preceding", ".."));
    assertEquals("preceding::node()", XPathProcessor.addAxis("preceding", "."));
    assertEquals("preceding::node()", XPathProcessor.addAxis("preceding", "self::node()"));
  }

  @Test
  void testAStepIsReadTheSameWayHoweverItIsSpelled() {
    // Each pair says the same thing, the one spelled out and the other short, so each pair has to
    // come out the same.
    assertEquals(XPathProcessor.addAxis("preceding", "./b"),
        XPathProcessor.addAxis("preceding", "self::node()/b"));
    assertEquals(XPathProcessor.addAxis("preceding", "../b"),
        XPathProcessor.addAxis("preceding", "parent::node()/b"));
    assertEquals(XPathProcessor.addAxis("preceding", ".[x]/b"),
        XPathProcessor.addAxis("preceding", "self::node()[x]/b"));
    assertEquals(XPathProcessor.addAxis("preceding", "..[x]/b"),
        XPathProcessor.addAxis("preceding", "parent::node()[x]/b"));
    assertEquals(XPathProcessor.addAxis("preceding", "b/c"),
        XPathProcessor.addAxis("preceding", "child::b/c"));

    // Naming something is not moving about, even on the same axes.
    assertEquals("preceding::b/c", XPathProcessor.addAxis("preceding", "self::b/c"));
    assertEquals("preceding::b/c", XPathProcessor.addAxis("preceding", "parent::b/c"));
    assertEquals("preceding::text()/b", XPathProcessor.addAxis("preceding", "self::text()/b"));
  }

  @Test
  void testExpressionsAreNotLookedForAlongAnAxis() {
    assertEquals("preceding::node()/$var/b", XPathProcessor.addAxis("preceding", "$var/b"));
    assertEquals("preceding::node()/doc('x')/b", XPathProcessor.addAxis("preceding", "doc('x')/b"));
    assertEquals("preceding::node()/(a | b)/c", XPathProcessor.addAxis("preceding", "(a | b)/c"));
  }

  @Test
  void testAStepKeepsTheSpellingItWasReadFrom() {
    assertEquals("b", firstStepOf("b/c").getStepText());
    assertEquals("@x", firstStepOf("@x").getStepText());
    assertEquals("..", firstStepOf("..").getStepText());
    assertEquals(".", firstStepOf(".").getStepText());
    assertEquals("preceding::b", firstStepOf("preceding::b/c").getStepText());

    // A spelled-out step is not rewritten, even where a shorter spelling says the same thing.
    assertEquals("child::b", firstStepOf("child::b/c").getStepText());
    assertEquals("attribute::x", firstStepOf("attribute::x").getStepText());
    assertEquals("self::node()", firstStepOf("self::node()").getStepText());
    assertEquals("parent::node()", firstStepOf("parent::node()").getStepText());
  }

  @Test
  void testAStepReadFromAPathIsTheSameAsOneBuiltFromItsText() {
    final XPathStep read = firstStepOf("a[x = 1]");
    final XPathStep built = new XPathStep("a", Arrays.asList("[x = 1]"));

    assertEquals(built, read);
    assertEquals(read, built);
    assertEquals(built.hashCode(), read.hashCode());
    assertEquals(0, read.compareTo(built));
    assertTrue(read.isTheSameAs(built));
  }

  private XPathStep firstStepOf(final String path) {
    return XPathProcessor.parse(path).getSteps().get(0);
  }

  private XPathStep buildStep(String elt, String... predicates) {
    return new XPathStep(elt, Arrays.asList(predicates));
  }
}
