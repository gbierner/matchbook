/*
 * Copyright (c) 2014, Gann Bierner
 */
package org.bierner.matchbook.analyzer;

/**
 *
 * @author gann
 */
public interface AnnotatableSentence extends Sentence {
    <T> void setAnnotations(AnnotationType<T> type, Annotations<T> annotations);
}
