/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.matcher.realtime.expr;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A capture expression as defined in the matchbook grammar in Matchbook.g.
 * @author gann
 */
@AllArgsConstructor
public class CaptureExpression implements Expression {
    @Getter private String id;
    @Getter private Expression expression;

    @Override
    public void accept(ExpressionVisitor visitor) {
        expression.accept(visitor);
        visitor.visit(this);
    }    
    
    @Override
    public String toString() {
        return id + "=" + expression;
    }
}
