/*
 * Copyright (c) 2014, Gann Bierner
 */
package org.bierner.matchbook.analyzer;

/**
 * A textual analysis module that identifies some, presumably, interesting component of
 * a {@link Sentence}.  A SentenceAnnotator should be registered with {@link AnnotationType},
 * with {@link AnnotationType#registerAnnotator(java.lang.Class, org.bierner.matchbook.analyzer.AnnotationType[])},
 * usually in a static block.
 * @author gann
 */
public interface SentenceAnnotator {
    /**
     * Sets annotations on the given sentence using {@link AnnotatableSentence#setAnnotations(org.bierner.matchbook.analyzer.AnnotationType, org.bierner.matchbook.analyzer.Annotations) }
     * @param sentence
     */
    void annotate(AnnotatableSentence sentence);
}
