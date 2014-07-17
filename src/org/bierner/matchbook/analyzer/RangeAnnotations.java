/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author gann
 */
public class RangeAnnotations<T> implements Annotations<T> {
    private List<Annotation<T>> annotations = new ArrayList<>();

    public RangeAnnotations(List<? extends Annotation<T>> annotations) {
        for (Annotation<T> annotation : annotations)
            this.annotations.add(annotation);
    }
    
    public RangeAnnotations<T> add(Annotation<T> a) {
        annotations.add(a);
        return this;
    }
    
    @Override
    public int size() { 
        return annotations.size(); 
    }

    @Override
    public Annotation<T> get(int i) {
        return annotations.get(i);
    }

    @Override
    public Iterator<Annotation<T>> iterator() {
        return annotations.iterator();
    }

}
