/*
 * Copyright (c) 2014, Gann Bierner
 */
package org.bierner.matchbook.matcher.realtime.expr;

/**
 * A common interface for all matchbook expressions.
 * @author gann
 */
public interface Expression {
    
    /**
     * Visits any subexpressions of this expression (if they exist) then the expression itself.
     * @param visitor
     */
    void accept(ExpressionVisitor visitor);
}
