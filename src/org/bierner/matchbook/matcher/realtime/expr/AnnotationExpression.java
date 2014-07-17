/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.matcher.realtime.expr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bierner.matchbook.analyzer.Annotation;

/**
 * An annotation expression as defined in the matchbook grammar in Matchbook.g.
 * @author gann
 */
@AllArgsConstructor
public class AnnotationExpression implements Expression {
    
    @Getter private String type;
    @Getter private String value;

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }    
    
    @Override
    public String toString() {
        if (value == null)
            return type;
        else {
            switch (type) {
                case Annotation.STEM:
                    return "''" + value + "''";
                case Annotation.TOKEN:
                    return '"' + value + '"';
                case Annotation.CONCEPT:
                    return '<' + value + '>';
                case Annotation.CHUNK:
                    return '[' + value + ']';
                case Annotation.POS:
                    return '{' + value + '}';
                default:
                    return type + ":" + value;
            }

        }
    }

}
