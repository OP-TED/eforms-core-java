package eu.europa.ted.eforms.xpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * One step of a path.
 *
 * <p>
 * A step read from a path is known well enough to say whether it can be looked for along another
 * axis, which is all {@link XPathProcessor#addAxis} needs of it. A step built from its text alone
 * says nothing of the sort and is taken at its word.
 *
 * <p>
 * How a step was read is not part of what it is worth: two steps written the same way, carrying the
 * same predicates, are the same step.
 */
public class XPathStep implements Comparable<XPathStep> {
  /** The node test that matches any node, which the steps that only move about look for. */
  private static final String ANY_NODE = "node()";

  /**
   * As much as is needed to know whether a step can be looked for along a different axis. This is
   * not a reading of XPath's own grammar, which distinguishes far more than this: an explicit
   * {@code child::b} and a plain {@code b} are told apart there and are the same thing here.
   */
  private enum StepKind {
    /** The step names something that can be looked for along another axis. */
    RETARGETABLE,

    /** The step only moves about, naming nothing: the current node, or the parent node. */
    NAVIGATION,

    /**
     * Anything else. An attribute, a namespace and an expression all name something that is only
     * found where it already is, and a step built from text alone is not known at all. All of them
     * stay where they are.
     */
    OPAQUE
  }

  private final String stepText;
  private final List<String> predicates;
  private final StepKind kind;

  /** What the step looks for, where it looks for anything. */
  private final String nodeTest;

  /**
   * Builds a step from the text it is written as. Steps read from a path are built by the parser,
   * which knows more about them than their text says.
   */
  public XPathStep(String stepText, List<String> predicates) {
    this(stepText, predicates, StepKind.OPAQUE, null);
  }

  private XPathStep(final String stepText, final List<String> predicates, final StepKind kind,
      final String nodeTest) {
    this.stepText = StringUtils.strip(stepText);
    this.predicates = predicates == null ? Collections.emptyList() : predicates;
    this.kind = kind;
    this.nodeTest = nodeTest;
  }

  /**
   * A step that names what it looks for, which can therefore be looked for along another axis. The
   * node test is what it looks for, apart from however the step happens to be written.
   */
  static XPathStep retargetable(final String stepText, final String nodeTest,
      final List<String> predicates) {
    return new XPathStep(stepText, predicates, StepKind.RETARGETABLE,
        StringUtils.strip(nodeTest));
  }

  /**
   * A step that only moves about: {@code .} or {@code ..}. It names nothing, so what it arrives at
   * is any node at all, and any predicate it carries describes that node.
   */
  static XPathStep navigation(final String stepText, final List<String> predicates) {
    return new XPathStep(stepText, predicates, StepKind.NAVIGATION, ANY_NODE);
  }

  /**
   * A step that has to be left where it is: an attribute, a namespace, or an expression evaluated
   * for the nodes it returns.
   */
  static XPathStep opaque(final String stepText, final List<String> predicates) {
    return new XPathStep(stepText, predicates, StepKind.OPAQUE, null);
  }

  /**
   * A step that walks the given axis and takes whatever it finds.
   */
  static XPathStep anyNodeOn(final String axis) {
    return retargetable(axis + "::" + ANY_NODE, ANY_NODE, Collections.emptyList());
  }

  /**
   * The same step, looked for along the given axis instead.
   *
   * <p>
   * A step that names what it looks for is simply looked for elsewhere, and one step comes back. A
   * step that has to stay where it is keeps its place behind a step that walks the axis, and two
   * come back.
   */
  List<XPathStep> onAxis(final String axis) {
    if (this.kind == StepKind.OPAQUE) {
      return Arrays.asList(anyNodeOn(axis), this);
    }
    return Collections
        .singletonList(retargetable(axis + "::" + this.nodeTest, this.nodeTest, this.predicates));
  }

  /**
   * Whether the step only moves about, without naming anything to look for.
   */
  boolean isNavigationStep() {
    return this.kind == StepKind.NAVIGATION;
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
}
