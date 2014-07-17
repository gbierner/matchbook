/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import lombok.AllArgsConstructor;
import org.bierner.matchbook.analyzer.Annotation.UnitAnnotation;

/**
 * An implementation of {@link Annotations} for cases where there is exactly one annotation per token of length one.  
 * This is useful for annotations like tokens, stems, pos tags, etc and is more efficient than an implementation containing
 * explicit span information.
 * @param <T> 
 * @author gann
 */
@AllArgsConstructor
public class UnitAnnotations<T> implements Annotations<T> {
    private List<T> valueList;
    
    /**
     * Returns the list of annotation values without the {@link Annotation} wrapper.
     * @return a list of annotations
     */
    public List<T> getValueList() {
        return Collections.unmodifiableList(valueList);
    }
    
    @Override
    public int size() {
        return valueList.size();
    }

    @Override
    public Annotation<T> get(int i) {
        return new UnitAnnotation<>(valueList.get(i).toString(), valueList.get(i), i);
    }

    @Override
    public Iterator<Annotation<T>> iterator() {
        return new Iterator<Annotation<T>>() {
            private int pos = 0;
            @Override public boolean hasNext() { return pos < valueList.size(); }
            @Override public Annotation<T> next() { return get(pos++); }
            @Override public void remove() { throw new UnsupportedOperationException("Annotations are immutable."); }
        };
    }
}
