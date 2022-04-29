/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.matcher.realtime.expr;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A repeat expression to as defined in the matchbook grammar described in Matchbook.g.
 * 
 * @author gann
 */
@AllArgsConstructor
public class RepeatExpression implements Expression {
    @Getter private Expression expression;
    @Getter private int        from;
    @Getter private int        to;
    
    @Override
    public void accept(ExpressionVisitor visitor) {
        expression.accept(visitor);
        visitor.visit(this);
    }    

    @Override
    public String toString() {
        if (from == 0 && to == 1)
            return expression + "?";
        else if (from == to)
            return expression + "(" + from + ")";
        else
            return expression + "(" + from + "," + to + ")";
    }
}
