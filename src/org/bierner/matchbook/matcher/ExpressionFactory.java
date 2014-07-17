/*
 * Copyright (c) 2014, Gann Bierner
 */
package org.bierner.matchbook.matcher;

import com.google.common.base.Function;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.bierner.matchbook.analyzer.Analyzer;
import org.bierner.matchbook.parser.ExpressionListener;
import org.bierner.matchbook.parser.MatchbookLexer;
import org.bierner.matchbook.parser.MatchbookParser;

/**
 * An implementation of this abstract class is provided to {@link ExpressionListener} to build an internal representation of a matching expression.
 * The result can then be used in whatever matching strategy is desired.  For example, an expression factory could be used to generate Solr/Lucene
 * search queries.  Parsing to a new internal form is as simple as subclassing this class and calling the {@link #parse(java.lang.String) } method.
 * 
 * @param <T> The top level expression type.
 * @author gann
 */
@RequiredArgsConstructor
public abstract class ExpressionFactory<T> {
    @NonNull private Analyzer analyzer;
    
    /**
     * Parses matching expressions using the ANTLR grammar in Matchbook.g.
     * @param input matching expression based on the syntax defined in Matchbook.g
     * @return an expression of type T
     */
    public T parse(String input) {
        MatchbookLexer        lexer    = new MatchbookLexer(new ANTLRInputStream(input));
        MatchbookParser       parser   = new MatchbookParser(new CommonTokenStream(lexer));

        parser.setErrorHandler(new BailErrorStrategy());        
        
        ParseTree             tree     = parser.expression();
        ParseTreeWalker       walker   = new ParseTreeWalker();
        ExpressionListener<T> listener = new ExpressionListener<>(analyzer, this);
        
        walker.walk(listener, tree);
        
        return listener.getResult();
     }
    
    // Atomic Expressions
    public abstract T start();
    public abstract T end();    
    public abstract T annotation(String type);    
    public abstract T annotation(String type, String value);
    
    // Compound Expressions
    public abstract T or      (List<T> children);
    public abstract T is      (List<T> children);        
    public abstract T sequence(List<T> children);            
    public abstract T isnt    (T t1, T t2);
    
    // Misc Expressions
    public abstract T with(T expression1, T expression2);
    public abstract T repeat(T expression, int n, int m);
    public abstract T capture(String id, T expression);
    
    // Convenience functions until we get lambda in Java 8
    public final Function<List<T>,T> orFcn = new Function<List<T>, T>() {
        @Override public T apply(List<T> subExpressions) { return or(subExpressions); }
    };    
    
    public final Function<List<T>,T> sequenceFcn = new Function<List<T>, T>() {
        @Override public T apply(List<T> subExpressions) { return sequence(subExpressions); }
    };    
    
    public final Function<List<T>,T> isFcn = new Function<List<T>, T>() {
        @Override public T apply(List<T> subExpressions) { return is(subExpressions); }
    };
}
