/*
 * Copyright (c) 2014, Gann Bierner
 */
package org.bierner.matchbook.analyzer;

import java.util.Locale;
import org.bierner.matchbook.analyzer.util.SentenceUtilities;

/**
 * A single sentential unit, most likely as determined by an {@link Analyzer}.  This interface is kept as lean
 * as possible, but see {@link SentenceUtilities} for useful methods for working with sentences.  Also note that
 * Sentence objects are not modifiable through this interface.  See {@link AnnotatableSentence} for that.
 * @author gann
 */
public interface Sentence {
    /**
     * Returns the locale used to create this sentence.
     * @return a locale
     */
    Locale getLocale();

    /**
     * Returns the original text of this sentence.
     * @return a locale
     */
    String getText();

    /**
     * Returns the analyzer used to annotate this sentence.
     * @return an analyzer
     */
    Analyzer getAnalyzer();

    /**
     * Returns true if this sentence provides the requested annotation.
     * @param <T>
     * @param type
     * @return true if this sentence provides the requested annotation.
     */
    <T> boolean hasAnnotation(AnnotationType<T> type);

    /**
     * Returns the annotations for the requested type.
     * @param <T> the type of the annotation's value
     * @param type the requested type (token, stem, pos tag, chunk, etc)
     * @return an Annotations object
     * @throws UnsupportedOperationException if an annotation type is requested that is not supported by this sentences analyzer
     */
    <T> Annotations<T> getAnnotations(AnnotationType<T> type);
}
