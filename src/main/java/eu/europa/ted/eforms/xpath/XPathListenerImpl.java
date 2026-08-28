package eu.europa.ted.eforms.xpath;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import eu.europa.ted.eforms.xpath.XPath20Parser.AbbrevforwardstepContext;
import eu.europa.ted.eforms.xpath.XPath20Parser.PathexprContext;
import eu.europa.ted.eforms.xpath.XPath20Parser.AxisstepContext;
import eu.europa.ted.eforms.xpath.XPath20Parser.FilterexprContext;
import eu.europa.ted.eforms.xpath.XPath20Parser.PredicateContext;

class XPathListenerImpl extends XPath20BaseListener {
  private XPathInfo xpathInfo;
  
  private String inputText;
  private CharStream inputStream;
  private LinkedList<StepInfo> steps;
  private int inPredicate = 0;
  private boolean anchorFound;

  public XPathInfo parse(String xpathInput) {
    steps = new LinkedList<>();
    xpathInfo = new XPathInfo();
    inPredicate = 0;
    anchorFound = false;

    this.inputText = xpathInput;
    this.inputStream = CharStreams.fromString(xpathInput);
    final XPath20Lexer lexer = new XPath20Lexer(inputStream);
    final CommonTokenStream tokens = new CommonTokenStream(lexer);
    final XPath20Parser parser = new XPath20Parser(tokens);
    final ParseTree tree = parser.xpath();

    final ParseTreeWalker walker = new ParseTreeWalker();
    walker.walk(this, tree);

    steps.stream().forEach(s -> {
      XPathStep step = new XPathStep(s.stepText, s.predicates, s.nodeTest);
      xpathInfo.addStep(step);
    });

    if (!xpathInfo.isAttribute()) {
      // The XPath does not point to an attribute, so it is the path to the last element
      xpathInfo.setPathToLastElement(xpathInput);
    }

    return xpathInfo;
  }

  @Override
  public void exitAxisstep(AxisstepContext ctx) {
    if (inPredicateMode()) {
      return;
    }

    // When we recognize a step, we add it to the queue if is is empty.
    // If the queue is not empty, and the depth of the new step is not smaller than
    // the depth of the last step in the queue, then this step needs to be added to
    // the queue too.
    // Otherwise, the last step in the queue is a sub-expression of the new step,
    // and we need to
    // replace it in the queue with the new step.
    if (this.steps.isEmpty() || !this.steps.getLast().isPartOf(ctx.getSourceInterval())) {
      this.steps.offer(new StepInfo(ctx, this::getInputText));
    } else {
      Interval removedInterval = ctx.getSourceInterval();
      while(!this.steps.isEmpty() && this.steps.getLast().isPartOf(removedInterval)) {
        this.steps.removeLast();
      }
      this.steps.offer(new StepInfo(ctx, this::getInputText));
    }
  }

  @Override
  public void exitFilterexpr(FilterexprContext ctx) {
    if (inPredicateMode()) {
      return;
    }

    // Same logic as for axis steps here (sse exitAxisstep).
    if (this.steps.isEmpty() || !this.steps.getLast().isPartOf(ctx.getSourceInterval())) {
      this.steps.offer(new StepInfo(ctx, this::getInputText));
    } else {
      Interval removedInterval = ctx.getSourceInterval();
      while(!this.steps.isEmpty() && this.steps.getLast().isPartOf(removedInterval)) {
        this.steps.removeLast();
      }
      this.steps.offer(new StepInfo(ctx, this::getInputText));
    }
  }

  /**
   * The grammar spells the anchor out: {@code pathexpr : (SLASH relativepathexpr?) | (SS
   * relativepathexpr) | relativepathexpr}. Reading it from the parse tree rather than from the
   * start of the input keeps it right for paths the lexer has to look at first, such as one
   * preceded by a comment. Only the outermost path expression is the path's own anchor; those
   * inside a predicate belong to the predicate.
   */
  @Override
  public void enterPathexpr(PathexprContext ctx) {
    if (inPredicate > 0 || anchorFound) {
      return;
    }
    anchorFound = true;

    if (ctx.SS() != null) {
      xpathInfo.setAnchor(XPathAnchor.DESCENDANT_FROM_ROOT);
    } else if (ctx.SLASH() != null) {
      xpathInfo.setAnchor(XPathAnchor.ROOT);
    } else {
      xpathInfo.setAnchor(XPathAnchor.RELATIVE);
    }
  }

  @Override
  public void enterPredicate(PredicateContext ctx) {
    this.inPredicate++;
  }

  @Override
  public void exitPredicate(PredicateContext ctx) {
    this.inPredicate--;
  }

  @Override
  public void exitAbbrevforwardstep(AbbrevforwardstepContext ctx) {
    if (!inPredicateMode() && ctx.AT() != null) {
      xpathInfo.setAttributeName(ctx.nodetest().getText());

      int splitPosition = ctx.AT().getSymbol().getCharPositionInLine();
      String path = inputText.substring(0, splitPosition);
      while (path.endsWith("/")) {
        path = path.substring(0, path.length() - 1);
      }
      xpathInfo.setPathToLastElement(path);
    }
  }

  /**
   * Helper method that returns the input text that matched a parser rule context. It is useful
   * because {@link ParserRuleContext#getText()} omits whitespace and other lexer tokens in the
   * HIDDEN channel.
   *
   * @param context Information on a rule that matched
   * @return The input text that matched the rule corresponding to the specified context
   */
  private String getInputText(ParserRuleContext context) {
    return this.inputStream
        .getText(new Interval(context.start.getStartIndex(), context.stop.getStopIndex()));
  }

  private Boolean inPredicateMode() {
    return inPredicate > 0;
  }

  /**
   * Whether the step is written as a bare node test, which the grammar spells out as
   * {@code abbrevforwardstep : AT? nodetest}. That is the only form an axis can be followed by: a
   * reverse step, a step carrying an axis of its own and an attribute are all steps, but none of
   * them can be written after {@code axis::}.
   */
  private static boolean isBareNodeTest(final AxisstepContext ctx) {
    return ctx.forwardstep() != null && ctx.forwardstep().abbrevforwardstep() != null
        && ctx.forwardstep().abbrevforwardstep().AT() == null;
  }

  private class StepInfo {
    String stepText;
    List<String> predicates;
    boolean nodeTest;
    int a;
    int b;

    private StepInfo(AxisstepContext ctx, Function<ParserRuleContext, String> getInputText) {
      this(ctx.reversestep() != null ? getInputText.apply(ctx.reversestep()) : getInputText.apply(ctx.forwardstep()), 
          ctx.predicatelist().predicate().stream().map(getInputText).collect(Collectors.toList()),
          ctx.getSourceInterval(), isBareNodeTest(ctx));
    }

    /**
     * A filter expression is a literal, a variable, a parenthesised expression, the context item or
     * a function call. None of them names nodes the way an axis needs.
     */
    private StepInfo(FilterexprContext ctx, Function<ParserRuleContext, String> getInputText) {
      this(getInputText.apply(ctx.primaryexpr()),
          ctx.predicatelist().predicate().stream().map(getInputText).collect(Collectors.toList()),
          ctx.getSourceInterval(), false);
    }

    private StepInfo(String stepText, List<String> predicates, Interval interval,
        boolean nodeTest) {
      this.stepText = stepText;
      this.predicates = predicates;
      this.nodeTest = nodeTest;
      this.a = interval.a;
      this.b = interval.b;
    }

    private Boolean isPartOf(Interval interval) {
      return this.a >= interval.a && this.b <= interval.b;
    }
  }
}
