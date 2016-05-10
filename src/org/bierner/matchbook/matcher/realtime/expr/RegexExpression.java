package org.bierner.matchbook.matcher.realtime.expr;


import lombok.Data;

/**
 * An expression that matches tokens by regular expression
 * @author gann
 */
@Data
public class RegexExpression implements Expression {
    private final String regex;

    @Override
    public String toString() {
        return "/" + regex + "/";
    }

    @Override
    public void accept(ExpressionVisitor visitor) {
        visitor.visit(this);
    }
}
