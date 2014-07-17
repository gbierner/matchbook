/*
 * Copyright (c) 2014, Gann Bierner
 */
package org.bierner.matchbook.analyzer.ontology;

import java.util.Locale;
import org.bierner.matchbook.analyzer.Analyzer;
import org.bierner.matchbook.analyzer.AnnotationType;
import org.bierner.matchbook.analyzer.Annotations;
import org.bierner.matchbook.analyzer.Sentence;
import org.bierner.matchbook.analyzer.SimpleAnalyzer;
import org.bierner.matchbook.analyzer.java.BreakIteratorSentenceDetector;
import org.bierner.matchbook.analyzer.java.BreakIteratorTokenizer;
import org.bierner.matchbook.analyzer.opennlp.PorterStemmer;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author gann
 */
public class SimpleConceptAnnotatorTest {
    /**
     * Test of annotate method, of class SimpleConceptAnnotator.
     */
    @Test
    public void testAnnotate() {
        Ontologies.setLoader(new Ontologies.ResourceLoadStrategy("org/bierner/matchbook/analyzer/ontology"));
        Analyzer analyzer = SimpleAnalyzer.builder().
                locale(Locale.ENGLISH).
                sentenceDetector(new BreakIteratorSentenceDetector(Locale.ENGLISH)).
                annotator(new BreakIteratorTokenizer(Locale.ENGLISH)).
                annotator(new PorterStemmer()).
                annotator(new SimpleConceptAnnotator("animal.ont")).
                build();
        
        Sentence sentence = analyzer.getSentence("My canine has fleas");
        Annotations<Ontology.Concept> a = sentence.getAnnotations(AnnotationType.CONCEPT);
        assertEquals(1, a.size());
        assertEquals("dog", a.get(0).getId());
    }
}