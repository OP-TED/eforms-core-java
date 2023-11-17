package eu.europa.ted.eforms.xpath;

import java.util.LinkedList;
import java.util.List;

public class XPathInfo {
  private LinkedList<XPathStep> steps = new LinkedList<>();
  private String pathToLastElement;
  private String attributeName;

  public List<XPathStep> getSteps() {
    return steps;
  }

  public XPathStep getLastStep() {
    return steps.getLast();
  }

  public void addStep(XPathStep step) {
    steps.addLast(step);
  }

  public String getPathToLastElement() {
    return pathToLastElement;
  }

  public void setPathToLastElement(String pathToLastElement) {
    this.pathToLastElement = pathToLastElement;
  }

  public boolean isAttribute() {
    return attributeName != null;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  /**
   * Returns true if the XPath has any predicate that contains the specified string
   * @param match The string to search for
   * @return true if the XPath has any predicate that contains the specified string, false otherwise
   */
  public boolean hasPredicate(String match) {
    return getSteps().stream().anyMatch(s -> s.getPredicateText().contains(match));
  }
}
