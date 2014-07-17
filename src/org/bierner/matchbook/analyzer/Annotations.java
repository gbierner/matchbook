/*
 * Copyright (c) 2014, Gann Bierner
 */

package org.bierner.matchbook.analyzer;

/**
 * An Annotations is the result of a textual analysis of a given {@link AnnotationType} on a {@link Sentence}.  We
 * don't simply use a collection based container so that we can be wrap similar data structures from other packages
 * (eg Span[] in OpenNLP) and also be efficient about handling annotation types that always produce annotations
 * of length one.
 * 
 * Annotations should be ordered first by starting position then ending position.
 * 
 * @param <T> The type of the value of the annotation.
 * @author gann
 */
public interface Annotations<T> extends Iterable<Annotation<T>> {
    /**
     * Returns the number of annotations.
     * @return the number of annotations.
     */
    int size();

    /**
     * Returns the ith annotation.
     * @param i the annotation to retrieve
     * @return an {@link Annotation} object
     */
    Annotation<T> get(int i);
}
