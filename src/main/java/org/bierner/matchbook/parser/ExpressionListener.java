/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.parser;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.bierner.matchbook.analyzer.Analyzer;
import org.bierner.matchbook.analyzer.Annotation;
import org.bierner.matchbook.analyzer.util.SentenceUtilities;
import org.bierner.matchbook.matcher.ExpressionFactory;

/**
 * Given an {@link ExpressionFactory}, this class will traverse a matchbook expression and build up
 * a final expression.  It shouldn't be necessary to access this class directly, though.  See the
 * {@link ExpressionFactory#parse(java.lang.String) } method for the simplest entry point.
 *
 * @param <T>
 * @author gann
 */
@RequiredArgsConstructor
@ExtensionMethod(SentenceUtilities.class)
public class ExpressionListener<T> implements MatchbookListener {
    @NonNull private Analyzer analyzer;
    @NonNull private ExpressionFactory<T> exprFactory;

    private ParseTreeProperty<T> exprs = new ParseTreeProperty<>();

    @Getter T result;

    ///////////////////////////////////////////////////////////////////////////
    // Atomic Expressions
    ///////////////////////////////////////////////////////////////////////////
    @Override public void enterEnd(MatchbookParser.EndContext ctx) { }
    @Override public void exitEnd(MatchbookParser.EndContext ctx) {
        exprs.put(ctx, exprFactory.end());
    }

    @Override public void enterStart(MatchbookParser.StartContext ctx) { }
    @Override public void exitStart(MatchbookParser.StartContext ctx) {
        exprs.put(ctx, exprFactory.start());
    }

    @Override public void enterPos(MatchbookParser.PosContext ctx) { }
    @Override public void exitPos(MatchbookParser.PosContext ctx) {
        exprs.put(ctx, exprFactory.annotation(Annotation.POS, ctx.getText().substring(1, ctx.getText().length()-1)));
    }

    @Override public void enterRegex(MatchbookParser.RegexContext ctx) { }
    @Override public void exitRegex(MatchbookParser.RegexContext ctx) {
        exprs.put(ctx, exprFactory.regexp(ctx.getText().substring(1, ctx.getText().length()-1)));
    }

    @Override public void enterConcept(MatchbookParser.ConceptContext ctx) { }
    @Override public void exitConcept(MatchbookParser.ConceptContext ctx) {
        String concept = ctx.getText().substring(1, ctx.getText().length()-1);
        exprs.put(ctx, exprFactory.or(new ImmutableList.Builder<T>().
                add(exprFactory.annotation(Annotation.CONCEPT, concept)).
                add(exprFactory.annotation(Annotation.ANCESTOR_CONCEPT, concept)).build()));
    }

    @Override public void enterExactConcept(MatchbookParser.ExactConceptContext ctx) { }
    @Override public void exitExactConcept(MatchbookParser.ExactConceptContext ctx) {
        exprs.put(ctx, exprFactory.annotation(Annotation.CONCEPT, ctx.getText().substring(2, ctx.getText().length()-2)));
    }

    @Override public void enterChunk(MatchbookParser.ChunkContext ctx) { }
    @Override public void exitChunk(MatchbookParser.ChunkContext ctx) {
        exprs.put(ctx, exprFactory.annotation(Annotation.CHUNK, ctx.getText().substring(1, ctx.getText().length()-1)));
    }

    @Override public void enterAnnotation(MatchbookParser.AnnotationContext ctx) { }
    @Override public void exitAnnotation(MatchbookParser.AnnotationContext ctx) {
            exprs.put(ctx, getAnnotation(ctx.getText()));
    }

    private T getAnnotation(String text) {
        int colonPos = text.indexOf(":");
        if (colonPos > 0)
            return exprFactory.annotation(text.substring(0, colonPos), text.substring(colonPos+1));
        else
            return exprFactory.annotation(text);
    }

    ///////////////////////////////////////////////////////////////////////////
    // String sequences
    ///////////////////////////////////////////////////////////////////////////
    private T stringSequence(List<String> annotation, String type) {
        if (annotation.size() == 1)
            return exprFactory.annotation(type, annotation.get(0));
        else {
            List<T> annotations = new ArrayList<>();
            for (String stem : annotation)
                annotations.add(exprFactory.annotation(type, stem));
            return exprFactory.sequence(annotations);
        }
    }

    @Override public void enterTokens(MatchbookParser.TokensContext ctx) { }
    @Override public void exitTokens(MatchbookParser.TokensContext ctx) {
        String text = ctx.getText().substring(1, ctx.getText().length() - 1);
        exprs.put(ctx, stringSequence(analyzer.getSentence(text).getTokens(), Annotation.TOKEN));
    }

    @Override public void enterStems(MatchbookParser.StemsContext ctx) { }
    @Override @SuppressWarnings("unchecked")
    public void exitStems(MatchbookParser.StemsContext ctx) {
        String text = ctx.getText().startsWith("'")? ctx.getText().substring(1, ctx.getText().length() - 1) : ctx.getText();
        exprs.put(ctx, stringSequence(analyzer.getSentence(text).getStems(), Annotation.STEM));
    }

