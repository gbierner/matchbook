/*
 * Copyright (c) 2014, Gann Bierner
 */
package org.bierner.matchbook.matcher.realtime.expr;

/**
 * An expression visitor to be used in conjunction with {@link Expression#accept(org.bierner.matchbook.matcher.realtime.expr.ExpressionVisitor) }.
 * @author gann
 */
public interface ExpressionVisitor {
    void visit(AnnotationExpression expr);
    void visit(CaptureExpression expr);    
    void visit(CompoundExpression expr);        
    void visit(RepeatExpression expr);            
    void visit(WithExpression expr);                
}
