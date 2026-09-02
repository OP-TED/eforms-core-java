package eu.europa.ted.eforms.xpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class XPathProcessor {

  public static XPathInfo parse(String xpathInput) {
    XPathListenerImpl parser = new XPathListenerImpl();

    return parser.parse(xpathInput);
  }

  /**
   * Rewrites a path so that it looks along the given axis instead of along the one it was written
   * for.
   *
   * <p>
   * This serves the axis that can be written on an EFX-1 field reference, and nothing more. The
   * axis is expected to be one that XPath knows, and the path to be relative to the context the
   * axis is applied from; an absolute path cannot keep its anchor, because an axis cannot be
   * followed by a separator, so it is read the same way. It is not a general way of rewriting
   * XPath.
   *
   * @throws IllegalArgumentException if either the axis or the path is missing. A path that is
   *         there is always rewritten into one that XPath accepts; one that is not there names
   *         nothing to rewrite, and an axis that is not there asks for nothing to be done.
   */
  public static String addAxis(final String axis, final String path) {
    if (axis == null || axis.trim().isEmpty()) {
      throw new IllegalArgumentException(
          "No axis was given to look along. Pass the name of an XPath axis, such as 'preceding'.");
    }
    if (path == null || path.trim().isEmpty()) {
      throw new IllegalArgumentException(String.format(
          "No path was given to look for along the '%s' axis.", axis.trim()));
    }

    final LinkedList<XPathStep> steps = new LinkedList<>(parse(path).getSteps());

    // Moving about before the axis makes no difference to what it finds, since it searches from the
    // context node wherever the path would have gone first. Such steps are dropped, except for the
    // one the axis is put on, and except where a predicate says which node was arrived at.
    while (steps.size() > 1 && steps.getFirst().isNavigationStep()
        && steps.getFirst().getPredicates().isEmpty()) {
      steps.removeFirst();
    }

    if (steps.isEmpty()) {
      return XPathStep.anyNodeOn(axis).toString();
    }

    steps.addAll(0, steps.removeFirst().onAxis(axis));

    return steps.stream().map(s -> s.toString()).collect(Collectors.joining("/"));
  }

  public static String join(final String first, final String second) {

    if (first == null || first.trim().isEmpty()) {
      return second;
    }

    if (second == null || second.trim().isEmpty()) {
      return first;
    }

    final XPathInfo firstPart = parse(first);
    LinkedList<XPathStep> firstPartSteps = new LinkedList<>(firstPart.getSteps());
    LinkedList<XPathStep> secondPartSteps = new LinkedList<>(parse(second).getSteps());

    final XPathAnchor anchor = firstPart.getAnchor();
    final String joined = getJoinedXPath(firstPartSteps, secondPartSteps, anchor);

    if (joined.isEmpty()) {
      // The back-steps consumed both parts, so the join resolves to where it started from: the
      // root of the document if the first part was anchored there, and the current context if not.
      return anchor.isAbsolute() ? "/" : ".";
    }

    return anchor.getSeparator() + joined;
  }

  public static String contextualize(final String contextXpath, final String xpath) {

    // If we are asked to contextualise against a null or empty context
    // then we must return the original xpath (instead of throwing an exception).
    if (contextXpath == null || contextXpath.isEmpty()) {
      return xpath;
    }

    LinkedList<XPathStep> contextSteps = new LinkedList<>(parse(contextXpath).getSteps());
    LinkedList<XPathStep> pathSteps = new LinkedList<>(parse(xpath).getSteps());

    return getContextualizedXpath(contextSteps, pathSteps);
  }

  private static String getContextualizedXpath(Queue<XPathStep> contextQueue,
      final Queue<XPathStep> pathQueue) {

    // We will store the relative xPath here as we build it.
    String relativeXpath = "";

    if (contextQueue != null) {

      // First we will "consume" all nodes that are the same in both xPaths.
      while (!contextQueue.isEmpty() && !pathQueue.isEmpty()
          && pathQueue.peek().isTheSameAs(contextQueue.peek())) {
        contextQueue.poll();
        pathQueue.poll();
      }

      // At this point there are no more matching nodes in the two queues.

      // We look at the first of the remaining steps in both queues and look if
      // the context is the same as or less restrictive than the path. In this case
      // we want to use a dot step with the predicate of the path.
      if (!contextQueue.isEmpty() && !pathQueue.isEmpty()
          && pathQueue.peek().isSameAsOrNarrowerThan(contextQueue.peek())) {
        // Consume the same step from the contextQueue and get its predicates
        List<String> contextPredicates = contextQueue.poll().getPredicates(); 
        // Keep only the predicates that are not in the context.
        String pathPredicates = pathQueue.poll().getPredicates().stream().filter(p -> !contextPredicates.contains(p)).collect(Collectors.joining(""));
        if (contextQueue.isEmpty()) {
          // Since there are no more steps in the contextQueue, the relative xpath should 
          // start with a dot step to provide a context for the predicate.
          relativeXpath += "." + pathPredicates;
        } else {
          // Since there are more steps in the contextQueue which we will need to navigate back to,
          // using back-steps, we will use a back-step to provide context for the predicate.
          // This avoids an output that looks like ../.[predicate] which is valid but silly.  
          contextQueue.poll();  // consume the step from the contextQueue
          relativeXpath += ".." + pathPredicates;
        }
      }

      // We start building the resulting relativeXpath by appending any nodes
      // remaining in the pathQueue.
      while (!pathQueue.isEmpty()) {
        final XPathStep step = pathQueue.poll();
        relativeXpath += "/" + step;
      }

      // We remove any leading forward slashes from the resulting xPath.
      while (relativeXpath.startsWith("/")) {
        relativeXpath = relativeXpath.substring(1);
      }

      // For each step remaining in the contextQueue we prepend a back-step (..) in
      // the resulting relativeXpath.
      while (!contextQueue.isEmpty()) {
        contextQueue.poll(); // consume the step
        relativeXpath = "../" + relativeXpath; // prepend a back-step
      }

      // We remove any trailing forward slashes from the resulting xPath.
      while (relativeXpath.endsWith("/")) {
        relativeXpath = relativeXpath.substring(0, relativeXpath.length() - 1);
      }


      // The relativeXpath will be empty if the path was identical to the context.
      // In this case we return a dot.
      if (relativeXpath.isEmpty()) {
        relativeXpath = ".";
      }
    }

    return relativeXpath;
  }

  private static String getJoinedXPath(LinkedList<XPathStep> first,
      final LinkedList<XPathStep> second, final XPathAnchor anchor) {
    List<String> dotSteps = Arrays.asList("..", ".");

    // A path that searches from the root matches at any depth, so the position of its first step is
    // not known. Cancelling that step against a parent step would claim a position it does not
    // have, so it is left in place.
    final int minimumStepsToKeep = anchor == XPathAnchor.DESCENDANT_FROM_ROOT ? 1 : 0;
    while (!second.isEmpty() && first.size() > minimumStepsToKeep
        && second.getFirst().getStepText().equals("..")
        && !dotSteps.contains(first.getLast().getStepText()) && !first.getLast().isVariableStep()
        // A step going somewhere and a step coming back cancel out, but only when neither says
        // anything about where it went. A predicate on either of them is a condition on the result,
        // so a step carrying one is kept and the two are left to stand as they were written.
        && second.getFirst().getPredicates().isEmpty()
        && first.getLast().getPredicates().isEmpty()) {
      second.removeFirst();
      first.removeLast();
    }

    // Both parts are joined as one sequence of steps. Gluing the two halves together with a
    // separator of our own would put one in front of the result whenever the first part is empty,
    // which happens when the back-steps above consume all of it.
    final List<XPathStep> steps = new ArrayList<>(first);
    steps.addAll(second);

    return steps.stream().map(s -> s.toString()).collect(Collectors.joining("/"));
  }
}
