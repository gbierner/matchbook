/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.matcher.realtime.expr;

import com.google.common.base.Joiner;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A catchall for any expression that is a collection of subexpressions with an operator as defined in the matchbook grammar in Matchbook.g.
 * Note that some such expressions (such as "with" expressions) are handled separately.
 * 
 * @author gann
 */
@AllArgsConstructor
public class CompoundExpression implements Expression {
    public enum Type { OR, IS, ISNT, SEQUENCE };
    
    @Getter private Type             type;
    @Getter private List<Expression> subExpressions;
    
    @Override
    public void accept(ExpressionVisitor visitor) {
        for (Expression expr : subExpressions)
            expr.accept(visitor);
        visitor.visit(this);
    }    

    @Override
    public String toString() {
        return   "(" 
               + Joiner.on(type.equals(Type.SEQUENCE)? " " : " " + type.name() + " ").join(subExpressions) 
               + ")";
    }
}
