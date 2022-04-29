/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.matcher.realtime.expr;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A "with" expression as defined in the matchboook grammar in Matchbook.g.
 * @author gann
 */
@AllArgsConstructor
public class WithExpression implements Expression {
    @Getter private AnnotationExpression annotation;
    @Getter private Expression           withExpression;
    
    @Override
    public void accept(ExpressionVisitor visitor) {
        withExpression.accept(visitor);
        annotation.accept(visitor);
        visitor.visit(this);
    }    

    @Override
    public String toString() {
        return annotation + " WITH " + withExpression;
    }
}
