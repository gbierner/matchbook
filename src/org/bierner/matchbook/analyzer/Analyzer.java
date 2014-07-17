/*
 * Copyright (c) 2014, Gann Bierner
 */
package org.bierner.matchbook.analyzer;

import java.util.List;
import java.util.Set;


/**
 * The high level wrapper between this library and any set of external textual analyzers.  Since
 * we do not rely on our own analysis, we need a consistent interface to other packages such as
 * OpenNLP, Stanford NLP, Java's internal tools, or whatever.  These could be mix-and-matched
 * or all come from the same place, so the Analyzer interface places no restrictions on how this
 * is done.  This library contains two implementations: a {@link SimpleAnalyzer} composed of
 * analyzing tools pre-constructed for a single language and a {@link MultiLingualAnalyzer} that
 * uses a supplied {@link LanguageDetector} and locale-based factories to analyze text for a
 * whatever languages are supported by the supplied factories.  Specific Analyzers could easily be written
 * to wrap entire projects if desired.
 * 
 * The bulk of analysis is done on the sentence level, so {@link Sentence} is where most of the action is.
 * An Analyzer would typically create Sentences from text and provide itself as the strategy for providing
 * annotations.  This is how {@link SimpleSentence} works.
 * 
 * @author gann
 */
public interface Analyzer {
    /**
     * Constructs a single sentence from the given text.  You would use this if you know that the given
     * text is only one sentence or if you don't care and you'd like to treat it as one unit regardless.
     * In the latter case, some analyses may provide unexpected results as they already expect
     * sentence detection to have occurred.
     *
     * @param text the text to analyze as one Sentence unit
     * @return a <@link Sentence> 
     */
    Sentence getSentence(String text);    
    
    /**
     * Sentence detects the given text.
     * 
     * @param text the text break into sentences
     * @return a list of sentences
     */
    List<Sentence> getSentences(String text);
    
    /**
     * Applies annotations from a sentence for the requested type.  See {@link AnnotationType} for some existing types and
     * information on how to construct and register your own.  An {@link AnnotatableSentence} is required for the annotations
     * to be set, allowing a {@link Sentence} implementation to protect itself from outside interference.
     * 
     * @param <T> The type of the annotations value
     * @param sentence the sentence from which to create the annotations.  It must be, specifically, an {@link AnnotatableSentence}
     * so that it can accept new annotations.
     * @param type the annotation type (eg token, stem, etc) to be computed and applied
     */
    <T> void applyAnnotations(AnnotatableSentence sentence, AnnotationType<T> type);
    
}
