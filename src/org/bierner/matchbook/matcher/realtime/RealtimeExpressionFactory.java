/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.matcher.realtime;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import org.bierner.matchbook.analyzer.Analyzer;
import org.bierner.matchbook.analyzer.Annotation;
import org.bierner.matchbook.analyzer.SimpleAnalyzer;
import org.bierner.matchbook.analyzer.java.BreakIteratorSentenceDetector;
import org.bierner.matchbook.analyzer.java.BreakIteratorTokenizer;
import org.bierner.matchbook.analyzer.opennlp.PorterStemmer;
import org.bierner.matchbook.matcher.ExpressionFactory;
import org.bierner.matchbook.matcher.realtime.expr.AnnotationExpression;
import org.bierner.matchbook.matcher.realtime.expr.CaptureExpression;
import org.bierner.matchbook.matcher.realtime.expr.CompoundExpression;
import org.bierner.matchbook.matcher.realtime.expr.Expression;
import org.bierner.matchbook.matcher.realtime.expr.RepeatExpression;
import org.bierner.matchbook.matcher.realtime.expr.WithExpression;

/**
 * An implementation of {@link ExpressionFactory} that builds an internal representation for matching text on-the-fly
 * using the {@link RealtimeSentenceMatcher}.
 * 
 * @author gann
 */
public class RealtimeExpressionFactory extends ExpressionFactory<Expression> {
    public RealtimeExpressionFactory(Analyzer analyzer) {
        super(analyzer);
    }

    @Override
    public CompoundExpression or(List<Expression> children) {
        return new CompoundExpression(CompoundExpression.Type.OR, children);
    }

    @Override
    public CompoundExpression is(List<Expression> children) {
        return new CompoundExpression(CompoundExpression.Type.IS, children);
    }

    @Override
    public CompoundExpression sequence(List<Expression> children) {
        return new CompoundExpression(CompoundExpression.Type.SEQUENCE, children);        
    }

    @Override
    public CompoundExpression isnt(Expression e1, Expression e2) {
        return new CompoundExpression(CompoundExpression.Type.ISNT, Lists.newArrayList(e1, e2));
    }

    @Override
    public AnnotationExpression annotation(String type) {
        return new AnnotationExpression(type, null);
    }

    @Override
    public AnnotationExpression annotation(String type, String value) {
        return new AnnotationExpression(type, value);
    }
    
    @Override
    public AnnotationExpression start() {
        return annotation(Annotation.BOUNDARY, Annotation.BOUNDARY_START);
    }

    @Override
    public AnnotationExpression end() {
        return annotation(Annotation.BOUNDARY, Annotation.BOUNDARY_END);
    }
    
    @Override
    public WithExpression with(Expression expression1, Expression expression2) {
        return new WithExpression((AnnotationExpression) expression1, expression2);
    }
    
    @Override
    public RepeatExpression repeat(Expression expression, int n, int m) {
        return new RepeatExpression(expression, n, m);
    }

    @Override
    public CaptureExpression capture(String id, Expression expression) {
        return new CaptureExpression(id, expression);
    }
    
    // This is a simple test main to parse using this factory.
    public static void main(String[] args) {
        SimpleAnalyzer analyzer = SimpleAnalyzer.builder().
                locale(Locale.ENGLISH).
                sentenceDetector(new BreakIteratorSentenceDetector(Locale.ENGLISH)).
                annotator(new BreakIteratorTokenizer(Locale.ENGLISH)).
                annotator(new PorterStemmer()).build();
        System.out.println(new RealtimeExpressionFactory(analyzer).parse(args[0]));
    }
}    