    @Override public void enterStrictStems(MatchbookParser.StrictStemsContext ctx) { }
    @Override public void exitStrictStems(MatchbookParser.StrictStemsContext ctx) {
        String text = ctx.getText().substring(2, ctx.getText().length() - 2);
        exprs.put(ctx, stringSequence(analyzer.getSentence(text).getTokens(), Annotation.STEM));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Compound Expressions
    ///////////////////////////////////////////////////////////////////////////
    private void handleCompoundExpression(ParseTree ctx, Function<List<T>, T> fcn) {
        handleCompoundExpression(ctx, fcn, true);
    }
    private void handleCompoundExpression(ParseTree ctx, Function<List<T>, T> fcn, boolean hasOp) {
        if (ctx.getChildCount() == 1)
            exprs.put(ctx, exprs.get(ctx.getChild(0)));
        else {
            List<T> subExpressions = Lists.newArrayList();
            int incr = hasOp? 2 : 1;
            for (int i = 0; i < ctx.getChildCount(); i+=incr)
                subExpressions.add(exprs.get(ctx.getChild(i)));
            exprs.put(ctx, fcn.apply(subExpressions));
        }
    }

    @Override public void enterOr(MatchbookParser.OrContext ctx) { }
    @Override public void exitOr(MatchbookParser.OrContext ctx) {
        handleCompoundExpression(ctx, exprFactory.orFcn);
    }

    @Override public void enterSequence(MatchbookParser.SequenceContext ctx) { }
    @Override public void exitSequence(MatchbookParser.SequenceContext ctx) {
        handleCompoundExpression(ctx, exprFactory.sequenceFcn, false);
    }

    @Override public void enterIs(MatchbookParser.IsContext ctx) {}
    @Override public void exitIs(MatchbookParser.IsContext ctx) {
        handleCompoundExpression(ctx, exprFactory.isFcn);
    }

    @Override public void enterIsnt(MatchbookParser.IsntContext ctx) { }
    @Override public void exitIsnt(MatchbookParser.IsntContext ctx) {
        if (ctx.getChildCount() == 1)
            exprs.put(ctx, exprs.get(ctx.getChild(0)));
        else
            exprs.put(ctx, exprFactory.isnt(exprs.get(ctx.getChild(0)), exprs.get(ctx.getChild(2))));
    }


    ///////////////////////////////////////////////////////////////////////////
    // With
    ///////////////////////////////////////////////////////////////////////////
    @Override public void enterWith(MatchbookParser.WithContext ctx) { }
    @Override public void exitWith(MatchbookParser.WithContext ctx) {
        exprs.put(ctx, exprFactory.with(getAnnotation(ctx.getChild(0).getText()), exprs.get(ctx.getChild(2))));
    }

    @Override public void enterWithList(MatchbookParser.WithListContext ctx) { }
    @Override public void exitWithList(MatchbookParser.WithListContext ctx) {
        // ANNOTATION WITH ( expr, expr, ...)
        T annotation = getAnnotation(ctx.getChild(0).getText());
        List<T> l = new ArrayList<>();
        for (int i = 3; i < ctx.getChildCount(); i+=2)
            l.add(exprFactory.with(annotation, exprs.get(ctx.getChild(i))));
        exprs.put(ctx, exprFactory.is(l));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Repeat
    ///////////////////////////////////////////////////////////////////////////
    @Override public void enterRepeatNtoM(MatchbookParser.RepeatNtoMContext ctx) { }
    @Override public void exitRepeatNtoM(MatchbookParser.RepeatNtoMContext ctx) {
        int n = Integer.parseInt(ctx.getChild(2).getText());
        int m = Integer.parseInt(ctx.getChild(4).getText());
        exprs.put(ctx, exprFactory.repeat(exprs.get(ctx.getChild(0)), n, m));
    }

    @Override public void enterRepeat0or1(MatchbookParser.Repeat0or1Context ctx) { }
    @Override public void exitRepeat0or1(MatchbookParser.Repeat0or1Context ctx) {
        exprs.put(ctx, exprFactory.repeat(exprs.get(ctx.getChild(0)), 0, 1));
    }

    @Override public void enterRepeatN(MatchbookParser.RepeatNContext ctx) { }
    @Override public void exitRepeatN(MatchbookParser.RepeatNContext ctx) {
        int n = Integer.parseInt(ctx.getChild(2).getText());
        exprs.put(ctx, exprFactory.repeat(exprs.get(ctx.getChild(0)), n, n));
    }

    @Override public void enterRepeatNone(MatchbookParser.RepeatNoneContext ctx) { }
    @Override public void exitRepeatNone(MatchbookParser.RepeatNoneContext ctx) {
        exprs.put(ctx, exprs.get(ctx.getChild(0)));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Misc
    ///////////////////////////////////////////////////////////////////////////
    @Override public void enterCapture(MatchbookParser.CaptureContext ctx) { }
    @Override public void exitCapture(MatchbookParser.CaptureContext ctx) {
        if (ctx.getChildCount() == 1)
            exprs.put(ctx, exprs.get(ctx.getChild(0)));
        else
            exprs.put(ctx, exprFactory.capture(ctx.getChild(0).getText(), exprs.get(ctx.getChild(2))));
    }

    @Override public void enterExpr(MatchbookParser.ExprContext ctx) { }
    @Override public void exitExpr(MatchbookParser.ExprContext ctx) {
        exprs.put(ctx, exprs.get(ctx.getChild(1)));
    }

    @Override public void enterExpression(MatchbookParser.ExpressionContext ctx) { }
    @Override public void exitExpression(MatchbookParser.ExpressionContext ctx) {
        result = exprs.get(ctx.getChild(0));
    }

    @Override public void visitTerminal(TerminalNode tn) { }
    @Override public void visitErrorNode(ErrorNode en) { }

    @Override public void enterEveryRule(ParserRuleContext prc) { }
    @Override public void exitEveryRule(ParserRuleContext prc) { }

}
