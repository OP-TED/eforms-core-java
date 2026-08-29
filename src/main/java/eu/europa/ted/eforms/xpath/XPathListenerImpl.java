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
import eu.europa.ted.eforms.xpath.XPath20Parser.PredicatelistContext;
import eu.europa.ted.eforms.xpath.XPath20Parser.ForwardaxisContext;
import eu.europa.ted.eforms.xpath.XPath20Parser.ForwardstepContext;
import eu.europa.ted.eforms.xpath.XPath20Parser.NodetestContext;
import eu.europa.ted.eforms.xpath.XPath20Parser.ReversestepContext;

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

    steps.stream().forEach(s -> xpathInfo.addStep(s.step));

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
   * The step an axis step is written as, told apart by whether what it looks for could be looked
   * for along another axis. A step is read the same way however it is spelled: {@code b} is
   * {@code child::b}, {@code @x} is {@code attribute::x} and {@code ..} is {@code parent::node()}.
   */
  private XPathStep readAxisStep(final AxisstepContext ctx, final List<String> predicates) {
    final ForwardstepContext forward = ctx.forwardstep();
    if (forward != null) {
      final AbbrevforwardstepContext abbreviated = forward.abbrevforwardstep();
      if (abbreviated != null) {
        return abbreviated.AT() != null
            ? XPathStep.opaque(getInputText(forward), predicates)
            : XPathStep.retargetable(getInputText(forward),
                getInputText(abbreviated.nodetest()), predicates);
      }

      final ForwardaxisContext axis = forward.forwardaxis();
      if (axis.KW_ATTRIBUTE() != null || axis.KW_NAMESPACE() != null) {
        // An attribute and a namespace are found on the axis leading to them and nowhere else, so
        // a node test naming one means nothing along another axis.
        return XPathStep.opaque(getInputText(forward), predicates);
      }
      if (axis.KW_SELF() != null && namesAnyNode(forward.nodetest())) {
        return XPathStep.navigation(getInputText(forward), predicates);
      }
      return XPathStep.retargetable(getInputText(forward), getInputText(forward.nodetest()),
          predicates);
    }

    final ReversestepContext reverse = ctx.reversestep();
    if (reverse.reverseaxis() == null) {
      return XPathStep.navigation(getInputText(reverse), predicates);
    }
    if (reverse.reverseaxis().KW_PARENT() != null && namesAnyNode(reverse.nodetest())) {
      return XPathStep.navigation(getInputText(reverse), predicates);
    }
    return XPathStep.retargetable(getInputText(reverse), getInputText(reverse.nodetest()),
        predicates);
  }

  /**
   * The step a filter expression is written as. The context item is the one of them that only moves
   * about; the rest are evaluated for the nodes they return and stay where they are.
   */
  private XPathStep readFilterStep(final FilterexprContext ctx, final List<String> predicates) {
    if (ctx.primaryexpr().contextitemexpr() != null) {
      return XPathStep.navigation(getInputText(ctx.primaryexpr()), predicates);
    }
    return XPathStep.opaque(getInputText(ctx.primaryexpr()), predicates);
  }

  /**
   * Whether the node test takes any node at all, which is what the steps that only move about look
   * for. It is the {@code node()} of {@code self::node()} and {@code parent::node()}, written short
   * as {@code .} and {@code ..}.
   */
  private static boolean namesAnyNode(final NodetestContext ctx) {
    return ctx.kindtest() != null && ctx.kindtest().anykindtest() != null;
  }

  private static List<String> predicatesOf(final PredicatelistContext ctx,
      final Function<ParserRuleContext, String> getInputText) {
    return ctx.predicate().stream().map(getInputText).collect(Collectors.toList());
  }


  private class StepInfo {
    XPathStep step;
    int a;
    int b;

    private StepInfo(AxisstepContext ctx, Function<ParserRuleContext, String> getInputText) {
      this(readAxisStep(ctx, predicatesOf(ctx.predicatelist(), getInputText)),
          ctx.getSourceInterval());
    }

    private StepInfo(FilterexprContext ctx, Function<ParserRuleContext, String> getInputText) {
      this(readFilterStep(ctx, predicatesOf(ctx.predicatelist(), getInputText)),
          ctx.getSourceInterval());
    }

    private StepInfo(XPathStep step, Interval interval) {
      this.step = step;
      this.a = interval.a;
      this.b = interval.b;
    }

    private Boolean isPartOf(Interval interval) {
      return this.a >= interval.a && this.b <= interval.b;
    }
  }
}
