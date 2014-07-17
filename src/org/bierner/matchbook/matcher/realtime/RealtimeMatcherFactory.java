/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.matcher.realtime;

import java.util.Collections;
import org.bierner.matchbook.matcher.realtime.expr.Expression;
import org.bierner.matchbook.matcher.realtime.indexing.IndexingRealtimeSentenceMatcher;
import org.bierner.matchbook.matcher.realtime.indexing.PackedVectorFactory;
import org.bierner.matchbook.matcher.realtime.indexing.VectorFactory;

/**
 * A factory class for creating real-time matchers.  At this time, the only implemented method for matching
 * is using an indexed approach with a packed vector (ie integer start and end positions packed into a long).
 * This class will expand as new techniques are created.
 * 
 * @author gann
 */
public class RealtimeMatcherFactory {
    
    /**
     * Creates a new indexing-based matcher for the given expression.
     * 
     * @param expression
     * @return a matcher
     */
    public static RealtimeSentenceMatcher newIndexingMatcher(Expression expression) {
        return newIndexingMatcher(expression, new PackedVectorFactory());
    }
    
    /**
     * Creates a new indexing-based matcher for the given expression, using a custom vector factory to implement
     * the expression operations.
     * 
     * @param expression
     * @param factory
     * @return a matcher
     */
    public static RealtimeSentenceMatcher newIndexingMatcher(Expression expression, VectorFactory factory) {
        return new IndexingRealtimeSentenceMatcher(factory, Collections.singletonList(expression));
    }
    
}
