package eu.europa.ted.eforms.xpath;

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

    assertFalse(a.isSimilarTo(b));
  }

  @Test
  void testComparison_MorePredicates() {
    XPathStep a = buildStep("foo", "x=1");
    XPathStep b = buildStep("foo", "x=1", "y=2");

    assertTrue(a.isTheSameAs(b));

    assertTrue(a.isSimilarTo(b));
  }

  @Test
  void testComparison_LessPredicates() {
    XPathStep a = buildStep("foo", "x=1", "y=2");
    XPathStep b = buildStep("foo", "x=1");

    assertFalse(a.isTheSameAs(b));

    assertFalse(a.isSimilarTo(b));
  }

  @Test
  void testComparison_DifferentPredicate() {
    XPathStep a = buildStep("foo", "x=1", "y=2");
    XPathStep b = buildStep("foo", "x=1", "zzz");

    assertFalse(a.isTheSameAs(b));

    assertFalse(a.isSimilarTo(b));
  }

  @Test
  void testComparison_NoPredicates() {
    XPathStep a = buildStep("foo", "x=1", "y=2");
    XPathStep b = buildStep("foo");

    assertFalse(a.isTheSameAs(b));

    assertTrue(a.isSimilarTo(b));
  }

  @Test
  void testComparison_AddPredicates() {
    XPathStep a = buildStep("foo");
    XPathStep b = buildStep("foo", "x=1", "y=2");

    assertTrue(a.isTheSameAs(b));

    assertTrue(a.isSimilarTo(b));
  }

  private XPathStep buildStep(String elt, String... predicates) {
    return new XPathStep(elt, Arrays.asList(predicates));
  }
}
