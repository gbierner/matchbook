/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer.opennlp;

import java.util.Iterator;
import lombok.AllArgsConstructor;
import lombok.Delegate;
import opennlp.tools.util.Span;
import org.bierner.matchbook.analyzer.Annotation;
import org.bierner.matchbook.analyzer.Annotations;

/**
 * An adapter from OpenNLP's Span array to Annotations.
 * 
 * @author gann
 */
@AllArgsConstructor
public class SpanAnnotations implements Annotations<String> {
    private Span[] span;

    @AllArgsConstructor
    private class SpanAnnotation implements Annotation<String> {
        @Delegate private Span span;
        @Override public String getId()    { return span.getType(); }
        @Override public String getValue() { return span.getType(); }
    }
    
    @Override
    public int size() {
        return span.length;
    }

    @Override
    public Annotation<String> get(int i) {
        return new SpanAnnotation(span[i]);
    }

    @Override
    public Iterator<Annotation<String>> iterator() {
        return new Iterator<Annotation<String>>() {
            private int pos = 0;
            @Override public boolean hasNext() { return pos < span.length; }
            @Override public Annotation<String> next() { return get(pos++); }
            @Override public void remove() { throw new UnsupportedOperationException("Annotations are immutable."); }
        };
    }

}
