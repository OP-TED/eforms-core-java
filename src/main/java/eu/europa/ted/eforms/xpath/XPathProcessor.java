package eu.europa.ted.eforms.xpath;

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

  public static String addAxis(String axis, String path) {
    LinkedList<XPathStep> steps = new LinkedList<>(parse(path).getSteps());

    while (steps.getFirst().getStepText().equals("..")) {
      steps.removeFirst();
    }

    return axis + "::" + steps.stream().map(s -> s.getStepText()).collect(Collectors.joining("/"));
  }

  public static String join(final String first, final String second) {

    if (first == null || first.trim().isEmpty()) {
      return second;
    }

    if (second == null || second.trim().isEmpty()) {
      return first;
    }

    LinkedList<XPathStep> firstPartSteps = new LinkedList<>(parse(first).getSteps());
    LinkedList<XPathStep> secondPartSteps = new LinkedList<>(parse(second).getSteps());

    return getJoinedXPath(firstPartSteps, secondPartSteps);
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
        relativeXpath += "/" + step.getStepText() + step.getPredicateText();
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
      final LinkedList<XPathStep> second) {
    List<String> dotSteps = Arrays.asList("..", ".");
    while (second.getFirst().getStepText().equals("..")
        && !dotSteps.contains(first.getLast().getStepText()) && !first.getLast().isVariableStep()) {
      second.removeFirst();
      first.removeLast();
    }

    return first.stream().map(f -> f.getStepText()).collect(Collectors.joining("/"))
        + "/" + second.stream().map(s -> s.getStepText()).collect(Collectors.joining("/"));
  }
}
