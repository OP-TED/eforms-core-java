package eu.europa.ted.eforms.xpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

public class XPathStep implements Comparable<XPathStep> {
  private final String stepText;
  private final List<String> predicates;

  /**
   * Whether the step was written as a bare node test, as the parser read it from the grammar. It
   * follows from the step itself rather than being a fact of its own, so it takes no part in
   * equality or ordering.
   */
  private final boolean nodeTest;

  /**
   * Builds a step that names the nodes to look for, which is what a step written as an element name
   * does. Steps read from a path are built by the parser instead, which tells the two apart from
   * the grammar.
   */
  public XPathStep(String stepText, List<String> predicates) {
    this(stepText, predicates, true);
  }

  /**
   * Reserved for the parser: it is the only place where the form of a step is known for certain.
   */
  XPathStep(String stepText, List<String> predicates, boolean nodeTest) {
    this.stepText = StringUtils.strip(stepText);
    this.predicates = predicates;
    this.nodeTest = nodeTest;
  }

  public String getStepText() {
    return stepText;
  }

  public List<String> getPredicates() {
    return predicates;
  }

  public String getPredicateText() {
    return String.join("", predicates);
  }

  /**
   * The step as it was written in the path it was parsed from, predicates included. Use this
   * wherever a step is put back into a path: the step text and its predicates are held separately,
   * so composing a path from the step text alone silently discards the predicates.
   */
  @Override
  public String toString() {
    return getStepText() + getPredicateText();
  }

  @Override
  public int hashCode() {
    return Objects.hash(stepText, predicates);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }

    XPathStep other = (XPathStep) obj;
    // First check the step texts are the different.
    if (!Objects.equals(getStepText(), other.getStepText())) {
      return false;
    }

    if (getPredicates().size() != other.getPredicates().size()) {
      // If one of the two steps has more predicates that the other,
      // then the steps are not equal.
      return false;
    }

    // Both steps have the same number of predicates
    // If there are no predicates then the steps are the same.
    if (getPredicates().isEmpty()) {
      return true;
    }

    // If there is only one predicate in each step, then we can do a quick comparison.
    if (getPredicates().size() == 1) {
      return Objects.equals(getPredicates().get(0), other.getPredicates().get(0));
    }

    // Both steps contain multiple predicates.
    // We need to compare them one by one.
    // First we make a copy so that we can sort them without affecting the original lists.
    List<String> pathPredicates = new ArrayList<>(getPredicates());
    List<String> contextPredicates = new ArrayList<>(other.getPredicates());
    Collections.sort(pathPredicates);
    Collections.sort(contextPredicates);

    return pathPredicates.equals(contextPredicates);
  }

  public boolean isTheSameAs(final XPathStep other) {

    // First check the step texts are the different.
    if (!Objects.equals(getStepText(), other.getStepText())) {
      return false;
    }

    // If one of the two steps has more predicates that the other,
    if (this.getPredicates().size() != other.getPredicates().size()) {
      // then the steps are the same if the path has no predicates
      // or all the predicates of the path are also found in the context.
      return this.getPredicates().isEmpty() || other.getPredicates().containsAll(this.getPredicates());
    }

    // Both steps have the same number of predicates
    // If there are no predicates then the steps are the same.
    if (this.getPredicates().isEmpty()) {
      return true;
    }

    // If there is only one predicate in each step, then we can do a quick comparison.
    if (this.getPredicates().size() == 1) {
      return Objects.equals(getPredicates().get(0), other.getPredicates().get(0));
    }

    // Both steps contain multiple predicates.
    // We need to compare them one by one.
    // First we make a copy so that we can sort them without affecting the original lists.
    List<String> pathPredicates = new ArrayList<>(this.getPredicates());
    List<String> contextPredicates = new ArrayList<>(other.getPredicates());
    Collections.sort(pathPredicates);
    Collections.sort(contextPredicates);

    return pathPredicates.equals(contextPredicates);
  }

  /*
   * @deprecated Use {@link #isSameAsOrNarrowerThan(XPathStep)} instead.
   *
   * This method was renamed for clarity. It is marked as deprecated so that the
   * library interface does not change. It will be removed in the next major
   * version of the library.
   * 
   */
  @Deprecated(since = "1.3.0", forRemoval = true)
  public boolean isSimilarTo(final XPathStep other) {
    return isSameAsOrNarrowerThan(other);
  }

  public boolean isSameAsOrNarrowerThan(final XPathStep other) {

    // First check the step texts are different.
    if (!Objects.equals(other.stepText, this.stepText)) {
      return false;
    }

    // If one of the two steps has more predicates that the other,
    if (this.predicates.size() != other.predicates.size()) {
      // then this step is same as or narrower that the other, if either of them has
      // no predicates or all the predicates of the other step are also found in this
      // step. In this case this step has the same predicates as the other one, plus
      // some more, which means it selects a subset of the nodes selected by the other
      // step and therefore it is "narrower".
      return other.predicates.isEmpty() || this.predicates.containsAll(other.predicates);
    }

    assert !this.isTheSameAs(other) : "You should not be calling isSameAsOrNarrowerThan() without first checking isTheSameAs()";
    return false;
  }

  @Override
  public int compareTo(XPathStep other) {
    return Comparator.comparing(XPathStep::getStepText)
        .thenComparing(XPathStep::getPredicateText)
        .compare(this, other);
  }

  public boolean isVariableStep() {
    return stepText.startsWith("$");
  }

  /**
   * Whether this step only moves about, without naming anything: the current node or the parent
   * node. Any predicate it carries still describes the node it arrives at.
   */
  public boolean isNavigationStep() {
    return ".".equals(this.stepText) || "..".equals(this.stepText);
  }

  /**
   * Whether this step names the nodes to look for, which is what an axis has to be followed by.
   * A parent step, an attribute, an axis of the step's own, a variable, a function call, a literal
   * and a parenthesised expression are all steps, but none of them can be written after an axis.
   */
  public boolean isNodeTest() {
    return this.nodeTest;
  }
}
